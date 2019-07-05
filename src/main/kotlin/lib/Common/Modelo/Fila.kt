package lib.Common.Modelo

import tech.tablesaw.api.*
import tech.tablesaw.columns.Column
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

class Fila<T> {

    private val dateColumnMap = TreeMap<String, DateColumn>(String.CASE_INSENSITIVE_ORDER)
    private val doubleColumnMap = TreeMap<String, DoubleColumn>(String.CASE_INSENSITIVE_ORDER)
    private val intColumnMap = TreeMap<String, IntColumn>(String.CASE_INSENSITIVE_ORDER)
    private val longColumnMap = TreeMap<String, LongColumn>(String.CASE_INSENSITIVE_ORDER)
    private val shortColumnMap = TreeMap<String, ShortColumn>(String.CASE_INSENSITIVE_ORDER)
    private val floatColumnMap = TreeMap<String, FloatColumn>(String.CASE_INSENSITIVE_ORDER)
    private val stringColumnMap = TreeMap<String, Column<String>>(String.CASE_INSENSITIVE_ORDER)
    private val booleanColumnMap = TreeMap<String, BooleanColumn>(String.CASE_INSENSITIVE_ORDER)
    private val dateTimeColumnMap = TreeMap<String, DateTimeColumn>(String.CASE_INSENSITIVE_ORDER)
    private val timeColumnMap = TreeMap<String, TimeColumn>(String.CASE_INSENSITIVE_ORDER)
    private val columnMap = TreeMap<String, Column<*>>(String.CASE_INSENSITIVE_ORDER)

    private var columnNames: LinkedList<String> = LinkedList()
    private var numColumnas = 0
    private var table: Table? = null
    private lateinit var tipo: Class<*>

    companion object {
        inline fun <reified T> crear(table: Table): Fila<T>{
            return Fila(T::class.java, table)
        }
    }

    constructor(clazz: Class<*>, table: Table){
        this.tipo = clazz
        this.table = table
        this.numColumnas = table.columnCount()
        table.columnNames().forEach { columnNames.add(it) }
        for (column in table.columns()) {

            // Comprobamos que la columna no tenga datos o como mucho uno
            if(column.size() == 0 || column.size() == 1){
                if (column is DoubleColumn) {
                    doubleColumnMap.set(column.name(), column)
                }
                if (column is IntColumn) {
                    intColumnMap.set(column.name(), column)
                }
                if (column is ShortColumn) {
                    shortColumnMap.set(column.name(), column)
                }
                if (column is LongColumn) {
                    longColumnMap.set(column.name(), column)
                }
                if (column is FloatColumn) {
                    floatColumnMap.set(column.name(), column)
                }
                if (column is BooleanColumn) {
                    booleanColumnMap.set(column.name(), column)
                }
                if (column is StringColumn) {
                    stringColumnMap.set(column.name(), column)
                }
                if (column is TextColumn) {
                    stringColumnMap.set(column.name(), column)
                }
                if (column is DateColumn) {
                    dateColumnMap.set(column.name(), column)

                } else if (column is DateTimeColumn) {
                    dateTimeColumnMap.set(column.name(), column)

                } else if (column is TimeColumn) {
                    timeColumnMap.set(column.name(), column)
                }
                columnMap.set(column.name(), column)
            }
        }
    }



    fun columnCount(): Int {
        return numColumnas
    }

    /**
     * Returns a list containing the names of each column in the row
     */
    fun columnNames(): List<String> {
        return columnNames
    }

    fun getBoolean(columnIndex: Int): Boolean? {
        return getBoolean(columnNames[columnIndex])
    }

    fun getBoolean(columnName: String): Boolean? {
        return booleanColumnMap[columnName]!!.get(0)
    }

    fun getDate(columnName: String): LocalDate {
        return dateColumnMap[columnName]!!.get(0)
    }

    fun getDate(columnIndex: Int): LocalDate {
        return dateColumnMap[columnNames[columnIndex]]!!.get(0)
    }

    fun getDateTime(columnIndex: Int): LocalDateTime {
        return getDateTime(columnNames[columnIndex])
    }

    fun getDateTime(columnName: String): LocalDateTime {
        return (columnMap[columnName] as DateTimeColumn).get(0)
    }

    fun getDouble(columnIndex: Int): Double {
        return getDouble(columnNames[columnIndex])
    }

    fun getDouble(columnName: String): Double {
        return doubleColumnMap[columnName]!!.getDouble(0)
    }

    fun getFloat(columnIndex: Int): Float {
        return getFloat(columnNames[columnIndex])
    }

    fun getFloat(columnName: String): Float {
        return floatColumnMap[columnName]!!.getFloat(0)
    }

    fun getInt(columnIndex: Int): Int {
        return getInt(columnNames[columnIndex])
    }

    fun getInt(columnName: String): Int {
        return intColumnMap[columnName]!!.getInt(0)
    }

    fun getLong(columnIndex: Int): Long {
        return getLong(columnNames[columnIndex])
    }

    fun getLong(columnName: String): Long {
        return longColumnMap[columnName]!!.getLong(0)
    }

