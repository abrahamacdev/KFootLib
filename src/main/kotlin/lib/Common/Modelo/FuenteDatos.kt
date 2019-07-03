package lib.Common.Modelo

import lib.Common.Controlador.Item.Item
import lib.Common.Utiles.ColumnsUtils
import tech.tablesaw.api.Table
import tech.tablesaw.columns.Column
import java.lang.reflect.Field

class FuenteDatos <T: Item>(tipo: Class<*>, tabla: Table? = null){

    private var cabeceras: ArrayList<String> = ArrayList()

    // Guardámos la posición de los valores de cada fila
    // dentro del map de valores
    private var filas: HashMap<Int,HashMap<String,Int>> = HashMap()

    // Número de la última fila añadida
    private var ultFila: Int = 0

    // Guardamos todos los valores en columnas indexados por el nombre de la columna
    private var valores: HashMap<String,Column<Any?>> = HashMap()

    // Número de filas de la columna que más filas tiene
    private var numMayTupla = -1

    // Número de la última fila recorrida
    var ultTuplaRecorrida = 0

    // Tipo de dato que se almacenará en la FuenteDatos
    val tipoFuenteDatos: Class<*> = tipo


    companion object {
        inline fun <reified T: Item> crear(tabla: Table? = null): FuenteDatos<T>{
            return FuenteDatos(T::class.java, tabla)
        }
    }

    init {

        // Comprobamos si tenemos datos con los que crear
        // la fuente de datos
        if (tabla != null){
            crearFuenteDeDatos(tabla)
        }
    }



    fun getCabeceras(): ArrayList<String>{
        return this.cabeceras
    }

    /**
     * Comprobamos si existe una cabecera con el
     * [nombre] recibido por parámetros
     *
     * @param nombre: Nombre de la cabecera a buscar
     *
     * @return Boolean: Si existe dicha cabecera o no
     */
    private fun existeCabecera(nombre: String): Boolean{
        return cabeceras.indexOf(nombre) != -1
    }

