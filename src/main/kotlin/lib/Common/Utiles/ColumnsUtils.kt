package lib.Common.Utiles

import KFoot.IMPORTANCIA
import KFoot.Logger
import tech.tablesaw.api.*
import tech.tablesaw.columns.Column

object ColumnsUtils {

    val TIPOS_PERMITIDOS = arrayOf<Class<*>>(Boolean::class.java,Int::class.java, Long::class.java, Float::class.java,Double::class.java,String::class.java)

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
            clase.name.equals("long") -> return LongColumn.create(nombreCol)
            clase.name.equals("float") -> return FloatColumn.create(nombreCol)
            clase.name.equals("double") -> return DoubleColumn.create(nombreCol)
            clase.name.equals("java.lang.String") -> return StringColumn.create(nombreCol)
        }

        // Tipo de dato no soportado
        Logger.getLogger().debug(KFoot.DEBUG.DEBUG_TEST, "Tipo de dato aún no soportado. Por favor, compruebe los tipos permitidos", IMPORTANCIA.ALTA)

        return null
    }

    /**
     * Obtenemos la clase de un [ColumnType] a partir
     * del objeto recibido por parámetros
     *
     *  @param columnType: Tipo de la columna
     *
     *  @return Class<*>: Clase del tipo correspondiente
     */
    fun castearColTypeAClase(columnType: ColumnType): Class<*>?{

        when {

            columnType == ColumnType.BOOLEAN -> {
                return Boolean::class.java
            }

            columnType == ColumnType.INTEGER -> {
                return Int::class.java
            }

            columnType == ColumnType.LONG -> {
                return Long::class.java
            }

            columnType == ColumnType.FLOAT -> {
                return Float::class.java
            }

            columnType == ColumnType.DOUBLE-> {
                return Double::class.java
            }

            columnType == ColumnType.STRING -> {
                return String::class.java
            }

            columnType == ColumnType.TEXT -> {
                return String::class.java
            }

        }

        return null
    }

    /**
     * Retornamos la lista de tipos permitidos
     * para su conversión a una columna
     *
     * @return Array<String>: Array con los tipos permitidos
     */
    fun obtenerTiposPermitirdos(): Array<String>{
        return Array<String>(TIPOS_PERMITIDOS.size, {it: Int -> TIPOS_PERMITIDOS.get(it).canonicalName})
    }

    /**
     * Comprobamos si el [tipo] pasado por parámetros
     * coincide con los tipos de datos permitidos para
     * la creación de columnas
     *
     * @param tipo: Tipo de dato
     *
     * @return Boolean: Si el tipo de dato es válido
     */
    fun tipoDatoValido(tipo: Class<*>): Boolean {
        return tipo in ColumnsUtils.TIPOS_PERMITIDOS
    }

    /**
     * Comprobamos si el [tipo] pasado por parámetros
     * coincide con los tipos de datos permitidos para
     * la creación de columnas
     *
     * @param tipo: Tipo de dato
     *
     * @return Boolean: Si el tipo de dato es válido
     */
    fun tipoDatoValido(tipo: String): Boolean {
        return tipo in ColumnsUtils.obtenerTiposPermitirdos()
    }
}