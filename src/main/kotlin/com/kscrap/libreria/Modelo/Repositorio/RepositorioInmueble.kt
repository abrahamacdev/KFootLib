package com.kscrap.libreria.Modelo.Repositorio

import com.kscrap.libreria.Modelo.Dominio.Inmueble
import com.kscrap.libreria.Utiles.Constantes
import com.kscrap.libreria.Utiles.Utils
import com.andreapivetta.kolor.Color
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
import java.util.concurrent.TimeUnit

class RepositorioInmueble<T: Inmueble>(clazz: Class<T>, listaInmuebles: List<T>? = null, propiedades: Propiedades = Propiedades()) {

private lateinit var tipoActual: Class<T>                               // Tipo de dato que se almacena en el dataframe
    private lateinit var nomCols: List<String>                          // Nombre de las columnas que almacena el dataframe
    private lateinit var dataframe: Table                               // Dataframe con los datos
    private var yaExisteArchivo: Boolean = false;                       // Comprobamos si el archivo ya ha sido creado
    private lateinit var propiedades: Propiedades;                      // Conjunto de propiedades que se usarán pòr defecto
    private lateinit var nombreArchivo: String;                         // Nombre con extensión del archivo donde se escribirán los datos

    companion object {

        private var guardandoAutomaticamente = false                    // Nos ayuda a saber si actualmente estamos guardando de forma automática los datos


        /**
         * Creamos un {[ConjuntoInmuebleFactory]} del tipo necesitado
         *
         * @return {[ConjuntoInmuebleFactory]}
         */
        inline fun <reified T: Inmueble> create(listaInmueble: List<T>? = null, propiedades: Propiedades = Propiedades()): RepositorioInmueble<T> =
                RepositorioInmueble<T>(T::class.java, listaInmueble, propiedades)
    }

