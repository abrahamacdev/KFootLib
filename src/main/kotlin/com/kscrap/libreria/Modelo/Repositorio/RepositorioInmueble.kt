package com.kscrap.libreria.Modelo.Repositorio

import com.andreapivetta.kolor.Color
import com.kscrap.libreria.Modelo.Dominio.Inmueble
import com.kscrap.libreria.Utiles.Constantes
import com.kscrap.libreria.Utiles.Utils
import kotlinx.coroutines.*
import tech.tablesaw.api.Table
import tech.tablesaw.io.csv.CsvWriteOptions
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import com.kscrap.libreria.Controlador.Transmisor
import io.reactivex.subjects.PublishSubject
import tech.tablesaw.api.Row
import kotlin.coroutines.CoroutineContext

class RepositorioInmueble<T: Inmueble>(clazz: Class<T>, listaInmuebles: List<T>? = null, configuracion: ConfiguracionRepositorioInmueble = ConfiguracionRepositorioInmueble()): CoroutineScope {

    private lateinit var instanciaTipoActual: Inmueble                  // Nos servirá más adelante para obtener la información de los inmuebles que se creen
    private lateinit var tipoActual: Class<*>                           // Tipo de dato que almacena el repositorio

    private lateinit var nomCols: List<String>                          // Nombre de las columnas que almacena el dataframe
    private lateinit var dataframe: Table                               // Dataframe con los datos

    private lateinit var configuracion: ConfiguracionRepositorioInmueble; // Configuracion que se usara para el repositorio
    private var transmisor: Transmisor<Inmueble>? = null                // Transmisor al que conectamos el el repositorio para el envío automático de los datos

    private var guardadoRealizado = false                               // Primer guardado que realicemos para el repositorio actual
    private var permitirGuardado = true                                 // Permitira guardar los datos o por el contrario denegara la accion
    private var seHaCambiadoTipo = false                                // Permitira la modificacion de las columnas del dataframe en su estado inicial
    private var yaExisteArchivo: Boolean = false;                       // Comprobamos si el archivo ya ha sido creado
    private lateinit var nombreArchivo: String;                         // Nombre con extensión del archivo donde se escribirán los datos

    val job = Job()                                                     // Tarea asociada a la coroutina
    override val coroutineContext: CoroutineContext                     // Contexto de la coroutina
        get() = Dispatchers.IO + job

    companion object {

        /**
         * Creamos un {[ConjuntoInmuebleFactory]} del tipo necesitado
         *
         * @return {[ConjuntoInmuebleFactory]}
         */
        inline fun <reified T: Inmueble> create(listaInmueble: List<T>? = null, propiedades: ConfiguracionRepositorioInmueble = ConfiguracionRepositorioInmueble()): RepositorioInmueble<T> =
                RepositorioInmueble<T>(T::class.java, listaInmueble, propiedades)
    }

    init {

        // Establecemos la configuraciona utilizar
        this.configuracion = configuracion

        // Guardamos el tipo de dato que se utilizara
        this.instanciaTipoActual = clazz.newInstance()
        this.tipoActual = clazz

        // Creamos el dataframe y añadimos los inmuebles
        crearDataFrame(listaInmuebles)

        // Comprobamos si el archivo existe
        if (comprobarExistenciaArchivo()){

            // El archivo existe
            yaExisteArchivo = true

            // Comprobamos si las columnas del archivo coinciden con las del dataframe actual
            if(colsArchivoCoincidenConActuales()){



            }
        }


        /*// Comprobamos si el tipo de inmueble que se almacena en el archivo
        // es diferente al actual
        if (!archivoAlmacenaInmuebleActual()){

            // No coinciden los tipos, denegaremos el guardado
            permitirGuardado = false
        }*/


        // Comenzamos a emitir ticks para el guardado de los datos
        //establecerGuardadoAutomatico()
    }

    /**
     * Creamos el dataframe y añadimos datos si nos lo pasan
     *
     * @param listaInmuebles: Lista de inmuebles que añadiremos al dataframe
     */
    private fun crearDataFrame(listaInmuebles: List<T>? = null){

        // Creamos el dataframe
        dataframe = Table.create()

        // Añadimos las columnas al dataframe
        anadirColsDataframe(dataframe, instanciaTipoActual)

        // Establecemos los nombres de las columnas según los nombres de los atributos
        // del tipo actual
        this.nomCols = instanciaTipoActual.obtenerNombreAtributos()

        // Añadimos la lista de inmueble al dataframe
        anadirListaInmuebles(listaInmuebles)
    }

