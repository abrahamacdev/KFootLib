package lib.Common.Controlador.BufferedWriter

import KFoot.Constantes
import KFoot.IMPORTANCIA
import KFoot.Logger
import KFoot.Utils
import com.andreapivetta.kolor.Color
import io.reactivex.Completable
import kotlinx.coroutines.*
import lib.Common.Controlador.Item.Item
import lib.Common.Modelo.FuenteDatos
import java.io.*
import java.nio.charset.StandardCharsets
import kotlin.coroutines.*

class BufferedWriterCSV: IBufferedWriterAsync {

    // Ruta del archivo en el que se guardará la información
    private var archivo: File? = null

    // Si es escribirán las cabeceras en el archivo o no
    private var escribirCabeceras: Boolean = false

    // Lista de cabeceras a escribir en el archivo
    private var listaDeCabeceras: List<String>? = null

    // FuenteDatos de datos
    private var fuenteDatos: FuenteDatos? = null

    // Writer del archivo
    private var bufferedWriter: BufferedWriter? = null

    // Separador del archivo CSV
    private var separador = ","

    // Contexto de la coroutina
    var job: Job? = null
    var scope: CoroutineScope? = null
    var completableDeferred: CompletableDeferred<Unit>? = null

    // Interfaz asociada al guardado asíncrono
    var guardadoAsyncListener: GuardadoAsyncListener.onGuardadoAsyncListener? = null

    // Nos servirá para reaundar el guardado de los datos en caso de pausa
    @Volatile
    var continuacion: Continuation<Unit>? = null

    @Volatile
    private var guardando = false
    @Volatile
    private var pausado = false
    @Volatile
    private var cancelado = false

    /**
     * Nos servirá para saber si ya hemos escrito las cabeceras
     * @see [escribirCabeceras]
    */
    private var cabeceraEscrita = false

    private constructor(fuenteDatos: FuenteDatos?, rutaArchivo: File? = null, escribirCabeceras: Boolean = false, separador: String, listaCabeceras: List<String>? = null){
        this.archivo = rutaArchivo
        this.escribirCabeceras = escribirCabeceras
        this.separador = separador
        this.fuenteDatos = fuenteDatos
        this.listaDeCabeceras = listaCabeceras
    }



    class Builder(){

        private var rutaArchivo: File? = null
        private var escribirCabeceras: Boolean = false
        private var escribirSiNoExiste: Boolean = false
        private var fuenteDatos: FuenteDatos? = null
        private var listaCabeceras: List<String>? = null
        private var separador = ","

        /**
         * Servirá para escribir las cabeceras en el archivo
         *
         * @param escribir: Si se escribirán las cabeceras en el archivo o no
         */
        fun escribirCabeceras(escribir: Boolean = false): Builder {
            this.escribirCabeceras = escribir
            return this
        }

        /**
         * Establecemos la escritura de las cabeceras si el archivo
         * no existe aún
         */
        fun escribirCabecerasSiNoExisteArchivo(): Builder{
            escribirSiNoExiste = true
            return this
        }

        /**
         * Establecemos la ruta del archivo en el que
         * se guadarán los datos
         *
         * @param rutaArchivo: Ruta absoluta del archivo en el que se guardarán los datos
         */
        fun guardarEn(rutaArchivo: String): Builder {
            val f = File(rutaArchivo)

            // Es un archivo .csv
            if (f.absolutePath.endsWith(".csv")){

                // Comprobamos que la ruta del archivo existe
                var ruta = File(rutaArchivo.split(Regex("(?:\\/[\\w-_:](?:[\\w-_:])*\\.csv)\$"))[0])

                // Comprobamos que la ruta exista
                if (ruta.isDirectory && ruta.exists()){
                    this.rutaArchivo = File(rutaArchivo)
                }
                else {
                    Logger.getLogger().debug(KFoot.DEBUG.DEBUG_SIMPLE,"La ruta proporcionada para el guardado de los datos no es válida", IMPORTANCIA.ALTA)
                }
            }
            return this
        }

        /**
         * Establecemos el separador que se usará en el
         * archivo CSV
         *
         * @param separador: Caracter que se usará como separador
         */
        fun usarComoSeparador(separador: Char){
            this.separador = separador.toString()
        }