    /**
     * {[guardadoAutomatico]} ->    Permite ejecutar un guardado periodicamente
     * {[intervalos]} ->            Cada cuanto tiempo se ejecutará el guardado automático (se necesita activar "guardadoAutomatico"). Por defecto se guardará cada 30 segundos
     * {[unidadTiempo]} ->          Unidad de tiempo a emplear para las emisiones periódicas (se necesita activar "guardadoAutomatico"). Por defecto se guardará cada 30 segundos
     * {[rutaGuardadoArchivos]} ->  Ruta en la que se guardará los archivos. Por defecto se guardará en el directorio "Documentos"
     * {[nombreArchivo]} ->         Nombre que tendra el archivo. Por defecto se le asignará uno si el valor de este es "null"
     * {[extensionArchivo]} ->             Extensión que usará el archivo. Por defecto será "csv"
     */
    class Propiedades(private var guardadoAutomatico: Boolean = false, private var intervalos: Long = 30,
                      private var unidadTiempo: TimeUnit = TimeUnit.SECONDS, private var rutaGuardadoArchivos: String? = Utils.obtenerDirDocumentos(),
                      private var nombreArchivo: String? = null, private var extensionArchivo: Constantes.EXTENSIONES_ARCHIVOS = Constantes.EXTENSIONES_ARCHIVOS.CSV){

        init {

            // Queremos guardado automático pero los intervalos pasados no son válidos
            if (guardadoAutomatico == true && !intervalosGuardadoAutValidos(intervalos,unidadTiempo)){
                Utils.debug(Constantes.DEBUG.DEBUG_SIMPLE,"No se ha establecido el guardado automático porque los intervalos son demasiado cortos.", Color.RED);
                guardadoAutomatico = false
                intervalos = 30
                unidadTiempo = TimeUnit.SECONDS
            }

            // Comprobamos que la ruta de guardado de los archivos sea válida
            if (rutaGuardadoArchivos != null){
                if (!rutaDeGuardadoValida(rutaGuardadoArchivos!!)){
                    Utils.debug(Constantes.DEBUG.DEBUG_SIMPLE,"La ruta proporcionada para el almacenamiento de los inmuebles no es válida. " +
                            "Se usará $rutaGuardadoArchivos", Color.RED)
                }
            }
        }

        /**
         * Activamos la opción de guardar de forma automática
         * la información del dataframe cada {[intervalos]}
         *
         * @param intervalos: Cada cuanto tiempo se guardará la información
         * @param timeUnit; Unidad de tiempo que se utilizara entre ticks
         */
        fun guardaCada(intervalos: Long = 30, unidadTiempo: TimeUnit = TimeUnit.SECONDS){

            // Evitamos los ticks de menos de 3 segundos
            if (!intervalosGuardadoAutValidos(intervalos,unidadTiempo)){
                Utils.debug(Constantes.DEBUG.DEBUG_SIMPLE,"No se ha establecido el guardado automático porque los intervalos son demasiado cortos.", Color.RED);
            }

            else {
                this.guardadoAutomatico = true
                this.intervalos = intervalos
                this.unidadTiempo = unidadTiempo
            }
        }

        /**
         * Establecemos la ruta en la que se guardará
         * el {[RepositorioInmueble]}
         *
         * @param ruta String: Ruta a utilizar para el guardado
         */
        fun guardaLosDatosEn(ruta: String){

            // Comprobamos que la ruta sea válida
            if (rutaDeGuardadoValida(ruta)){

                // Eliminamos la "/" final
                if (ruta.endsWith("/")){
                    rutaGuardadoArchivos = ruta.removeRange(ruta.length - 1, ruta.length)
                    return
                }

                rutaGuardadoArchivos = ruta
            }

            // Mostramos un mensaje de error
            else {
                Utils.debug(Constantes.DEBUG.DEBUG_SIMPLE,"La ruta proporcionada para el almacenamiento de los inmuebles no es válida. " +
                        "Se usará $rutaGuardadoArchivos", Color.RED)
            }
        }

        /**
         * Establecemos el nombre que tendrá el archivo
         *
         * @param nombreArchivo: Nombre que se utilizará para nombrar al archivo
         */
        fun archivoConNombre(nombreArchivo: String){

            // Comprobamos que el nombre del archivo sea válido
            if (nombreArchivoValido(nombreArchivo)){
                this.nombreArchivo = nombreArchivo
            }

            // Mostramos un mensaje de error
            else {
                Utils.debug(Constantes.DEBUG.DEBUG_SIMPLE, "El nombre del archivo en el que se guardarán los inmuebles no es válido", Color.RED)
            }
        }

        /**
         * Establecemos la extensión que tendrá el archivo
         *
         * @param extension: Extensión que tendrá el archivo
         */
        fun archivoConExtension(extension: Constantes.EXTENSIONES_ARCHIVOS){
            this.extensionArchivo = extension
        }

        /**
         * Comprobamos si los intervalos que se setearán para el guardado automático son
         * válidos
         *
         * @param intervalos: Cantidada de tiempo entre ticks
         * @param unidadTiempo: Unidad de tiempo que se utilizará para emitir los ticks
         *
         * @return Boolean: Si los intervalos a setear son válidos
         */
        private fun intervalosGuardadoAutValidos(intervalos: Long, unidadTiempo: TimeUnit): Boolean{
            if ((intervalos < 3000000000 && unidadTiempo == TimeUnit.NANOSECONDS) || (intervalos < 3000 && unidadTiempo == TimeUnit.MILLISECONDS) ){
                return false
            }

            return true
        }

        /**
         * Comprobamos que la ruta en la que se guardará el archivo es válida
         *
         * @param rutaGuardadoArchivos: Ruta en la que se guardarán los archivos
         *
         * @return Boolean: Si la ruta es válida
         */
        private fun rutaDeGuardadoValida(ruta: String): Boolean{
            val dir = File(ruta)
            return dir.exists() && dir.isDirectory
        }

        /**
         * Comprobamos si el nombre del archivo es válido
         *
         * @param nombreArchivo: Nombre que se usará para el archivo
         *
         * @return Boolean: Si el nombre del archivo es válido
         */
        private fun nombreArchivoValido(nombreArchivo: String): Boolean {

            val patron = Regex("^\\w+(?:[-](?:\\w|[_])+)*\$")

            return nombreArchivo.matches(patron)
        }


        fun getGuardadoAutomatico(): Boolean {
            return this.guardadoAutomatico
        }

        fun getIntervalos(): Long {
            return this.intervalos
        }

        fun getUnidadTiempo(): TimeUnit{
            return this.unidadTiempo
        }

        fun getRutaGuardadoArchivos(): String?{
            return this.rutaGuardadoArchivos
        }

        fun getNombreArchivo(): String?{
            return this.nombreArchivo
        }

        fun getExtensionArchivo(): Constantes.EXTENSIONES_ARCHIVOS{
            return this.extensionArchivo
        }

        /**
         * Guardamos el nombre del archivo y la extensión
         *
         * @param nombreArchivo: Nombre que tendrá el archivo
         * @param extension: Extensión con la que se guardará el archivo
         */
        /*fun conNombreYExtension(nombreArchivo: String, extension: Constantes.EXTENSIONES_ARCHIVOS){

            if (nombreArchivo.matches(Regex("^\\w+(?:[-](?:\\w|[_])+)*\$"))){
                this.nombreArchivo = nombreArchivo
            }

            this.extensionArchivo = extension
        }*/
    }