    /**
     * Añadimos al dataframe las columnas y sus respectivos tipos
     * a partir de los atributos del tipo de dato que se almacena
     *
     * @param df: Tabla a la que se le asignaran las columnas
     * @param inmueble: Inmueble del que estraeremos las columnas a establecer en [df]
     */
    fun anadirColsDataframe(df: Table, inmueble: Inmueble){

        val listaAtributos = inmueble.obtenerNombreTipoAtributos()

        with(df){
            addColumns(
                //Hacemos uso del operador "spread" (*), que nos permite pasar un Array a un vararg
                *listaAtributos.map {
                    Utils.castearAColumna(it.first,it.second)!!
                }.toTypedArray()
            )
        }
    }

    /**
     * Comprobamos si existe el archivo en el que
     * se guardaran los datos del dataframe
     *
     * @return Boolean: Si existe el archivo o no
     */
    fun comprobarExistenciaArchivo(): Boolean {

        // Nombre completo del archivo
        nombreArchivo = this.configuracion.getNombreArchivo() + "." + configuracion.getExtensionArchivo().name

        // Comprobamos la existencia del archivo
        val archivo = File(configuracion.getRutaGuardadoArchivos() + "/$nombreArchivo")
        if (archivo.exists() && archivo.isFile){
            return true
        }
        return false
    }



    /**
     * Añadimos el inmueble pasado por parámetro
     * a nuestro dataframe
     *
     * @param inmueble: Inmueble a añadir al dataframe
     */
    fun anadirInmueble(inmueble: T){

        // Comprobamos que el inmueble recibido por parametro sea diferente al ya establecido
        if (!inmueble.javaClass.canonicalName.equals(tipoActual.canonicalName)){

            // Comprobamos si ya hemos cambiado el tipo de dato del inmueble
            if (!seHaCambiadoTipo){

                cambiarTipoDataFrame(inmueble)
            }

            // El inmueble recibido es diferente al actual y ya hemos modificado el tipo de dato almacenado
            else {
                Utils.debug(Constantes.DEBUG.DEBUG_SIMPLE, "El tipo de inmueble almacenado en el repositorio ya ha sido modificado. El inmueble no se guardara.", Color.RED)
                return
            }
        }

        // Recorremos cada atributo del inmueble
        nomCols.forEach { atributo ->

            val valor = inmueble.obtenerValorDe(atributo,inmueble)

            // Evitamos setear valores extraños al dataframe
            if (valor != null){
                dataframe.column(atributo).appendCell(valor)
            }
        }

        // Si hay un transmisor le pasamos los inmuebles
        if (transmisor != null){
            transmisor!!.enviarInmueble(inmueble)
        }

    }

    /**
     * Añadimos los inmuebles de la lista al dataframe para su posterior
     * guardado
     *
     * @param inmuebles: Lista con los inmuebles
     */
    fun anadirListaInmuebles(listaInmuebles: List<T>?){

        // Añadimos los datos al dataframe
        if (listaInmuebles != null) {
            if (listaInmuebles.size > 0){

                // Recorremos cada uno de los inmuebles
                listaInmuebles.forEach {inmueble ->

                    anadirInmueble(inmueble)

                }
            }
        }
    }



    /**
     * Ejecutamos un volcado de datos en el archivo
     * y directorio deseados. Se creará un nuevo hilo que será el encargado de
     * guardar los datos bloqueando el archivo.
     *
     *  @return Sujeto que nos permitira saber cuando se ha completado el guardado
     */
    suspend fun guardar(avisoGuardado: PublishSubject<Nothing>? = null) {

        // Comprobamos que el guardado este permitido
        if (permitirGuardado){

            // Comprobamos que la tabla tenga datos para guardar
            if (dataframe.rowCount() > 0){

                // Ejecutamos el proceso de escritura en una corutina dedicada
                launch (coroutineContext){

                    // Añadimos los datos al archivo
                    val bufferedWriter = BufferedWriter(FileWriter(File(configuracion.getRutaGuardadoArchivos() + "/$nombreArchivo"),true))

                    // Evitamos que se escriba en el archivo desde otra parte
                    synchronized(bufferedWriter){
                        val opciones: CsvWriteOptions;

                        // Si el archivo ya existe, no escribiremos las cabeceras
                        if (yaExisteArchivo){
                            opciones = CsvWriteOptions.builder(bufferedWriter).header(false).build()
                        }

                        // Sino las añadiremos
                        else {
                            opciones = CsvWriteOptions.builder(bufferedWriter).build()
                        }


                        dataframe.write().csv(opciones)         // Escribimos los datos en el archivo

                        bufferedWriter.close()                  // Cerramos el búffer

                        dataframe = dataframe.emptyCopy()       // Eliminamos los datos del dataframe

                        // Si el archivo no existía ha sido creado
                        if (!yaExisteArchivo){
                            yaExisteArchivo = true
                        }

                        // COmprobamos si es el primer guardado que realizamos
                        if (!guardadoRealizado){
                            guardadoRealizado = true
                        }

                        // Avisaremos del guardado por el sujeto pasado como parametro
                        if (avisoGuardado != null){
                            avisoGuardado.onComplete()
                        }
                    }
                }.join()
            }
        }
    }

