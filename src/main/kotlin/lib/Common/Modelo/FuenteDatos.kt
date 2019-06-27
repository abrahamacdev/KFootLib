package lib.Common.Modelo

import lib.Common.Utiles.Utils
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
    fun anadirDatos(columna: Column<Any?>){

        // No existe ninguna cabecera con el nombre de la columna
        if (cabeceras.indexOf(columna.name()) == -1){
            cabeceras.add(columna.name())
        }

        // Ya existe la cabecera, actualizamos los datos de la columa
        else {

            // Obtenemos el tipo y la columna segun el nombre de la cabecera
            val nuestraColumna = valores.get(columna.name())

            // Comprobamos que el tipo de dato de la vieja columna coincida con el de la nueva columna
            if (columna.type() == nuestraColumna!!.type()){

                // Añadimos a la vieja columna los datos de la nueva
                columna.forEach {
                    if (it != null){
                        nuestraColumna.append(it)
                    }
                 }

                // Actualizamos los cambios
                valores.replace(columna.name(), nuestraColumna)

                // Actualizamos el valor de la columna con más tuplas
                if (nuestraColumna.count() > numMayTupla){
                    numMayTupla = columna.count()
                }
            }
        }
    }

    /**
     * Añadimos los datos de una lista a la fuente de datos
     *
     * @param listaValores: Lista de la que obtendremos los datos
     * @param nombreCabecera: Nombre de la cabecera a la que pertenece
     */
    fun anadirDatos(listaValores: List<Any?>, nombreCabecera: String){

        // Comprobamos si hay elementos en la lista
        if(listaValores.size > 0){

            // Comprobamos si la cabecera no ha sido incluida en la fuente de datos
            if(cabeceras.indexOf(nombreCabecera) == -1){

                // Añadimos la cabecera a la fuente de datos
                cabeceras.add(nombreCabecera)

                // Tipo de dato que se almacena en la lista
                var tipoAlmacenado: Class<*> = this.javaClass
                for (i in 0 until listaValores.size){
                    val tmp = listaValores.get(i)

                    if (tmp != null){
                        tipoAlmacenado = tmp.javaClass
                    }
                }

                // Si todos los valores son nulos no los añadiremos a la fuente de datos
                if (tipoAlmacenado == this.javaClass){
                    return
                }

                // Añadimos todos los valores de la lista a la fuente de datos
                val column = Utils.castearAColumna(nombreCabecera, tipoAlmacenado)

                // Añadimos la columna al map de valores
                valores.put(nombreCabecera,column as Column<Any>)
            }

            // Obtenemos el tipo y la columna segun el nombre de la cabecera
            val columna = valores.get(nombreCabecera)
            val tipoDato = columna!!.get(0)!!.javaClass

            // Añadimos todos los valores de la lista a la columna
            listaValores.forEach {

                // Comprobamos que haya algún objeto
                if (it != null){

                    // Comprobamos que el tipo de dato del objeto coincida con el de
                    // la columna
                    if (it.javaClass == tipoDato){
                        columna.appendCell(it.toString())
                    }
                }

            }

            // Actualizamos los cambios de la variable #valores
            valores.replace(nombreCabecera, columna)

            // Guardamos el valor de la última tupla
            if (columna.count() > numMayTupla){
                numMayTupla = listaValores.size
            }
        }
    }

    /**
     * Añadimos los datos de una fila completa a la fuente de datos
     *
     * @param fila: Map del que obtendremos los datos
     */
    fun anadirDatosFila(fila: Map<String, Any?>){

        // Comprobamos si hay elementos en el map
        if(fila.size > 0){

            // Recorremos cada una de las claves del map
            fila.forEach {

                // Si el valor es nulo no lo insertaremos
                if (fila.getValue(it.key) == null){
                    return
                }

                // Comprobamos si la cabecera existe en la lista de cabeceras
                if(cabeceras.indexOf(it.key) == -1){

                    // Añadimos la cabecera a la lista de cabeceras
                    cabeceras.add(it.key)


                    // Tipo de dato que se almacena en el map
                    val tipoAlmacenado = fila.getValue(it.key)!!.javaClass

                    // Añadimos todos los valores de la lista a la fuente de datos
                    val column = Utils.castearAColumna(it.key, tipoAlmacenado)

                    // Añadimos la columna
                    valores.put(it.key, column as Column<Any>)
                }

                // Obtenemos el tipo y la columna segun el nombre de la cabecera
                val columna = valores.get(it.key)
                val tipoDato = columna!!.type().name().toLowerCase()

                // Añadimos el valor a la columna si coincide con
                // el tipo de la columna
                val valor = fila.getValue(it.key)
                val tipoValorSpliteado = valor!!.javaClass.toString().split(".")
                val tipoValor = tipoValorSpliteado.get(tipoValorSpliteado.size - 1).toLowerCase()

                // Comprobamos que el tipo del valor sea igual que el de la columna
                if (tipoValor.equals(tipoDato)){
                    columna.appendCell(valor.toString())
                }

                // Actualizamos los cambios de la variable #valores
                valores.replace(it.key, columna)

                // Actualizamos el valor de la columna con más tuplas
                val cuentaColumna = columna.count()

                if (cuentaColumna > numMayTupla){
                    numMayTupla = cuentaColumna
                }
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