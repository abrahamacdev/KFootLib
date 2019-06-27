package lib.Common.Controlador.BufferedWriter

import KFoot.Constantes
import KFoot.Utils
import com.andreapivetta.kolor.Color
import io.reactivex.Completable
import kotlinx.coroutines.*
import lib.Common.Modelo.FuenteDatos
import java.io.*
import java.nio.charset.StandardCharsets
import kotlin.coroutines.*

class BufferedWriterCSV: IBufferedWriter {

    // Ruta del archivo en el que se guardará la información
    private var archivo: File? = null

    // Si es escribirán las cabeceras en el archivo o no
    private var escribirCabeceras: Boolean = false

    // FuenteDatos de datos
    private var fuenteDatos: FuenteDatos? = null

    // Writer del archivo
    private var bufferedWriter: BufferedWriter? = null

    // Separador del archivo CSV
    private var separador = ","

    // Último contexto utilizado
    @Volatile
    private var ultContexto: CoroutineContext? = null

    // Contexto de la coroutina
    var job: Job? = null
    var scope: CoroutineScope? = null
    var completableDeferred: CompletableDeferred<Unit>? = null

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

    class Builder(){

        private var rutaArchivo: File? = null
        private var escribirCabeceras: Boolean = false
        private var fuenteDatos: FuenteDatos? = null
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
                    KFoot.Utils.debug(Constantes.DEBUG.DEBUG_SIMPLE,"La ruta proporcionada para el guardado de los datos no es válida", Color.RED)
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
         * Construimos el [BufferedWriterCSV] con el que
         * se guardarán los datos en el archivo csv
         *
         * @return BufferedWriterCSV: Objeto que se utilizará para guardar la información
         */
        fun build(): BufferedWriterCSV {
            return BufferedWriterCSV(this.fuenteDatos, this.rutaArchivo, this.escribirCabeceras, this.separador)
        }
    }

    private constructor(fuenteDatos: FuenteDatos?, rutaArchivo: File? = null, escribirCabeceras: Boolean = false, separador: String){
        this.archivo = rutaArchivo
        this.escribirCabeceras = escribirCabeceras
        this.separador = separador
        this.fuenteDatos = fuenteDatos
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

            // Abrimos el writer
            bufferedWriter = BufferedWriter(OutputStreamWriter(FileOutputStream(archivo,true),StandardCharsets.UTF_8))

            // Escribimos la información en el archivos
            guardar(coroutineContext)
        }
    }

    /**
     * Guardamos los datos almacenados en la [fuenteDatos]
     * en el archivo con ruta [archivo] de forma asíncrona
     *
     * @return Deffered<Unit>: Futuro del guardado
     */
    fun guardarAsync(): CompletableDeferred<Unit>? {

        if (superaComprobacionesPrevias()){

            // Completable que avisará de la finalización del guardado
            completableDeferred = CompletableDeferred()

            // Creamos el alcance de la coroutina
            job = Job()
            scope = CoroutineScope(Dispatchers.IO + job!!)

            // Comenzamos el guardado de forma asíncrona
            scope!!.launch {
                guardar(scope!!.coroutineContext)
            }

            return completableDeferred
        }
        return null
    }

    /**
     * Guardamos los datos almacenados en la [fuenteDatos]
     * en el archivo con ruta [archivo]
     *
     * @param coroutineContext: Contexto en el que se está ejecutando el guardado
     */
    private suspend fun guardar(coroutineContext: CoroutineContext?) {

        // Cambiamos el estado del writer
        guardando = true

        // Abrimos el writer
        bufferedWriter = BufferedWriter(OutputStreamWriter(FileOutputStream(archivo,true),StandardCharsets.UTF_8))

        // Guardamos el nuevo contexto
        if (coroutineContext != null){
            ultContexto = coroutineContext
        }

        // Iniciamos el proceso de escritura bajo el último contexto
        if (ultContexto != null){

            // Escribimos en el archivo
            withContext(ultContexto!!){
                escribir()
            }
        }
    }


    var guardados = 0
    var mostrarCada = 50000
    /**
     * Escribimos toda la informacion de la [fuenteDatos] en
     * el archivo CSV con ruta [archivo]
     */
    private fun escribir(){

        // Comprobamos que halla una fuente de datos y un buffer abierto
        if (fuenteDatos != null && bufferedWriter != null){

            // Comprobamos que el guardado no se halla cancelado|pausado y halla filas por escribir
            while (!cancelado && !pausado && fuenteDatos!!.hayMasFilas()){

                // TODO
                guardados++
                if (guardados % mostrarCada == 0){
                    Utils.debug(Constantes.DEBUG.DEBUG_SIMPLE,"Guardando (${Thread.currentThread().name}) (Guardado nº$guardados)")
                }

                // Escribimos las cabeceras en el archivo
                if (escribirCabeceras && !cabeceraEscrita){

                    var cabecera = ""
                    for (i in 0 until fuenteDatos!!.getCabeceras().size - 1){
                        cabecera += fuenteDatos!!.getCabeceras().get(i) + separador
                    }
                    cabecera += fuenteDatos!!.getCabeceras().get(fuenteDatos!!.getCabeceras().size - 1) + "\n"
                    bufferedWriter!!.write(cabecera)

                    // Establecemos que ya hemos escrito la cabecera
                    cabeceraEscrita = true
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
            }

            // Terminamos el guardado de los datos
            if (cancelado || !fuenteDatos!!.hayMasFilas()){
                guardadoTerminado()
            }

            // TODO
            if (pausado){
                Utils.debug(Constantes.DEBUG.DEBUG_SIMPLE,"Ups, parece que han pausado el guardado")
            }
        }
    }

    /**
     * Realizamos una serie de comprobaciones antes de comenzar
     * a guardar la informacion en el archivo CSV
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



    override fun pausarGuardado() {
        pausado = true
    }

    override fun reanudarGuardado() {

        // TODO
        Utils.debug(Constantes.DEBUG.DEBUG_SIMPLE,"Retomamos el guardado")

        pausado = false

        // Guardado asíncrono
        if (scope != null){

            job = Job()
            scope = CoroutineScope(job!! + Dispatchers.IO)

            scope!!.launch {
                guardar(coroutineContext)
            }
        }

        // Guardado síncrono
        else {
            runBlocking {
                guardar(coroutineContext)
            }
        }
    }

    override fun cancelarGuardado() {
        cancelado = true
    }

    /**
     * Serie de tareas que se realizarán una vez que
     * se halla terminado el guardado o se halla cancelado
     */
    private fun guardadoTerminado(){

        // Permitimos que se vuelva a ejecutar el guardado
        guardando = false

        // Cerramos el buffer
        if (bufferedWriter != null){

            bufferedWriter!!.close()
            bufferedWriter = null
        }

        // Comprobamos si el guardado estaba siendo asíncrono
        if(scope != null){

            println(job!!.isCompleted)

            // Terminamos la ejecución de la coroutina
            completableDeferred!!.complete(Unit)

            // Cancelamos la ejecución de la coroutina
            job!!.cancel()

            println(job!!.isCompleted)

            // Eliminamos la coroutina
            job = null
            scope = null
        }
    }
}