        /**
         * Objeto del que extraeremos los datos para
         * guardarlos en el archivo CSV
         *
         * @param fuenteDatos: Fuente de datos
         */
        fun obtenerDatosDe(fuenteDatos: FuenteDatos): Builder {
            this.fuenteDatos = fuenteDatos
            return this
        }

        /**
         * Lista con las cabeceras que se escribirán en el archivo
         */
        fun listaDeCabeceras(cabeceras: List<String>): Builder{
            if (cabeceras.size > 0){
                this.listaCabeceras = cabeceras
            }
            return this
        }

        /**
         * Construimos el [BufferedWriterCSV] con el que
         * se guardarán los datos en el archivo csv
         *
         * @return BufferedWriterCSV: Objeto que se utilizará para guardar la información
         */
        fun build(): BufferedWriterCSV? {

            if(rutaArchivo != null && fuenteDatos != null){

                // Comprobamos si existe el archivo para escribir las cabeceras o no
                if (escribirSiNoExiste){
                    if (!Utils.existeArchivo(this.rutaArchivo!!.canonicalPath)){
                        this.escribirCabeceras = true
                    }
                }

                return BufferedWriterCSV(this.fuenteDatos, this.rutaArchivo, this.escribirCabeceras, this.separador, this.listaCabeceras)
            }
            return null
        }
    }



    /**
     * Guardamos los datos almacenados en la [fuenteDatos]
     * en el archivo con ruta [archivo]
     */
    override fun guardar() = runBlocking {

        // Comprobamos si supera todas las comprobaciones previas al guardado
        if (superaComprobacionesPrevias()){

            // Cambiamos el estado del writer
            guardando = true

            // Escribimos la información en el archivos
            guardar(coroutineContext)
        }
    }

    /**
     * Guardamos los datos almacenados en la [fuenteDatos]
     * en el archivo con ruta [archivo] de forma asíncrona
     *
     * @param guardadoAsyncListener: Listener por el que comunicaremos el comienzo,finalización y errores del guardado
     */
    override fun guardarAsync(guardadoAsyncListener: GuardadoAsyncListener.onGuardadoAsyncListener?) {

        // Comprobamos si supera todas las comprobaciones previas al guardado
        if (superaComprobacionesPrevias()){

            // Cambiamos el estado del writer
            guardando = true

            // Completable que avisará de la finalización del guardado
            completableDeferred = CompletableDeferred()

            // Creamos el alcance de la coroutina
            job = Job()
            scope = CoroutineScope(Dispatchers.IO + job!!)

            // Comenzamos el guardado de forma asíncrona
            scope!!.launch {
                guardar(scope!!.coroutineContext)
            }

            // Comprobamos si se ha pasado una interfaz por parámetros
            if(guardadoAsyncListener != null){

                // Guardamos la referencia del listener
                this.guardadoAsyncListener = guardadoAsyncListener

                // Avisamos de que el guardado a comenzado y pasamos el deferred asociado
                guardadoAsyncListener.onGuardadoComenzado(completableDeferred!!)
            }
        }
    }

    /**
     * Guardamos los datos almacenados en la [fuenteDatos]
     * en el archivo con ruta [archivo] de forma asíncrona
     *
     * @param guardadoComenzado: Funcion que se llamará una vez comience el guardado
     * @param guardadoError: Funcion que se llamará cuando ocurra algún error
     * @param guardadoCompletado: Funcion que se llamará una cuando termine de realizarse el guardado
     */
    override fun guardarAsync(guardadoComenzado: (CompletableDeferred<Unit>) -> Unit,
                              guardadoError: (Throwable) -> Unit,
                              guardadoCompletado: () -> Unit){
        super.guardarAsync(guardadoComenzado, guardadoError, guardadoCompletado)
    }

    /**
     * Utilizamos el contexto pasado por parámetro para realizar
     * el guardado de la información
     *
     * @param coroutineContext: Contexto en el que se está ejecutando el guardado
     */
    private suspend fun guardar(coroutineContext: CoroutineContext?) {

        // Abrimos el writer
        bufferedWriter = BufferedWriter(OutputStreamWriter(FileOutputStream(archivo,true),StandardCharsets.UTF_8))

        // Escribimos en el archivo
        if (coroutineContext != null){
            escribir(coroutineContext)
        }
    }

