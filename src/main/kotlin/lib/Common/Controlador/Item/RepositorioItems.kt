package lib.Common.Controlador.Item

import KFoot.Constantes
import com.andreapivetta.kolor.Color
import lib.Common.Utiles.Utils
import kotlinx.coroutines.*
import tech.tablesaw.api.Table
import java.io.File
import io.reactivex.subjects.PublishSubject
import tech.tablesaw.io.csv.CsvWriteOptions
import java.io.BufferedWriter
import java.io.FileWriter
import kotlin.coroutines.CoroutineContext

class RepositorioItems<T: Item>(clazz: Class<T>, listaInmuebles: List<T>? = null, val configuracion: ConfiguracionRepositorioItems = ConfiguracionRepositorioItems()): CoroutineScope {

    // --- Tipo de dato del Repositorio ---
    private var instanciaTipoActual: Item = clazz.newInstance()      // Nos servirá más adelante para obtener la información de los items que se creen
    private var tipoActual: Class<*> = clazz                         // Tipo de dato que almacena el repositorio
    // -----

    // --- Referente al Dataframe ---
    private lateinit var dataframe: Table                           // Dataframe con los datos
    private lateinit var nomCols: List<String>                      // Nombre de las columnas que almacena el dataframe
    // -----

    // --- Lógica de la clase ---
    private var seHaCambiadoTipo = false                            // Permitira la modificacion de las columnas del dataframe en su estado inicial
    // -----

    // --- Coroutina de la clase ---
    val job = Job()                                                 // Tarea asociada a la coroutina
    override val coroutineContext: CoroutineContext                 // Contexto de la coroutina
        get() = Dispatchers.IO + job
    // -----

    companion object {

        /**
         * Creamos un {[ConjuntoInmuebleFactory]} del tipo necesitado
         *
         * @return {[ConjuntoInmuebleFactory]}
         */
        inline fun <reified T: Item> create(listaInmueble: List<T>? = null, propiedades: ConfiguracionRepositorioItems = ConfiguracionRepositorioItems()): RepositorioItems<T> =
                RepositorioItems<T>(T::class.java, listaInmueble, propiedades)
    }

    init {

        // Creamos el dataframe y añadimos los inmuebles
        crearDataFrame(listaInmuebles)

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
        anadirListaItems(listaInmuebles)
    }



    /**
     * Añadimos el item pasado por parámetro
     * a nuestro dataframe
     *
     * @param item: Item a añadir al dataframe
     */
    fun anadirItem(item: T){

        // Comprobamos que el item recibido por parametro sea diferente al ya establecido
        if (!item.javaClass.canonicalName.equals(tipoActual.canonicalName)){

            // Comprobamos si ya hemos cambiado el tipo de dato del item
            if (!seHaCambiadoTipo){

                cambiarTipoDataFrameActual(item)
            }

            // El item recibido es diferente al actual y ya hemos modificado el tipo de dato almacenado
            else {
                KFoot.Utils.debug(Constantes.DEBUG.DEBUG_SIMPLE, "El tipo de item almacenado en el repositorio ya ha sido modificado. El item no se guardara.", Color.RED)
                return
            }
        }

        // Recorremos cada atributo del item
        nomCols.forEach { atributo ->

            val valor = item.obtenerValorDe(atributo,item)

            // Evitamos setear valores extraños al dataframe
            if (valor != null){
                dataframe.column(atributo).appendCell(valor)
            }
        }
    }

    /**
     * Añadimos los items de la lista al dataframe para su posterior
     * guardado
     *
     * @param listaItems: Lista con los items a añadir
     */
    fun anadirListaItems(listaItems: List<T>?){

        // Añadimos los datos al dataframe
        if (listaItems != null) {
            if (listaItems.size > 0){

                // Recorremos cada uno de los inmuebles
                listaItems.forEach { inmueble ->

                    anadirItem(inmueble)

                }
            }
        }
    }



    /**
     * Ejecutamos un volcado de datos en el archivo
     * y directorio deseados. Se creará un nuevo hilo que será el encargado de
     * guardar los datos bloqueando el archivo.
     *
     *  @return PublishSubject<Nothing>?: que nos permitira saber cuando se ha completado el guardado
     */
    suspend fun guardar(): PublishSubject<Nothing>? {

        // Comprobamos que el guardado este permitido
        if (permitirGuardado){

            // Comprobamos que la tabla tenga datos para guardar
            if (!todoGuardado()){

                // Sujeto por el que transmitiremos la finalizacion del guardado
                val avisoGuardado: PublishSubject<Nothing> = PublishSubject.create()

                when {

                    // Se guardará en un archivo CSV
                    configuracion.getExtensionArchivo() == Constantes.EXTENSIONES_ARCHIVOS.csv -> { guardarCSV(avisoGuardado) }
                }
            }
        }

        return null
    }

