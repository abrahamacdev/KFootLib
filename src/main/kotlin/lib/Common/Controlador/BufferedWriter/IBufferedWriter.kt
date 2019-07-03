package lib.Common.Controlador.BufferedWriter

interface IBufferedWriter {

    /**
     * Permite pausar el guardado de los datos hasta nuevo
     * aviso.
     */
    fun pausarGuardado()

    /**
     * Permite retomar el guardado de datos que se estaba
     * llevando cabo.
     */
    fun reanudarGuardado()

    /**
     * Cancelamos el guardado que se esté llevando a cabo.
     */
    fun cancelarGuardado()

    /**
     * Nos permite llevar a cabo un guardado síncrono de
     * los dataos
     */
    fun guardar()
}