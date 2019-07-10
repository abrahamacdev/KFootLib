package lib.Plugin

import java.lang.Exception

interface IPlugin {

    /**
     * Este método permitirá establecer la sincronización entre
     * la plataforma y los plugins, de esta forma podremos alterar
     * la ejecución del plugin y este avisar de algunos estados
     */
    fun sincronizar(onAvisosPluginListener: onAvisosPluginListener? = null): onControlPluginListener

    /**
     * Interfaz que servirá para recibir distintos datos
     * de control de los plugins
     */
    interface onControlPluginListener {

        /**
         * Este método será al que llamará la plataforma para comenzar
         * la ejecución del plugin
         *
         * @param onIniciarPluginListener: Callback por el que transmitiremos el inicio del plugin
         * ,o por contra, el error que no nos ha permitido realizar la ejecución
         */
        fun onIniciar(onIniciarPluginListener: onIniciarPluginListener? = null)

        /**
         * Servirá para comunicar al plugin que debe de pausar
         * su ejecución
         *
         * @param onPausarPluginListener: Callback por el que transmitiremos el correcto pausado
         * del plugin, o por contra, el error que no nos ha permitido realizar el pausado
         */
        fun onPausar(onPausarPluginListener: onPausarPluginListener? = null)

        /**
         * Servirá para comunicar al plugin que puede retomar
         * su ejecución después de haber sido pausada
         *
         * @param onReanudarPluginListener: Callback por el que transmitiremos el correcto reanudado
         * del plugin, o por contra, el error que no nos ha permitido reanudar la ejecución
         */
        fun onReanudar(onReanudarPluginListener: onReanudarPluginListener? = null)

        /**
         * Servirá para comunicar al plugin que debe de cancelar
         * su ejecución
         *
         * @param onCancelarPluginListener: Callback por el que transmitiremos la cancelación
         * del plugin, o por contra, el error que no nos ha permitido cancelar la ejecución
         */
        fun onCancelar(onCancelarPluginListener: onCancelarPluginListener? = null)
    }

    /**
     * Esta interfaz será implementada por la plataforma para
     * recibir algunos estados especiales del plugin como pueda
     * ser: la finalización de su ejecución... etc
     */
    interface onAvisosPluginListener {

        /**
         * Avisará a la plataforma de la finalización
         * de la ejecución del plugin
         */
        fun onPluginTerminado()
    }




    /**
     * Interfaz que permitirá a la plataforma conocer
     * si el plugin ha podido comenzar existosamente
     * su ejecución
     */
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
     * Interfaz que permitirá a la plataforma conocer
     * si el plugin ha podido pausarse existosamente
     * su ejecución
     */
    interface onPausarPluginListener {

        /**
         * Nos permitirá saber si el plugin ha pausado
         * su ejecución correctamente
         */
        fun onPluginPausado()

        /**
         * Avisará a la plataforma si el plugin no ha podido
         * pausarse por algún error
         *
         * @param error: Error que no ha permitido al plugin pausarse
         */
        fun onPluginPausadoError(error: Exception)
    }

    /**
     * Interfaz que permitirá a la plataforma conocer
     * si el plugin ha podido reanudarse existosamente
     * su ejecución
     */
    interface onReanudarPluginListener {

        /**
         * Nos permitirá saber si el plugin ha reanudado
         * su ejecución exitósamente
         */
        fun onPluginReanudado()

        /**
         * Avisará a la plataforma si el plugin no ha podido
         * reanudar su ejecución por algún error
         *
         * @param error: Error que no ha permitido al plugin reanudarse
         */
        fun onPluginReanudandoError(error: Exception)
    }

    /**
     * Interfaz que permitirá a la plataforma conocer
     * si el plugin ha podido reanudarse existosamente
     * su ejecución
     */
    interface onCancelarPluginListener {

        /**
         * Nos permitirá saber si el plugin ha cancelado
         * su ejecución correctamente
         */
        fun onPluginCancelado()

        /**
         * Avisará a la plataforma si el plugin no ha podido
         * cancelar su ejecución por algún error
         *
         * @param error: Error que no ha permitido al plugin cancelar su ejecución
         */
        fun onPluginCancelandoError(error: Exception)
    }
}