    /**
     * Realizaremos el guardado de los datos en un archivo CSV
     *
     * @param Sujeto que nos permitira saber cuando se ha completado el guardado
     */
    private suspend fun guardarCSV(avisoGuardado: PublishSubject<Nothing>?){

        // Ejecutamos el proceso de escritura en una corutina dedicada
        launch (coroutineContext){

            // Añadimos los datos al archivo
            val bufferedWriter = BufferedWriter(FileWriter(File(configuracion.getRutaGuardadoArchivos() + "/$nombreArchivo"),true))

            // Evitamos que se escriba en el archivo desde otra parte
            synchronized(bufferedWriter){
                val opciones: CsvWriteOptions

                opciones = CsvWriteOptions.builder(bufferedWriter).build()

                // Escribimos los datos en el archivo
                dataframe.write().csv(opciones)

                // Cerramos el búffer
                bufferedWriter.close()

                // Eliminamos los datos del dataframe
                dataframe = dataframe.emptyCopy()

                // Comprobamos si es el primer guardado que realizamos
                if (!primerGuardadoRealizado){
                    primerGuardadoRealizado = true
                }
            }
        }.join()
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
     * Cambiamos el tipo de dato que se almacena en el dataframe
     * por el tipo de inmueble pasado como parametro
     *
     * @param nuevoItem: Nuevo inmueble que se va a almacenar en el dataframe
     */
    private fun cambiarTipoDataFrameActual(nuevoItem: Item){

        // Comprobamos que no se haya cambiado ya el tipo
        if(!seHaCambiadoTipo){

            // Comprobamos que el item nuevo sea una subclase del actual
            if (KFoot.Utils.esSubclase(nuevoItem.javaClass,tipoActual)){

                // Dataframe con las columnas del nuevo tipo
                val tmpDataFrame = Table.create()

                // Añadimos al dataframe temporal las columnas del nuevo item
                anadirColsDataframe(tmpDataFrame, nuevoItem)

                // Clonamos los datos del dataframe viejo al nuevo
                clonarDatos(tmpDataFrame, nuevoItem)

                // Establecemos el nuevo dataframe con las nuevas columnas
                dataframe = tmpDataFrame

                // Nuevo tipo de inmueble que almacena el dataframe
                this.tipoActual = nuevoItem.javaClass

                // Instancia del inmueble almacenado
                this.instanciaTipoActual = nuevoItem

                // Guardamos los viejos nombres de columnas
                val viejosNomCols = nomCols

                // Establecemos los nuevos nombres de las columnas
                this.nomCols = nuevoItem.obtenerNombreAtributos()

                // Volvemos a comprobar si podemos guardar los datos
                /*if (colsDataframesCoinciden(nomCols,viejosNomCols)){
                    permitirGuardado = true
                }*/

                // Ya no podremos cambiar más el tipo de dato de este dataframe
                this.seHaCambiadoTipo = true
            }
        }
    }

    /**
     * Añadimos al dataframe las columnas y sus respectivos tipos
     * a partir de los atributos del tipo de dato que se almacena
     *
     * @param df: Tabla a la que se le asignaran las columnas
     * @param item: Item del que estraeremos las columnas a establecer en [df]
     */
    fun anadirColsDataframe(df: Table, item: Item){

        val listaAtributos = item.obtenerNombreTipoAtributos()

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
     *  Clonamos los datos del dataframe actual al pasado
     *  por parametro
     *
     *  @param nuevoDataframe: Nuevo dataframe al que le añadiremos los datos
     *  @param nuevoItem: Tipo de inmueble nuevo
     *
     */
    fun clonarDatos(nuevoDataframe: Table, nuevoItem: Item){

        val nuevaClase = nuevoItem.javaClass

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
    fun obtenerInmueblesAlmacenados(): ArrayList<Item> {

        val listaItems: ArrayList<Item> = ArrayList()

        // Recorremos cada tupla del dataframe
        dataframe.forEach{fila ->

            // Creamos una instancia del tipo de inmueble que se almacena en la tabla
            val objInmueble = tipoActual.newInstance() as Item

            // Recorremos cada columna de la tupla actual
            fila.columnNames().forEach {nombreCol ->

                // Obtenemos el campo actual de la tupla que estemos recorriendo
                var valorColumna = fila.getObject(nombreCol)

                // Establecemos el valor de la columna actual al respectivo atributo del inmueble
                objInmueble.establecerValor(nombreCol,valorColumna,objInmueble)

            }

            // Añadimos el inmueble a la lista
            listaItems.add(objInmueble as Item)

        }

        return listaItems
    }
}