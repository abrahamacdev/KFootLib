package com.kscrap.libreria.Modelo.Repositorio

import com.kscrap.libreria.Modelo.Dominio.Inmueble
import com.kscrap.libreria.Utiles.Constantes
import com.kscrap.libreria.Utiles.Utils
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.*
import tech.tablesaw.api.Table
import tech.tablesaw.io.csv.CsvWriteOptions
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.kscrap.libreria.Controlador.Transmisor

class RepositorioInmueble<T: Inmueble>(clazz: Class<T>, listaInmuebles: List<T>? = null, configuracion: ConfiguracionRepositorioInmueble = ConfiguracionRepositorioInmueble()) {

    private lateinit var inmueble: Inmueble                             // Nos servirá más adelante para obtener la información de los inmuebles que se creen
    private lateinit var tipoActual: Class<T>                           // Tipo de dato que almacena el repositorio
    private lateinit var nomCols: List<String>                          // Nombre de las columnas que almacena el dataframe
    private lateinit var dataframe: Table                               // Dataframe con los datos
    private var yaExisteArchivo: Boolean = false;                       // Comprobamos si el archivo ya ha sido creado
    private lateinit var configuracion: ConfiguracionRepositorioInmueble; // Conjunto de configuracion que se usarán pòr defecto
    private lateinit var nombreArchivo: String;                         // Nombre con extensión del archivo donde se escribirán los datos
    private var transmisor: Transmisor<T>? = null                       // Transmisor al que conectamos el el repositorio para el envío automático de los datos
    private var seHaCambiadoTipo = false                                // No servirá para modificar las columnas del dataframe

    companion object {

        private var guardandoAutomaticamente = false                    // Nos ayuda a saber si actualmente estamos guardando de forma automática los datos


        /**
         * Creamos un {[ConjuntoInmuebleFactory]} del tipo necesitado
         *
         * @return {[ConjuntoInmuebleFactory]}
         */
        inline fun <reified T: Inmueble> create(listaInmueble: List<T>? = null, propiedades: ConfiguracionRepositorioInmueble = ConfiguracionRepositorioInmueble()): RepositorioInmueble<T> =
                RepositorioInmueble<T>(T::class.java, listaInmueble, propiedades)
    }


    init {

        // Guardamos las configuracion que utilizaremos
        this.configuracion = configuracion

        // Guardamos una instancia del objeto que se almacenará
        this.inmueble = clazz.newInstance()

        // Tipo de dato que se está almacenando en el repositorio
        this.tipoActual = clazz

        // Comprobamos que el archivo en el que se guardarán los datos exista
        val tempNombreArchivo = this.configuracion.getNombreArchivo() ?: obtenerNombreArchivoDefecto()
        val archivoCompleto = tempNombreArchivo + determinarExtension()
        nombreArchivo = archivoCompleto
        val archivo = File(configuracion.getRutaGuardadoArchivos() + "/$archivoCompleto")
        if (archivo.exists() && archivo.isFile) {
            yaExisteArchivo = true
        }

        // Comenzamos a emitir ticks para el guardado de los datos
        establecerGuardadoAutomatico()

        // Creamos el dataframe y añadimos los inmuebles
        crearDataFrame(listaInmuebles)
    }

    /**
     * Creamos el dataframe y añadimos datos si nos lo pasan
     *
     * @param listaInmuebles: Lista de inmuebles que añadiremos al dataframe
     */
    private fun crearDataFrame(listaInmuebles: List<T>? = null){


        dataframe = Table.create()
        val listaAtributos = inmueble.obtenerNombreTipoAtributos() // Nombre de los atributos y su tipo

        with(dataframe){

            addColumns(
                //Hacemos uso del operador "spread" (*), que nos permite pasar un Array a un vararg
                 *listaAtributos.map {
                    Utils.castearAColumna(it.first,it.second)!!
                }.toTypedArray()
            )
        }

        // Establecemos los nombres de las columnas según los nombres de los atributos
        // del tipo actual
        this.nomCols = inmueble.obtenerNombreAtributos()

        // Añadimos la lista de inmueble al dataframe
        anadirListaInmuebles(listaInmuebles)
    }