    /**
     * Comprobamos si aún quedan datos en el
     * {[dataframe]}
     *
     * @param Boolean si aún quedan datos
     */
    fun todoGuardado(): Boolean{
        return dataframe.count() == 0
    }



    /**
     * Comprobamos si las columnas del archivo coinciden con
     * con las columnas del dataframe actual
     */
    private fun colsArchivoCoincidenConActuales(): Boolean{

        // Lista con las cabeceras del archivo
        val cabecerasArchivo = cargarNomColsArchivo()

        // Comprobamos que halla cols en la lista
        if (cabecerasArchivo != null){

            // Comprobamos que las cabeceras de ambas lista coincidan
            if (colsDataframesCoinciden(nomCols,cabecerasArchivo)){

                return true
            }
        }

        return false
    }

    /**
     * Comprobamos que las columnas de los dataframes pasados por parametro
     * coincidan
     * *Solo comprueba que los nombres de las columnas coincidan
     *
     * @param nuevoDataframe: Nuevo dataframe
     * @param viejoDataframe: VIejo dataframe
     */
    private fun colsDataframesCoinciden(nuevoDataframe: Table, viejoDataframe: Table): Boolean{

        println(nuevoDataframe)
        println(nuevoDataframe.columnCount())
        println(nomCols.size)

        // Comprobamos primero que la cuenta de columnas coincidan
        if (nuevoDataframe.columnCount() == nomCols.size){

            // Array con los nombres de las columnas del nuuevo dataframe
            val cabecerasNuevoDataframe = nuevoDataframe.columnArray()

            // Array con los nombres de las columnas del viejo dataframe
            val cabecerasViejoDataframe = viejoDataframe.columnArray()

            // Comprobamos si todas las cols del archivo tienen los mismos
            // nombres que las cols del dataframe actual
            val noCoincide = cabecerasNuevoDataframe.firstOrNull {
                !cabecerasViejoDataframe.contains(it)
            }

            // Estan todas las cols
            if (noCoincide == null){
                return true
            }
        }
        return false
    }

    /**
     * Comprobamos que las columnas de los dataframes pasados por parametro
     * coincidan
     * *Solo comprueba que los nombres de las columnas coincidan
     *
     * @param nuevasCabeceras: Nuevas cabeceras
     * @param viejasCabeceras: VIejos cabeceras
     */
    private fun colsDataframesCoinciden(nuevasCabeceras: List<String>, viejasCabeceras: List<String>): Boolean{

        // Comprobamos primero que la cuenta de columnas coincidan
        if (nuevasCabeceras.size == viejasCabeceras.size){

            // Comprobamos si todas las cols del archivo tienen los mismos
            // nombres que las cols del dataframe actual
            val noCoincide = nuevasCabeceras.firstOrNull {
                !viejasCabeceras.contains(it)
            }

            // Estan todas las cols
            if (noCoincide == null){
                return true
            }
        }
        return false
    }



    /**
     * Obtenemos los datos del archivo pasado por parametros
     *
     * @return ArrayList<String>?: Lista con los nombres de la cabecera
     */
    fun cargarNomColsArchivo(): List<String>?{

        // Lista con los nombres de la cabecera del archivo
        var cabeceras: ArrayList<String>? = null

        // Comprobamos que primero exista el archivo
        if (yaExisteArchivo){

            // Inicializamos la lista
            cabeceras = ArrayList()

            // Leemos los datos del archivo
            val tabla = Table.read().csv(configuracion.getRutaGuardadoArchivos() + "/" + nombreArchivo)

            // Añadimos a la lista los nombres de las columnas
            tabla.columnArray().forEach { cabeceras.add(it.name()) }
        }

        return cabeceras
    }

