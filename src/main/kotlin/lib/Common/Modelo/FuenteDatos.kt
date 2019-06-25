package lib.Common.Modelo

import lib.Common.Utiles.Utils
import tech.tablesaw.api.Row
import tech.tablesaw.api.Table
import tech.tablesaw.columns.Column

class FuenteDatos {

    private var cabeceras: ArrayList<String> = ArrayList()
    private var valores: HashMap<String,Column<Any>> = HashMap()

    // Número de filas de la columna que más filas tiene
    private var numMayTupla = -1

    // Número de la última fila recorrida
    var ultTupla = 0

    constructor(){}

    constructor(table: Table){
        crearFuenteDeDatos(table)
    }


    fun getCabeceras(): ArrayList<String>{
        return this.cabeceras
    }



    /**
     * Creamos una fuente de datos válida para
     * su poesterior tratamiento
     *
     * @param table: Tabla de la que obtendremos los datos
     */
    private fun crearFuenteDeDatos(table: Table){

        // Obtenemos los nombres de las columnas
        cabeceras = table.columnNames() as ArrayList<String>


        // Establecemos los valores de cada columna
        table.columns().forEach {columna ->
            val nombre = cabeceras.find { it.equals(columna.name()) }
            if (nombre != null){
                valores.put(nombre,columna as Column<Any>)
            }
        }

        // Guardamos el número de la última tupla
        numMayTupla = table.rowCount()
    }


    /**
     * Añadimos los datos de una columna a la fuente de datos
     *
     * @param columna: Columna de la que obtendremos los datos
     */
    fun anadirDatos(columna: Column<Any>){

        // No existe ninguna cabecera con el nombre de la columna
        if (cabeceras.indexOf(columna.name()) == -1){
            cabeceras.add(columna.name())
            valores.put(columna.name(), columna)
        }

        // Ya existe la cabecera, actualizamos los datos de la columa
        else {

            val nuevaColumna = valores.get(columna.name())

            // Comprobamos que el tipo de dato de la vieja columna coincida con el de la nueva columna
            if (nuevaColumna!!.type() == columna.type()){

                // Añadimos a la vieja columna los datos de la nueva
                columna.forEach { nuevaColumna.append(it) }

                // Actualizamos los cambios de la variable #valores
                valores.replace(columna.name(), nuevaColumna)
            }
        }

        // Guardamos el valor de la última tupla
        if (columna.count() > numMayTupla){
            numMayTupla = columna.count()
        }
    }

    /**
     * Añadimos los datos de una columna a la fuente de datos
     *
     * @param listaValores: Lista de la que obtendremos los datos
     * @param nombreCabecera: Nombre de la cabecera a la que pertenece
     */
    fun anadirDatos(listaValores: List<Any>, nombreCabecera: String){

        // Comprobamos si hay elementos en la lista
        if(listaValores.size > 0){

            // Comprobamos si la cabecera no ha sido incluida en la fuente de datos
            if(cabeceras.indexOf(nombreCabecera) == -1){

                // Añadimos la cabecera a la fuente de datos
                cabeceras.add(nombreCabecera)

                // Tipo de dato que se almacena en la lista
                val tipoAlmacenado = listaValores.get(0).javaClass

                // Añadimos todos los valores de la lista a la fuente de datos
                val column = Utils.castearAColumna(nombreCabecera, tipoAlmacenado)

                // Añadimos todos los valores de la lista a la columna
                listaValores.forEach {
                    if (it.javaClass == tipoAlmacenado){
                        column!!.appendCell(it.toString())
                    }
                }
            }

            // Añadimos los datos a la columna ya existente
            else {

                val nuevaColumna = valores.get(nombreCabecera)
                val tipoDato = nuevaColumna!!.get(0).javaClass

                // Comprobamos que el tipo de dato de la vieja columna coincida con el de la nueva columna
                if (nuevaColumna!!.type().name().equals(nombreCabecera)){

                    // Añadimos a la vieja columna los datos de la nueva
                    listaValores.forEach {

                        // Comprobamos que el tipo de dato del objeto
                        // coincida con el que se almacena
                        if (tipoDato == it.javaClass){
                            nuevaColumna.append(it)
                        } }

                    // Actualizamos los cambios de la variable #valores
                    valores.replace(nombreCabecera, nuevaColumna)
                }
            }

            // Guardamos el valor de la última tupla
            if (listaValores.count() > numMayTupla){
                numMayTupla = listaValores.size
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

        var fila:  ArrayList<String>? = null

        // Comprobamos que aún halla tuplas por recorrer
        if (ultTupla < numMayTupla){

            fila = ArrayList()

            cabeceras.forEach {cabecera ->

                // Obtenemos los valores de la columna correspondiente
                val columna = valores.get(cabecera)

                if(columna != null){

                    // Numero de valores que se almacena en la columna
                    val cantidadValores = columna.size()

                    // Valor de la fila correspondiente
                    var valor = "\'\'"

                    // Comprobamos que la columna tenga suficiente valores
                    if (ultTupla < cantidadValores){

                        valor = columna.get(ultTupla).toString()
                    }

                    // Añadimos el valor a la fila
                    fila.add(valor)
                }
            }

            // Aumentamos la fila que recorreremos en la siguiente llamada
            ultTupla++
        }

        return fila
    }

    /**
     * Retornamos si quedan más filas que recorrer
     *
     * @return Boolean: Si quedan más filas
     */
    fun hayMasFilas(): Boolean{
        return ultTupla < numMayTupla
    }
}