    /**
     * Obtenemos un nombre por defecto para el archivo en caso de que no
     * se proporcione uno al crear el objeto
     *
     * @return String: Nombre del archivo por defecto
     */
    private fun obtenerNombreArchivoDefecto(): String{
        val dtf: DateTimeFormatter = DateTimeFormatter.ofPattern("dd_MM_HH_mm")
        val ahora: LocalDateTime = LocalDateTime.now()
        return "KScrap_${dtf.format(ahora)}"
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
     * Añadimos el inmueble pasado por parámetro
     * a nuestro dataframe
     *
     * @param inmueble: Inmueble a añadir al dataframe
     */
    fun anadirInmueble(inmueble: T){

        // El inmueble recibido es un hijo de la clase "Inmueble" por lo que cambiaremos los datos de los inmuebles que se almacenan
        if (!inmueble.javaClass.canonicalName.equals(tipoActual.javaClass.canonicalName) && !seHaCambiadoTipo && inmueble.javaClass.superclass.canonicalName.equals(this.inmueble.javaClass.canonicalName)){
            cambiarDataframe(inmueble)
        }

        // Recorremos cada atributo del inmueble
        nomCols.forEach { atributo ->

            val valor = inmueble.obtenerValorDe(atributo,inmueble)
            dataframe.column(atributo).appendCell(valor)

        }

        // Si hay un transmisor le pasamos los inmuebles
        if (transmisor != null){
            transmisor!!.enviarInmueble(inmueble)
        }
    }

    /**
     * Ejecutamos un volcado de datos en el archivo
     * y directorio deseados. Se creará un nuevo hilo que será el encargado de
     * guardar los datos bloqueando el archivo.
     */
    fun guardar(){

        // Comprobamos que la tabla tenga datos para guardar
        if (dataframe.rowCount() > 0){

            runBlocking {

                // Ejecutamos el proceso de escritura en una corutina dedicada
                async(Dispatchers.IO){

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
                        yaExisteArchivo = true                  // Si el archivo no existía ha sido creado
                    }
                }
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
     * Activamos el guardado automático y comenzamos a emitir ticks para el guardado
     * de los datos
     */
    private fun establecerGuardadoAutomatico(){

        // Comprobamos que el guardado automático esté establecido
        if (this.configuracion.getGuardadoAutomatico()){

            Observable.interval(this.configuracion.getIntervalos(), this.configuracion.getUnidadTiempo()).subscribe(object :
                Observer<Any> {
                override fun onComplete() {
                    guardandoAutomaticamente = false            // Vamos a parar de guardar automáticamente
                }

                override fun onSubscribe(d: Disposable) {
                    guardandoAutomaticamente = true             // Hemos comenzado a recibir los ticks
                }

                override fun onNext(t: Any) {

                    if (guardandoAutomaticamente == false){
                        onComplete()
                    }

                    else {
                        guardar()                               // Guardamos los datos que halla hasta el momento
                    }
                }

                override fun onError(e: Throwable) {
                    guardandoAutomaticamente = false            // Vamos a parar de guardar automáticamente
                    e.printStackTrace()
                }

            })
        }
    }

    /**
     * Convertimos {[Constantes.EXTENSIONES_ARCHIVOS]} a una extensión válida
     * para nuestro archivo
     *
     * @return String: Extensíon a usar en el archivo
     */
    private fun determinarExtension(): String{

        var extension: String = "";

        when{
            configuracion.getExtensionArchivo() == Constantes.EXTENSIONES_ARCHIVOS.CSV -> { extension = ".csv" }
        }

        return extension
    }

    /**
     * Cambiamos los campos que se almacenarán en el dataframe para
     * satisfacer la necesidad de guardar los datos del nuevo tipo
     * de inmueble
     *
     * @param inmueble: Nuevo tipo de inmueble
     */
    private fun cambiarDataframe(inmueble: Inmueble){

        var tmpDataframe = Table.create()
        val listaAtributos = inmueble.obtenerNombreTipoAtributos()

        with(tmpDataframe){

            addColumns(
                    //Hacemos uso del operador "spread" (*), que nos permite pasar un Array a un vararg
                    *listaAtributos.map {
                        Utils.castearAColumna(it.first,it.second)!!
                    }.toTypedArray()
            )
        }

        // Nueva lista de columnas
        this.nomCols = inmueble.obtenerNombreAtributos()

        // Hacemos una copia de los antiguos datos a la nueva tabla
        tmpDataframe = copiarDatosDataframe(tmpDataframe,dataframe)

        // Ya no podremos cambiar más el tipo de dato de este dataframe
        //seHaCambiadoTipo = false

    }

    /**
     * Copiamos los datos almacenados del antiguo dataframe al
     * nuevo
     */
    private fun copiarDatosDataframe(dfNuevo: Table, dfViejo: Table): Table{

        val inmuebles = ArrayList<Inmueble>()

        dfViejo.forEach {fila ->

            var inmueble = inmueble.javaClass.newInstance()

            fila.columnNames().forEach {nomCol ->
                inmueble.establecerValor(nomCol, fila.getObject(nomCol), inmueble)
            }
        }

        System.exit(1)

        return dfViejo
    }

    /**
     * Covertimos los datos almacenados en el datafram en una lista
     * de inmuebles
     *
     * @return ArrayList<T>: Lista con los inmuebles
     */
    fun obtenerInmueblesAlmacenados(): ArrayList<T> {

        val listaInmuebles: ArrayList<T> = ArrayList()

        // Recorremos cada tupla del dataframe
        dataframe.forEach{fila ->

            // Creamos una instancia del tipo de inmueble que se almacena en la tabla
            val objInmueble = tipoActual.newInstance()

            // Recorremos cada columna de la tupla actual
            fila.columnNames().forEach {nombreCol ->

                // Obtenemos el campo actual de la tupla que estemos recorriendo
                var valorColumna = fila.getObject(nombreCol)

                // Establecemos el valor de la columna actual al respectivo atributo del inmueble
                objInmueble.establecerValor(nombreCol,valorColumna,objInmueble)

            }

            // Añadimos el inmueble a la lista
            listaInmuebles.add(objInmueble as T)

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
            this.transmisor = transmisor
        }
    }
}