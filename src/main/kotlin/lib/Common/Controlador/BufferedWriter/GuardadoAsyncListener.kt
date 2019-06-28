package lib.Common.Controlador.BufferedWriter

import kotlinx.coroutines.CompletableDeferred

interface GuardadoAsyncListener {

    interface onGuardadoAsyncListener {

        fun onGuardadoComenzado(completable: CompletableDeferred<Unit>)

        fun onGuardadoCompletado()

        fun onGuardadoError(error: Throwable)
    }
}
