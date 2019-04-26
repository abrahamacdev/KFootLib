package Utiles

import com.andreapivetta.kolor.Color
import com.andreapivetta.kolor.Kolor
import tech.tablesaw.api.*
import tech.tablesaw.columns.Column
import java.io.File

object Utils {

    /**
     * Comprobamos que sistema operativo tiene el cliente
     *
     * @return Constantes.SO: Sistema operativo del usuario
     */
    fun determinarSistemaOperativo(): Constantes.SO{
        val os = System.getProperty("os.name").toLowerCase()

        when {
            os.indexOf("win") >= 0-> {
                return Constantes.SO.WINDOWS
            }
            os.indexOf("nux") >= 0-> {
                return Constantes.SO.UBUNTU
            }
        }
        return Constantes.SO.DESCONOCIDO
    }

    /**
     * Logueamos los mensajes pasados por parámetro según el "nivel de debug" necesitado
     * y el establecido para la sesión actual.
     *
     * @param Constantes.DEBUG nivelRequerido: Nivel de debug requerido para mostrar el mensaje
     * @param String mensaje: Texto a mostrar
     * @param Color color: Color con el que se mostrará el mensaje
     */
    fun debug(nivelRequerido: Constantes.DEBUG, mensaje: String, color: Color = Color.BLACK){
        // Comprobamos que queremos loguear
        if (Constantes.DEBUG.DEBUG_LEVEL.value != Constantes.DEBUG.DEBUG_NONE.value){

            // El mensaje es de un test y estamos en el nivel de "Test"
            if (nivelRequerido.value == Constantes.DEBUG.DEBUG_LEVEL.value && Constantes.DEBUG.DEBUG_LEVEL.value == Constantes.DEBUG.DEBUG_TEST.value){
                println(Kolor.foreground(mensaje,color))
            }

            // Ej: Si el nivel actual es 'Avanzado', todos los de nivel 'Simple' también se mostrarán
            else if (nivelRequerido.value <= Constantes.DEBUG.DEBUG_LEVEL.value && nivelRequerido.value != Constantes.DEBUG.DEBUG_NONE.value){
                println(Kolor.foreground(mensaje,color))
            }
        }
    }

    /**
     * Creamos una columna a partir del {[nombreCol]} y del tipo de la {[clase]}
     * Actualmente los tipos de objeto soportados son: String, int, float, double, boolean y long
     *
     * @param String nombreCol: Nombre de la columna a crear
     * @param Class<*> clase: Clase del tipo de columna a crear
     * @return Column<*>?: Columna del tipo de la {[clase]]} pasada por parámetro
     */
    fun castearAColumna(nombreCol: String, clase: Class<*>): Column<*>?{

        when {
            clase.name.equals("boolean") -> return BooleanColumn.create(nombreCol)
            clase.name.equals("int") -> return IntColumn.create(nombreCol)
            clase.name.equals("float") -> return FloatColumn.create(nombreCol)
            clase.name.equals("double") -> return DoubleColumn.create(nombreCol)
            clase.name.equals("java.lang.String") -> return StringColumn.create(nombreCol)
        }

        // Tipo de dato no soportado
        debug(Constantes.DEBUG.DEBUG_SIMPLE, "Tipo de dato aún no soportado. Por favor, compruebe los tipos permitidos", Color.RED)

        return null
    }

    /**
     * Retornamos la ruta del directorio 'Documentos'
     *
     * @return String?:
     */
    fun obtenerDirDocumentos(): String?{

        val idioma = System.getProperty("user.language")
        val so = Utils.determinarSistemaOperativo()

        var ruta: String? = null

        when{

            // Ubuntu
            so == Constantes.SO.UBUNTU -> {
                when {

                    // Español
                    idioma.equals("es") -> { ruta = Constantes.DIRECTORIO_PERSONAL + "/Documentos"}

                    // Por defecto inglés
                    else -> ruta = Constantes.DIRECTORIO_PERSONAL + "/Documents"
                }
            }
        }

        // Comprobamos si la ruta es un directorio válido
        val dirDocumentos = File(ruta)
        if (!dirDocumentos.exists() || !dirDocumentos.isDirectory){
            ruta = null
        }


        return ruta
    }
}