package lib.Common.Controlador.BufferedWriter

import kotlinx.coroutines.CompletableDeferred

interface IBufferedWriterAsync: IBufferedWriter {

    /**
     * Nos permite llevar a cabo un guardado asíncrono de
     * los datos
     *
     * @param guardadoAsyncListener: Listener por el que comunicaremos el comienzo y finalización del guardado asíncrono,
     *                               además de los posibles errores que surjan.
     */
    fun guardarAsync(guardadoAsyncListener: GuardadoAsyncListener.onGuardadoAsyncListener? = null)

    /**
     * Guardamos los datos almacenados en la [fuenteDatos]
     * en el archivo con ruta [archivo] de forma asíncrona
     * * Esta forma nos permite
     *
     * @param guardadoComenzado: Funcion que se llamará una vez comience el guardado
     * @param guardadoError: Funcion que se llamará cuando ocurra algún error
     * @param guardadoCompletado: Funcion que se llamará una cuando termine de realizarse el guardado
     */
    fun guardarAsync(guardadoComenzado: (CompletableDeferred<Unit>) -> Unit,
                     guardadoError: (Throwable) -> Unit,
                     guardadoCompletado: () -> Unit){

        val asyncListener = object : GuardadoAsyncListener.onGuardadoAsyncListener {
            override fun onGuardadoComenzado(completable: CompletableDeferred<Unit>) {
                guardadoComenzado(completable)
            }

            override fun onGuardadoCompletado() {
                guardadoCompletado()
            }

            override fun onGuardadoError(error: Throwable) {
                guardadoError(error)
            }
        }

        // Comenzamos un guardado asíncrono
        guardarAsync(asyncListener)

    }

    /**
     * Esperamos hasta que se acabe de guardar toda la
     * información
     */
    fun esperarHastaFinalizacion()

}