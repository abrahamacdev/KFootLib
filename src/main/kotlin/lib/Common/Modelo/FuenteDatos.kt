package lib.Common.Modelo

import lib.Common.Controlador.Item.Item
import lib.Common.Utiles.ColumnsUtils
import tech.tablesaw.api.ColumnType
import tech.tablesaw.api.Row
import tech.tablesaw.api.Table
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class FuenteDatos {

    // Guardamos la posición de los valores de cada fila
    // dentro del map de valores
    private val filas: HashMap<Int,Fila<Item>> = HashMap()

    // Índice de la próxima tupla
    var numProximaFila = 0

    /*  Map con la posición de las filas que han sido solicitadas
        En la práctica, hará referencia a aquellas tuplas que ya han sido guardadas
     */
    private val filasGuardadas: LinkedList<Int> = LinkedList()

    /*
        Map con la posición de las filas que aún no han sido solicitadas
        En la práctica hará referencia a las tuplas que quedan por guardar
     */
    private val filasPorGuardar: LinkedList<Int> = LinkedList()

    // Número de la última fila recorrida
    var ultTuplaRecorrida = 0



    /**
     * Añadimos a la fuente de datos actual los valores
     * de los atributos almacenados en el
     *
     * @param item: Item del que extraeremos los datos
     */
    fun anadirItem(item: Item){

        // Obtenemos los nombres de los atributos del item
        val atributosItem = item.obtenerNombreTipoAtributos()

        // Creamos una tabla temporal
        val tempDF = Table.create()

        // Comprobamos que todos los atributos del item
        // coincidan con las cabeceras de la fuente de datos
        atributosItem.forEach {

            // Creamos una columna temporal y le añadimos el valor correspondiente
            val tempCol = ColumnsUtils.castearAColumna(it.first,it.second)
            if (tempCol != null){

                tempCol.appendCell(item.obtenerValorDe(it.first,item))
                tempDF.addColumns(tempCol)
            }
        }

        // Fila con los valores del item
        val tempFila = Fila.crear<Item>(tempDF)

        // Añadimos la fila a la fuente de datos
        filas.put(numProximaFila, tempFila)

        // Marcamos la fila para guardarla
        filasPorGuardar.add(numProximaFila)

        // Actualizamos el número de la próxima fila
        numProximaFila++
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
        if (ultTuplaRecorrida < filas.size){

            // Obtenemos la fila por el índice
            var fila = filas.get(ultTuplaRecorrida)

            var valor = "\'\'"

            temp = ArrayList()

            // Obtenemos la cantidad de columnas en la fila
            val numCols = fila!!.columnCount()

            // Recorremos las columnas de la fila
            for (i in 0 until numCols){

                // Obtenemos el valor de la columna
                valor = fila.getObject(i).toString()

                // Añadimos el valor al array
                temp.add(valor)
            }

            // Eliminamos la fila por guardar
            filasPorGuardar.remove(ultTuplaRecorrida)

            // Marcamos la fila que ha sido guardarda
            filasGuardadas.add(ultTuplaRecorrida)

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
        return filasPorGuardar.size > 0
    }
}