    fun getObject(columnName: String): Any {
        return columnMap[columnName]!!.get(0)
    }

    fun getObject(columnIndex: Int): Any {
        return columnMap[columnNames[columnIndex]]!!.get(0)
    }

    fun getPackedDate(columnName: String): Int {
        return dateColumnMap[columnName]!!.getIntInternal(0)
    }

    fun getPackedDate(columnIndex: Int): Int {
        return dateColumnMap[columnNames[columnIndex]]!!.getIntInternal(0)
    }

    fun getPackedDateTime(columnName: String): Long {
        return dateTimeColumnMap[columnName]!!.getLongInternal(0)
    }

    fun getPackedDateTime(columnIndex: Int): Long {
        return dateTimeColumnMap[columnNames[columnIndex]]!!.getLongInternal(0)
    }

    fun getPackedTime(columnName: String): Int {
        return timeColumnMap[columnName]!!.getIntInternal(0)
    }

    fun getPackedTime(columnIndex: Int): Int {
        return timeColumnMap[columnNames[columnIndex]]!!.getIntInternal(0)
    }

    fun getShort(columnIndex: Int): Short {
        return getShort(columnNames[columnIndex])
    }

    fun getString(columnIndex: Int): String {
        return getString(columnNames[columnIndex])
    }

    fun getShort(columnName: String): Short {
        return shortColumnMap[columnName]!!.getShort(0)
    }

    fun getText(columnName: String): String {
        return stringColumnMap[columnName]!!.get(0)
    }

    fun getText(columnIndex: Int): String {
        return getString(columnNames[columnIndex])
    }

    fun getTime(columnName: String): LocalTime {
        return timeColumnMap[columnName]!!.get(0)
    }

    fun getTime(columnIndex: Int): LocalTime {
        return timeColumnMap[columnNames[columnIndex]]!!.get(0)
    }

    fun getString(columnName: String): String {
        return stringColumnMap[columnName]!!.get(0)
    }

    fun getTipo(): Class<*>{
        return tipo
    }

    override fun toString(): String {

        if (this.table!!.rowCount() == 0 || this.table!!.rowCount() == 1){
            return this.table!!.toString()
        }

        else if (this.table!!.rowCount() > 1){
            var tempRow: Row? = null

            this.table!!.forEach {
                tempRow = it
                return@forEach
            }

            val tempDf = Table.create().addRow(tempRow)
            return tempDf.toString()
        }

        return ""
    }

    fun setBoolean(columnIndex: Int, value: Boolean) {
        setBoolean(columnNames[columnIndex], value)
    }

    fun setBoolean(columnName: String, value: Boolean) {
        booleanColumnMap[columnName]!!.set(0, value)
    }

    fun setDate(columnIndex: Int, value: LocalDate) {
        setDate(columnNames[columnIndex], value)
    }

    fun setDate(columnName: String, value: LocalDate) {
        dateColumnMap[columnName]!!.set(0, value)
    }

    fun setDateTime(columnIndex: Int, value: LocalDateTime) {
        setDateTime(columnNames[columnIndex], value)
    }

    fun setDateTime(columnName: String, value: LocalDateTime) {
        dateTimeColumnMap[columnName]!!.set(0, value)
    }

    fun setDouble(columnIndex: Int, value: Double) {
        setDouble(columnNames[columnIndex], value)
    }

    fun setDouble(columnName: String, value: Double) {
        doubleColumnMap[columnName]!!.set(0, value)
    }

    fun setFloat(columnIndex: Int, value: Float) {
        setFloat(columnNames[columnIndex], value)
    }

    fun setFloat(columnName: String, value: Float) {
        floatColumnMap[columnName]!!.set(0, value)
    }

    fun setInt(columnIndex: Int, value: Int) {
        setInt(columnNames[columnIndex], value)
    }

    fun setInt(columnName: String, value: Int) {
        intColumnMap[columnName]!!.set(0, value)
    }

    fun setLong(columnIndex: Int, value: Long) {
        setLong(columnNames[columnIndex], value)
    }

    fun setLong(columnName: String, value: Long) {
        longColumnMap[columnName]!!.set(0, value)
    }

    fun setShort(columnIndex: Int, value: Short) {
        setShort(columnNames[columnIndex], value)
    }

    fun setShort(columnName: String, value: Short) {
        shortColumnMap[columnName]!!.set(0, value)
    }

    fun setString(columnIndex: Int, value: String) {
        setString(columnNames[columnIndex], value)
    }

    fun setString(columnName: String, value: String) {
        stringColumnMap[columnName]!!.set(0, value)
    }

    fun setText(columnIndex: Int, value: String) {
        setString(columnNames[columnIndex], value)
    }

    fun setText(columnName: String, value: String) {
        stringColumnMap[columnName]!!.set(0, value)
    }

    fun setTime(columnIndex: Int, value: LocalTime) {
        setTime(columnNames[columnIndex], value)
    }

    fun setTime(columnName: String, value: LocalTime) {
        timeColumnMap[columnName]!!.set(0, value)
    }
}