    init {

        // Guardamos las propiedades que utilizaremos
        setPropiedades(propiedades)

        // Guardamos la clase de objeto que se almacenará en el dataframe
        setTipoActual(clazz)

        // Comprobamos que el archivo en el que se guardarán los datos exista
        val tempNombreArchivo = this.propiedades.getNombreArchivo() ?: obtenerNombreArchivoDefecto()
        val archivoCompleto = tempNombreArchivo + determinarExtension()
        nombreArchivo = archivoCompleto
        val archivo = File(propiedades.getRutaGuardadoArchivos() + "/$archivoCompleto")
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
    private fun crearDataFrame(listaInmuebles: List<T>?){


        dataframe = Table.create()
        val inmueble = tipoActual.newInstance()
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
        setNomCols(inmueble.obtenerNombreAtributos())
    }

    /**
     * Obtenemos el valor del {[atributo]} solicitado para
     * el {[inmueble]} actual
     *
     * @param atributo: Atributo a obtener del {[inmueble]}
     * @param inmueble: Objeto en el que buscaremos el valor del atributo solicitado
     *
     * @return String: Valor del atributo
     */
    private fun obtenerValorAtributo(atributo: String, inmueble: T): String {

        val field = inmueble.javaClass.getDeclaredField(atributo)
        field.isAccessible = true
        return field.get(inmueble).toString()
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

                    // Recorremos cada atributo del inmueble
                    nomCols.forEach { atributo ->

                        val valor = obtenerValorAtributo(atributo,inmueble)
                        dataframe.column(atributo).appendCell(valor)

                    }
                }
            }
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

            // Creamos un nuevo hilo
            Thread {
                runBlocking {

                    // Ejecutamos el proceso de escritura en una corutina dedicada
                    async(Dispatchers.IO){

                        // Añadimos los datos al archivo
                        val bufferedWriter = BufferedWriter(FileWriter(File(propiedades.getRutaGuardadoArchivos() + "/$nombreArchivo"),true))

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

                    }.await() // Esperamos a que se ejecute el código
                }
            }.start() // Ejecutamos el código que está dentro del hilo
        }
    }

    /**
     * Activamos el guardado automático y comenzamos a emitir ticks para el guardado
     * de los datos
     */
    private fun establecerGuardadoAutomatico(){

        // Comprobamos que el guardado automático esté establecido
        if (this.propiedades.getGuardadoAutomatico()){

            Observable.interval(this.propiedades.getIntervalos(), this.propiedades.getUnidadTiempo()).subscribe(object :
                Observer<Any> {
                override fun onComplete() {
                    guardandoAutomaticamente = false            // Vamos a parar de guardar automáticamente
                }

                override fun onSubscribe(d: Disposable) {
                    guardandoAutomaticamente = true             // Hemos comenzado a recibir los ticks
                }

                override fun onNext(t: Any) {
                    guardar()                                   // Guardamos los datos que halla hasta el momento
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
            propiedades.getExtensionArchivo() == Constantes.EXTENSIONES_ARCHIVOS.CSV -> { extension = ".csv" }
        }

        return extension
    }


    private fun setNomCols(nomCols: List<String>){
        this.nomCols = nomCols
    }

    private fun setTipoActual(tipo: Class<T>){
        this.tipoActual = tipo
    }

    private fun setPropiedades(propiedades: Propiedades){
        this.propiedades = propiedades
    }
}