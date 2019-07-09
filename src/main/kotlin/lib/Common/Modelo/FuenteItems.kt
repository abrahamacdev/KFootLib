package lib.Common.Modelo

import lib.Common.Controlador.Item.Item
import lib.Common.Utiles.ColumnsUtils
import tech.tablesaw.api.Table
import tech.tablesaw.columns.Column
import kotlin.collections.ArrayList

class FuenteItems<T: Item> {

    // Guardamos la posición de los valores de cada fila
    // dentro del map de valores
    private lateinit var tabla: Table

    // Número de la última fila recorrida
    var ultTuplaRecorrida = 0

    // Tipo de dato que se almacena en la fuente
    var tipo: Class<*>? = null



    constructor(tipo: Class<T>, tabla: Table){
        this.tabla = tabla
        this.tipo = tipo
    }

    constructor(item: T){
        this.tipo = item::class.java
        crearTabla(item)
    }


    /**
     * Creamos la tabla a partir de un item
     *
     * @param item: Item que usaremos para crear la tabla
     */
    private fun crearTabla(item: T){

        // Obtenemos las propiedades del items y sus tipos
        val nombreTipo = item.obtenerNombreTipoAtributos()

        // Columnas que añadiremos a la tabla
        val columnas: ArrayList<Column<*>> = ArrayList()

        nombreTipo.forEach {

            // Intentamos crear una columna
            val colum = ColumnsUtils.castearAColumna(it.first,it.second)

            if (colum != null){
                columnas.add(colum)
            }
        }

        // Si tenemos columnas en la lista, crearemos una tabla con ellas
        if (columnas.size > 0){
            tabla = Table.create()
            columnas.forEach {
                tabla.addColumns(it)
            }
        }

        // No se puede crear la fuente de items
        else {
            throw InstantiationException("El item usado para la creación de la fuente no es válido")
        }
    }

    /**
     * Retornamos las cabeceras de la tabla que contiene los
     * datos
     *
     * @return MutableList<String>?: Lista de cabeceras
     */
    fun obtenerCabeceras(): MutableList<String>? {
        return this.tabla.columnNames()
    }


    /**
     * Añadimos a la fuente de datos actual los valores
     * de los atributos almacenados en el
     *
     * @param item: Item del que extraeremos los datos
     */
    fun anadirItem(item: T){

        // Comprobamos que el tipo del item sea igual que
        // que el tipo d edatos de la fuente de datos
        if (item::class.java == tipo){

            // Recorremos las columnas de la tabla
            tabla.columnNames().forEach {

                // Obtenemos el valor del respectivo atirbuto del item
                val valor = item.obtenerValorDe(it,item)

                // Añadimos el valor a su respectiva columna
                tabla.column(it).appendCell(valor)
            }
        }
    }


    /**
     * Devolvemos la siguiente fila de datos
     * de la fuente
     *
     * @return ArrayList<String>?: Lista con los valores de la lista
     */
    fun siguienteFila(): ArrayList<String>? {

        // Datos de la fila
        var temp:  ArrayList<String>? = null

        // Comprobamos que aún halla tuplas por recorrer
        if (hayMasFilas()){

            // Valor por defecto que pasaremos
            var valor = "\'\'"

            // Lista con los valores de cada fila
            temp = ArrayList()

            // Obtenemos los valores de cada columna para su respectiva fila
            tabla.columnNames().forEach {
                valor = tabla.column(it).get(ultTuplaRecorrida).toString()
                temp.add(valor)
            }

            // Aumentamos la fila que recorreremos en la siguiente llamada
            ultTuplaRecorrida++
        }

        return temp
    }

    /**
     * Retornamos si quedan más filas que recorrer
     *
     * @return Boolean: Si quedan más filas
     */
    fun hayMasFilas(): Boolean{
        return tabla.rowCount() > ultTuplaRecorrida
    }
}
