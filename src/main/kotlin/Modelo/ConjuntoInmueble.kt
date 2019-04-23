package Modelo

import Utiles.Utils
import tech.tablesaw.api.Table
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class ConjuntoInmueble<T: Inmueble> {

    private lateinit var tipoActual: Class<T>                       // Tipo de dato que se almacena en el dataframe
    private lateinit var nomCols: List<String>                      // Nombre de las columnas que almacena el dataframe
    private lateinit var dataframe: Table                           // Dataframe con los datos
    private var yaExisteArchivo: Boolean = false;                   // Comprobamos si el archivo ya ha sido creado
    var propiedadesConjuntoInmueble = PropiedadesConjuntoInmueble() // Creamos un conjunto de propiedades por defecto


    companion object {

        /**
         * Creamos un {[ConjuntoInmuebleFactory]} del tipo necesitado
         *
         * @return {[ConjuntoInmuebleFactory]}
         */
        inline fun <reified T: Inmueble> create(listaInmueble: List<T>? = null, propiedadesConjuntoInmueble: PropiedadesConjuntoInmueble? = null): ConjuntoInmueble<T> = ConjuntoInmueble<T>(T::class.java, listaInmueble)

        /**
         * Creamos un {[ConjuntoInmuebleFactory]} del tipo necesitado
         *
         * @return {[ConjuntoInmuebleFactory]}
         */
        inline fun <reified T: Inmueble> create(block: (ConjuntoInmueble<T>) -> Unit): ConjuntoInmueble<T> {
            val conjuntoInmueble = ConjuntoInmueble<T>(T::class.java)   // Creamos el ConjuntoInmueble
            block(conjuntoInmueble)                                     // Dejamos que establezca las propiedades y demás atributos
            return conjuntoInmueble                                     // Le devolvemos el ConjuntoInmueble ya modificado
        }

    }

    constructor(clazz: Class<T>, listaInmuebles: List<T>? = null, propiedadesConjuntoInmueble: PropiedadesConjuntoInmueble? = null){

        // Guardamos el tipo de datos que se almacenará
        setTipoActual(clazz)

        // Creamos un dataframe con los valores pasados
        if (listaInmuebles != null && listaInmuebles.size > 0){
            crearDataFrame(listaInmuebles)
        }
        // Creamos un data frame vacío
        else{
            crearDataFrameVacio()
        }


    }

    /**
     * Creamos un dataframe con los datos del {[Inmueble]} que se esté almacenando
     */
    private fun crearDataFrameVacio(){

        dataframe = Table.create()
        val inmueble = tipoActual.newInstance()
        val lista = inmueble.obtenerNombreTipoAtributos() // Nombre de los atributos y su tipo

        with(dataframe){

            addColumns(
                //Hacemos uso del operador "spread" (*), que nos permite pasar un Array a un vararg
                 *lista.map {
                    Utils.castToColumn(it.first,it.second)!!
                }.toTypedArray()
            )
        }

        dataframe.columns().forEach { println("Nueva columna con nombre ${it.name()} y tipo ${it.type()}") }

        // Establecemos los nombres de las columnas a los nombres de los atributos
        // de un "Inmueble"
        setNomCols(inmueble.obtenerNombreAtributos())
    }

    /**
     * Creamos el dataframe y lo llenamos con los datos pasados por parámetro
     *
     * @param List<T> listaInmuebles: Lista de inmuebles a guardar
     */
    private fun crearDataFrame(listaInmuebles: List<T>){

        crearDataFrameVacio()

        if (listaInmuebles.size > 0){

            val atributos = listaInmuebles.get(0).obtenerNombreAtributos()

            // Recorremos cada uno de los inmuebles
            listaInmuebles.forEach {inmueble ->

                // Recorremos los atributos del inmueble
                atributos.forEach { atributo ->

                    val valor = obtenerValorAtributo(atributo,inmueble)
                    dataframe.column(atributo).appendCell(valor)

                }
            }
        }
    }

    private fun obtenerValorAtributo(atributo: String, inmueble: T): String {

        val field = inmueble.javaClass.getDeclaredField(atributo)

        field.isAccessible = true
        return field.get(inmueble).toString()
    }


    /**
     * Obtenemos un nombre por defecto para el archivo en caso de que no
     * se proporcione uno al crear el objeto
     *
     * @return String: Nombre del archivo por defecto
     */
    private fun obtenerNombreArchivoDefecto(): String{
        val dtf: DateTimeFormatter = DateTimeFormatter.ofPattern("dd_MM_HH_mm")
        val ahora: LocalDateTime = LocalDateTime.now()
        return "KScrap_${dtf.format(ahora)}"
    }

    private fun setNomCols(nomCols: List<String>){
        this.nomCols = nomCols
    }

    private fun setTipoActual(tipo: Class<T>){
        this.tipoActual = tipo
    }

}