    /**
     * Actualizamos el número de la columna
     * con más tuplas si la [columna] recibida por
     * parámetros es mayor
     *
     * @param columna: Columna a contar
     */
    private fun actualizarNumTuplasMayorCol(columna: Column<Any?>){

        val temp = columna.count()

        if(temp > numMayTupla){
            numMayTupla = temp
        }
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
                valores.put(nombre,columna as Column<Any?>)
            }
        }

        // Guardamos el número de la última tupla
        numMayTupla = table.rowCount()
    }

    /**
     * Añadimos a la fuente de datos actual los valores
     * de los atributos almacenados en el
     *
     * @param item: Item del que extraeremos los datos
     */
    fun anadirItem(item: T){

        // Obtenemos los atributos del item
        val cabecerasItem = item.obtenerNombreAtributos()

        // Posición de los valores en sus respectivas columnas
        val posValoresFila: HashMap<String, Int> = HashMap()

        // Recorremos las cabeceras del item
        cabecerasItem.forEach {cabecera ->

            // Obtenemos el atributo del item
            val campo = item.buscarCampo(cabecera)

            // Si no se ha encontrado el campo lo ignoramos
            if(campo == null){
                return@forEach
            }

            // Comprobamos que el tipo de dato del campo
            // sea válido
            if (ColumnsUtils.tipoDatoValido(campo.type)){

                // Comprobamos si la cabecera no existe en la fuente de datos
                // para añadirla
                if (!existeCabecera(cabecera)){

                    // Reajustamos la fuente de datos para añadir una columna más
                    anadirNuevaCol(cabecera, campo)
                }

                // Obtenemos el valor del atributo
                val valor = item.obtenerValorDe(cabecera,item)

                // Obtenemos la columna correspondiente
                val tempCol = valores.get(cabecera)

                // Guardamos la posición del valor dentro de la columna
                posValoresFila.put(cabecera, tempCol!!.count())

                // Añadimos el valor del atributo a la columna
                tempCol.appendCell(valor)

                // Actualizamos el valor
                valores.replace(cabecera,tempCol)

                // Actualizamos el número de la columna con más tuplas
                actualizarNumTuplasMayorCol(tempCol)
            }
        }

        // Añadimos una tupla al map de filas
        filas.put(ultFila, posValoresFila)

        // Actualizamos el índice que tendra la próxima fila a insertar
        ultFila++
    }

    /**
     * Añadimos una nueva columna a la fuente
     * de datos
     *
     * @param cabecera: Nombre de la columna
     * @param campo: Atributo del objeto del que extraeremos el valor
     */
    fun anadirNuevaCol(cabecera: String, campo: Field){


        // Obtenemos la columna que añadiremos al map de valores
        val tempCol = ColumnsUtils.castearAColumna(cabecera,campo.type)

        // Añadimos la cabecera a la fuente de datos
        cabeceras.add(cabecera)

        // Añadimos la columna al map de valores
        valores.put(cabecera, tempCol as Column<Any?>)
    }

    /*
    /**
     * Añadimos los datos de una columna a la fuente de datos
     *
     * @param columna: Columna de la que obtendremos los datos
     */
    fun anadirDatos(columna: Column<*>){

        // No existe ninguna cabecera con el nombre de la columna
        if (cabeceras.indexOf(columna.name()) == -1){

            // Obtenemos el tipo de la columna
            val tipo = Utils.castearColTypeAClase(columna.type())

            // Comprobamos que tenga un tipo válido
            if (tipo != null){

                // Guardamos la cabecera de la columna y una columna del tipo apropiado
                cabeceras.add(columna.name())
                valores.put(columna.name(), Utils.castearAColumna(columna.name(),tipo)!! as Column<Any>)
            }

            // No hemos podido añadir la columna porque el
            // tipo no es válido
            else {
                return
            }
        }

        // Obtenemos el tipo y la columna segun el nombre de la cabecera
        val nuevaTempCol = valores.get(columna.name())
        val tipoCol = Utils.castearColTypeAClase(nuevaTempCol!!.type())

        // Añadimos los datos a nuestra columna
        columna.forEach {

            // Comprobamos que no sea un valor nulo y el tipo
            // del objeto coincida con el de la columna
            if (it != null && it.javaClass == tipoCol){
                nuevaTempCol.append(it)
            }
        }

        // Actualizamos los cambios
        valores.replace(columna.name(), nuevaTempCol)

        // Actualizamos el valor de la columna con más tuplas
        if (nuevaTempCol.count() > numMayTupla){
            numMayTupla = nuevaTempCol.count()
        }
    }

    /**
     * Añadimos los datos de una tabla a la fuente de datos
     *
     * @param tabla: Tabla de la que obtendremos los datos
     */
    fun anadirDatos(tabla: Table){
        tabla.columns().asSequence().forEach {
            anadirDatos(it)
        }
    }

    /**
     * Añadimos los datos de una lista a la fuente de datos
     *
     * @param listaValores: Lista de la que obtendremos los datos
     * @param nombreCabecera: Nombre de la cabecera a la que pertenece
     */
    inline fun <reified T> anadirDatos(listaValores: List<T>, nombreCabecera: String){
        anadirDatos(listaValores,nombreCabecera,T::class.java)
    }

    /**
     * Añadimos los datos de una lista a la fuente de datos.
     * * Esta función complementa a [anadirDatos] para poder añadir los
     * datos correctamente
     *
     * @param listaValores: Lista de la que obtendremos los datos
     * @param nombreCabecera: Nombre de la cabecera a la que pertenece
     * @param tipo: Tipo de datos almacenado en la [listaValores]
    */
    fun <T> anadirDatos(listaValores: List<T>, nombreCabecera: String, tipo: Class<*>){

        // Comprobamos si hay elementos en la lista
        if(listaValores.size > 0){

            // Comprobamos si la cabecera no ha sido incluida en la fuente de datos
            if(cabeceras.indexOf(nombreCabecera) == -1){

                // Obtenemos la columna que añadiremos al map de valores
                val tempCol = Utils.castearAColumna(nombreCabecera,tipo)

                // Comprobamos que el tipo de la columna sea
                // válido
                if (tempCol != null){

                    // Añadimos la cabecera a la fuente de datos
                    cabeceras.add(nombreCabecera)

                    // Añadimos la columna al map de valores
                    valores.put(nombreCabecera, tempCol as Column<Any>)
                }

                // La columna no es de un tipo válido
                else {
                    return
                }
            }

            // Obtenemos el tipo y la columna segun el nombre de la cabecera
            val nuevaTempCol = valores.get(nombreCabecera)

            // Permitira comprobar si los elementos de la lista
            // se pueden añadir a la fuente de datos
            var tipoValido: Boolean = false
            var tempCol = Utils.castearAColumna("",tipo)
            if (tempCol != null){
                tipoValido = tempCol.type() == nuevaTempCol!!.type()
            }

            // Comprobamos si podemos añadir los elementos de
            // la lista a la fuente de datos
            if (tipoValido){

                // Añadimos todos los valores de la lista a la columna
                listaValores.forEach {

                    // Comprobamos que el valor no sea nulo
                    if (it != null){
                        nuevaTempCol!!.appendCell(it.toString())
                    }
                }

                // Actualizamos los cambios
                valores.replace(nombreCabecera, nuevaTempCol!!)

                // Guardamos el valor de la última tupla
                if (nuevaTempCol.count() > numMayTupla){
                    numMayTupla = nuevaTempCol.count()
                }
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

                // Comprobamos que el valor no sea nulo
                if (fila.getValue(it.key) != null){

                    // Comprobamos si la cabecera existe en la lista de cabeceras
                    if(cabeceras.indexOf(it.key) == -1){

                        // Obtenemos el tipo de dato que se almacenará en
                        // la columna y creamos una columna de es tipo
                        val tipoAlmacenado = fila.getValue(it.key)!!.javaClass
                        val column = Utils.castearAColumna(it.key, tipoAlmacenado)

                        // Comprobamos que se haya podido crear la columna
                        if (column != null){

                            // Añadimos la cabecera a la lista de cabeceras
                            cabeceras.add(it.key)

                            // Añadimos la columna
                            valores.put(it.key, column as Column<Any>)
                        }

                        // No se ha podido crear la columna
                        else {
                            return@forEach
                        }
                    }

                    // Obtenemos el tipo y la columna segun el nombre de la cabecera
                    val nuevaTempCol = valores.get(it.key)
                    val tipoCol = nuevaTempCol!!.type().name().toLowerCase()

                    // Añadimos el valor a la columna si coincide con
                    // el tipo de la columna
                    val valor = fila.getValue(it.key)
                    val tipoValorSpliteado = valor!!.javaClass.toString().split(".")
                    val tipoValor = tipoValorSpliteado.get(tipoValorSpliteado.size - 1).toLowerCase()

                    // Comprobamos que el tipo del valor sea igual que el de la columna
                    if (tipoValor.equals(tipoCol)){

                        // Añadimos el valor a la columna
                        nuevaTempCol.appendCell(valor.toString())

                        // Actualizamos los cambios de la variable #valores
                        valores.replace(it.key, nuevaTempCol)
                    }

                    // Actualizamos el valor de la columna con más tuplas
                    if (nuevaTempCol.count() > numMayTupla){
                        numMayTupla = nuevaTempCol.count()
                    }
                }
            }
        }
    }*/


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
        if (ultTuplaRecorrida < numMayTupla){

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
                    if (ultTuplaRecorrida < cantidadValores){

                        valor = columna.get(ultTuplaRecorrida).toString()
                    }

                    // Añadimos el valor a la fila
                    fila.add(valor)
                }
            }

            // Aumentamos la fila que recorreremos en la siguiente llamada
            ultTuplaRecorrida++
        }

        return fila
    }

    /**
     * Retornamos si quedan más filas que recorrer
     *
     * @return Boolean: Si quedan más filas
     */
    fun hayMasFilas(): Boolean{
        return ultTuplaRecorrida < numMayTupla
    }
}