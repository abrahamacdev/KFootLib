package lib.Plugin.Utiles

import KFoot.Constantes
import KFoot.Utils.debug
import com.andreapivetta.kolor.Color
import tech.tablesaw.api.*
import tech.tablesaw.columns.Column

object Utils {

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
}