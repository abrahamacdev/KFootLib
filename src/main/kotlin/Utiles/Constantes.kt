package Utiles

import javax.rmi.CORBA.Util

object Constantes {

    enum class SO{
        WINDOWS,
        UBUNTU,
        DESCONOCIDO
    }

    enum class EXTENSIONES_ARCHIVOS {
        CSV
    }

    val DIRECTORIO_PERSONAL = System.getProperty("user.home")

    // -- DEBUG --
    enum class DEBUG(val value: Int) {
        DEBUG_TEST(3),             // Nos permite seguir el flujo del programa al realizar tests
        DEBUG_AVANZADO(2),         // Imprimirá mucha más información
        DEBUG_SIMPLE(1),           // Imprimirá la información más básica
        DEBUG_NONE(0),             // No queremos debug
        DEBUG_LEVEL(DEBUG_TEST.value)    // Debug que queremos para la ejecución actual
    }
    // -----------------------------------------------

}