    /**
     * Cambiamos el tipo de dato que se almacena en el dataframe
     * por el tipo de inmueble pasado como parametro
     *
     * @param nuevoInmueble: Nuevo inmueble que se va a almacenar en el dataframe
     */
    private fun cambiarTipoDataFrame(nuevoInmueble: Inmueble){

        // Dataframe con las columnas del nuevo tipo
        var tmpDataFrame = Table.create()

        // Añadimos al dataframe temporal las columnas del nuevo inmueble
        anadirColsDataframe(tmpDataFrame, nuevoInmueble)

        // Clonamos los datos del dataframe actual al nuevo dataframe
        clonarDatos(tmpDataFrame, nuevoInmueble)

        // Establecemos el nuevo dataframe con las nuevas columnas
        dataframe = tmpDataFrame

        // Nuevo tipo de inmueble que almacena el dataframe
        this.tipoActual = nuevoInmueble.javaClass

        // Instancia del inmueble almacenado
        this.instanciaTipoActual = nuevoInmueble

        // Ya no podremos cambiar más el tipo de dato de este dataframe
        this.seHaCambiadoTipo = true

        // Guardamos los viejos nombres de columnas
        val viejosNomCols = nomCols

        // Establecemos los nuevos nombres de las columnas
        this.nomCols = nuevoInmueble.obtenerNombreAtributos()

        // Volvemos a comprobar si podemos guardar los datos
        if (colsDataframesCoinciden(nomCols,viejosNomCols)){
            permitirGuardado = true
        }
    }

    /**
     *  Clonamos los datos del dataframe actual al pasado
     *  por parametro
     *
     *  @param nuevoDataframe: Nuevo dataframe al que le añadiremos los datos
     *  @param nuevoInmueble: Tipo de inmueble nuevo
     *
     */
    fun clonarDatos(nuevoDataframe: Table, nuevoInmueble: Inmueble){

        val nuevaClase = nuevoInmueble.javaClass

        // Recorremos las filas del dataframe
        dataframe.forEach{fila ->

            val nuevaInstancia = nuevaClase.newInstance()

            // Recorremos cada columna
            fila.columnNames().forEach{nomCol ->

                // Obtenemos el valor de la celda actual
                val valor = fila.getObject(nomCol)

                // Establecemos los valores a la nueva instancia
                nuevaInstancia.establecerValor(nomCol,valor,nuevaInstancia)
            }


            // Añadimos los valores al nuevo dataframe
            nuevoDataframe.columns().forEach{ col ->

                // Obtenemos el valor del objeto
                val valor = nuevaInstancia.obtenerValorDe(col.name(),nuevaInstancia)

                // Añadimos el valor al nuevo dataframe
                nuevoDataframe.column(col.name()).appendCell(valor)
            }
        }
    }



    /**
     * Covertimos los datos almacenados en el datafram en una lista
     * de inmuebles
     *
     * @return ArrayList<T>: Lista con los inmuebles
     */
    fun obtenerInmueblesAlmacenados(): ArrayList<Inmueble> {

        val listaInmuebles: ArrayList<Inmueble> = ArrayList()

        // Recorremos cada tupla del dataframe
        dataframe.forEach{fila ->

            // Creamos una instancia del tipo de inmueble que se almacena en la tabla
            val objInmueble = tipoActual.newInstance() as Inmueble

            // Recorremos cada columna de la tupla actual
            fila.columnNames().forEach {nombreCol ->

                // Obtenemos el campo actual de la tupla que estemos recorriendo
                var valorColumna = fila.getObject(nombreCol)

                // Establecemos el valor de la columna actual al respectivo atributo del inmueble
                objInmueble.establecerValor(nombreCol,valorColumna,objInmueble)

            }

            // Añadimos el inmueble a la lista
            listaInmuebles.add(objInmueble as Inmueble)

        }

        return listaInmuebles
    }

    /**
     * Conectamos el {[RepositorioInmueble]} actual con un {[Transmisor]}
     * para que cada vez que añadamos datos al dataframe se transmitan
     * a través de este
     *
     * @param transmisor: Transmisor al que se conectará el repositorio
     */
    fun conectarConTransmisor(transmisor: Transmisor<T>){
        if (!transmisor.transmisionTerminada()){
            this.transmisor = transmisor as Transmisor<Inmueble>
        }
    }
}