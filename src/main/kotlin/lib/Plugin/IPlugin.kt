package lib.Plugin

import io.reactivex.subjects.PublishSubject

interface IPlugin {

    /**
     * Este método será al que llamará la plataforma para comenzar
     * la ejecución del plugin
     */
    fun onIniciar()

    /**
     * Este método lo usará la plataforma para indicar que va
     * a parar la ejecución del plugin
     *
     * @return PublishSubject<Nothing>: Se usará para indicar a la plataforma que hemos parado la ejecución existosamente
     */
    fun onParar(): PublishSubject<Nothing>


}