    var guardados = 0
    var mostrarCada = 50000
    /**
     * Escribimos toda la informacion de la [fuenteDatos] en
     * el archivo CSV con ruta [archivo]
     *
     * @param contexto: Contexto bajo el que se ejecutará el guardado
     */
    private suspend fun escribir(contexto: CoroutineContext) = withContext(contexto){

        // Comprobamos que haya una fuente de datos y un buffer
        if (fuenteDatos != null && bufferedWriter != null){

            // Comprobamos que haya que escribir las cabeceras y estas no hayan sido ya escritas
            if (!cabeceraEscrita && escribirCabeceras && listaDeCabeceras != null){

                // Escribimos todas las cabeceras seguidas del separador
                for (i in 0 until listaDeCabeceras!!.size - 1){
                    bufferedWriter!!.write("${listaDeCabeceras!!.get(i)}$separador")
                }

                // A la última cabecera le agregamos un salto de línea
                bufferedWriter!!.write("${listaDeCabeceras!!.get(listaDeCabeceras!!.size - 1)}\n")
            }

            // Comprobamos que el guardado no se halla cancelado|pausado y haya filas por escribir
            while (!cancelado && fuenteDatos!!.hayMasFilas()){

                // TODO
                guardados++
                if (guardados % mostrarCada == 0){
                    Logger.getLogger().debug(KFoot.DEBUG.DEBUG_TEST,"Guardando (${Thread.currentThread().name}) (Guardado nº$guardados)")
                }

                // Escribimos el cuerpo del CSV
                var fila = fuenteDatos!!.siguienteFila()
                var cuerpo = ""
                for (i in 0 until fila!!.size - 1){
                    cuerpo += fila.get(i) + separador
                }
                cuerpo += fila.get(fila.size - 1) + "\n"
                bufferedWriter!!.write(cuerpo)

                // Hacemos efectivos los cambios
                bufferedWriter!!.flush()

                // Obtenemos la continuacion de la coroutina
                if(pausado){
                    suspendCoroutine<Unit> {
                        continuacion = it
                    }
                }
            }

            // Terminamos el guardado de los datos
            if (cancelado || !fuenteDatos!!.hayMasFilas()){
                guardadoTerminado()
            }
        }
    }



    override fun pausarGuardado() {
        pausado = true
    }

    override fun reanudarGuardado() {

        // Deshabilitamos el pausado
        pausado = false

        if (continuacion != null){
            continuacion!!.resume(Unit)
        }

        // Guardado asíncrono
        /*if (scope != null){

            scope!!.launch {
                guardar(coroutineContext)
            }
        }

        // Guardado síncrono
        else {
            runBlocking {
                guardar(coroutineContext)
            }
        }*/
    }

    override fun cancelarGuardado() {
        cancelado = true
    }

    override fun esperarHastaFinalizacion() {
        if(completableDeferred != null){
            runBlocking {
                completableDeferred!!.await()
            }
        }
    }

    /**
     * Realizamos una serie de comprobaciones antes de comenzar
     * a guardar la informacion en el archivo CSV
     *
     * @return Si se cumplen los requisitos previos al guardado
     */
    private fun superaComprobacionesPrevias(): Boolean {

        // Comprobamos que no se halla iniciado ya un guardado
        // o se halla cancelado el que se estaba realizando
        if (!guardando && !cancelado){

            // Comprobamos que haya una fuente de datos
            if (fuenteDatos != null){

                return true
            }
        }
        return false
    }

    /**
     * Serie de tareas que se realizarán una vez que
     * se haya terminado el guardado o se haya cancelado
     */
    private fun guardadoTerminado(){

        // Permitimos que se vuelva a ejecutar el guardado
        guardando = false

        // Cerramos el buffer
        if (bufferedWriter != null){

            bufferedWriter!!.close()
            bufferedWriter = null
        }

        // Eliminamos la referencia a la continuación
        // de la coroutina
        continuacion = null

        // Comprobamos si el guardado estaba siendo asíncrono
        if(scope != null){

            // Terminamos la ejecución de la coroutina
            completableDeferred!!.complete(Unit)

            // Cancelamos la ejecución de la coroutina
            job!!.cancel()

            // Eliminamos la coroutina y objetos asociados
            job = null
            scope = null
            completableDeferred = null

            // Emitimos la finalización del guardado a través
            // de la interfaz
            if (guardadoAsyncListener != null){
                guardadoAsyncListener!!.onGuardadoCompletado()
                guardadoAsyncListener = null
            }
        }
    }
}