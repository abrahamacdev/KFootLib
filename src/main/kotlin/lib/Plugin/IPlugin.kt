package lib.Plugin

import java.lang.Exception

interface IPlugin {

    /**
     * Este método permitirá establecer la sincronización entre
     * la plataforma y los plugins, de esta forma podremos alterar
     * la ejecución del plugin y este avisar de algunos estados
     */
    fun sincronizar(onAvisosPluginListener: onAvisosPluginListener): onControlPluginListener


    /**
     * Interfaz que servirá para recibir distintos datos
     * de control de los plugins
     */
    interface onControlPluginListener {

        /**
         * Este método será al que llamará la plataforma para comenzar
         * la ejecución del plugin
         *
         * @param onResultadoAccionListener: Callback por el que transmitiremos el inicio del plugin
         * ,o por contra, el error que no nos ha permitido realizar la ejecución
         */
        fun onIniciar(onResultadoAccionListener: onResultadoAccionListener? = null)

        /**
         * Servirá para comunicar al plugin que debe de pausar
         * su ejecución
         *
         * @param onResultadoAccionListener: Callback por el que transmitiremos el correcto pausado
         * del plugin, o por contra, el error que no nos ha permitido realizar el pausado
         */
        fun onPausar(onResultadoAccionListener: onResultadoAccionListener? = null)

        /**
         * Servirá para comunicar al plugin que puede retomar
         * su ejecución después de haber sido pausada
         *
         * @param onResultadoAccionListener: Callback por el que transmitiremos el correcto reanudado
         * del plugin, o por contra, el error que no nos ha permitido reanudar la ejecución
         */
        fun onReanudar(onResultadoAccionListener: onResultadoAccionListener? = null)

        /**
         * Servirá para comunicar al plugin que debe de cancelar
         * su ejecución
         *
         * @param onResultadoAccionListener: Callback por el que transmitiremos la cancelación
         * del plugin, o por contra, el error que no nos ha permitido cancelar la ejecución
         */
        fun onCancelar(onResultadoAccionListener: onResultadoAccionListener? = null)
    }

    /**
     * Esta interfaz comprende las funciones que podrán ser
     * llamadas como resultado de intertar modificar la ejecucion
     * de un plugin
     */
    interface onResultadoAccionListener{

        /**
         * Esta función se llamará cada vez que se haya
         * podido llevar a cabo alguna acción referente
         * a la ejecución del plugin. Ej: parar, reanudar, cancelar... etc
         */
        fun onCompletado()

        /**
         * Esta función se llamará cada vez que haya ocurrido
         * un error que haya imposibilitado modificar la
         * ejecución del plugin: Ej: no hemos podido parar el plugin,
         * no se ha podido reanudar... etc
         */
        fun onError(e: Exception)

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
         *
         * @param error: Error que ha podido causar la finalizacion
         */
        fun onPluginTerminado(error: Exception? = null)
    }
}