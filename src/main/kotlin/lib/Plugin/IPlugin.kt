package lib.Plugin

import java.lang.Exception

interface IPlugin {

    /**
     * Este método permitirá tener la lógica del plugin
     * separada de la clase principal, la cuál permitirá
     */
    fun getControlPluginListener(): onControlPluginListener

    /**
     * Interfaz que servirá para recibir distintos datos
     * de control de los plugins
     */
    interface onControlPluginListener {

        /**
         * Este método será al que llamará la plataforma para comenzar
         * la ejecución del plugin
         */
        fun onIniciar(onIniciarPluginListener: onIniciarPluginListener? = null)

        interface onIniciarPluginListener {

            /**
             * Nos permitirá saber si el plugin ha comenzado a
             * ejecutarse correctamente
             */
            fun onPluginIniciado()

            /**
             * Avisará a la plataforma si el plugin no ha podido
             * comenzar a ejecutarse por algún error
             *
             * @param error: Error que no ha permitido al plugin ejecutarse
             */
            fun onPluginIniciandoError(error: Exception)
        }



        /**
         * Servirá para comunicar al plugin que debe de pausar
         * su ejecución
         */
        fun onPausar()

        /**
         * Servirá para comunicar al plugin que puede retomar
         * su ejecución después de haber sido pausada
         */
        fun onReanudar()

        /**
         * Servirá para comunicar al plugin que debe de cancelar
         * su ejecución
         */
        fun onCancelar()

        /**
         * Avisará a la plataforma de la finalización
         * de la ejecución del plugin
         */
        fun onPluginTerminado()
    }
}