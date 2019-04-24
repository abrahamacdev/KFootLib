package Modelo

import Utiles.Constantes
import Utiles.Utils
import io.reactivex.Observable
import tech.tablesaw.api.Table
import tech.tablesaw.io.csv.CsvWriteOptions
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.Writer
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

class ConjuntoInmueble<T: Inmueble>(clazz: Class<T>, listaInmuebles: List<T>? = null, propiedades: PropiedadesConjuntoInmueble = PropiedadesConjuntoInmueble()) {

private lateinit var tipoActual: Class<T>                               // Tipo de dato que se almacena en el dataframe
    private lateinit var nomCols: List<String>                          // Nombre de las columnas que almacena el dataframe
    private lateinit var dataframe: Table                               // Dataframe con los datos
    private var yaExisteArchivo: Boolean = false;                       // Comprobamos si el archivo ya ha sido creado
    private lateinit var propiedades: PropiedadesConjuntoInmueble;      // Conjunto de propiedades que se usarán pòr defecto
    private lateinit var nombreArchivo: String;                         // Nombre con extensión del archivo donde se escribirán los datos

    companion object {

        /**
         * Creamos un {[ConjuntoInmuebleFactory]} del tipo necesitado
         *
         * @return {[ConjuntoInmuebleFactory]}
         */
        inline fun <reified T: Inmueble> create(listaInmueble: List<T>? = null, propiedadesConjuntoInmueble: PropiedadesConjuntoInmueble? = PropiedadesConjuntoInmueble()): ConjuntoInmueble<T> = ConjuntoInmueble<T>(T::class.java, listaInmueble)

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

    init {

        // Guardamos las propiedades que utilizaremos
        setPropiedades(propiedades)

        // Guardamos la clase de objeto que se almacenará en el dataframe
        setTipoActual(clazz)

        // Comprobamos que el archivo en el que se guardarán los datos exista
        val tempNombreArchivo = this.propiedades.nombreArchivo ?: obtenerNombreArchivoDefecto()
        val archivoCompleto = nombreArchivo + determinarExtension()
        nombreArchivo = archivoCompleto
        val archivo = File(archivoCompleto)
        if (archivo.exists() && archivo.isFile) {
            yaExisteArchivo = true
        }

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
                    Utils.castearAColumna(it.first,it.second)!!
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

                // Recorremos cada atributo del inmueble
                atributos.forEach { atributo ->

                    val valor = obtenerValorAtributo(atributo,inmueble)
                    dataframe.column(atributo).appendCell(valor)

                }
            }
        }
    }

    /**
     * Obtenemos el valor del {[atributo]} si existe
     * en el objeto {[inmueble]}
     *
     * @param atributo: Atributo a obtener del {[inmueble]}
     * @param inmueble: Objeto en el que buscaremos el inmueble
     *
     * @return String: Valor del parametro
     */
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


    /**
     * Ejecutamos un volcado de datos en el archivo
     * y directorio deseados
     */
    fun guardar(){

        // Comprobamos que la tabla tenga datos para guardar
        if (dataframe.rowCount() > 0){

            // Añadimos los datos al archivo
            val bufferedWriter = BufferedWriter(FileWriter(File(nombreArchivo),true))
            var opciones: CsvWriteOptions;

            // Si el archivo ya existe, no escribiremos las cabeceras
            if (yaExisteArchivo){
                opciones = CsvWriteOptions.builder(bufferedWriter).header(false).build()
            }

            opciones = CsvWriteOptions.builder(bufferedWriter).build()

            dataframe.write().csv(opciones)         // Escribimos los datos en el archivo

            bufferedWriter.close()                  // Cerramos el búffer

            dataframe = dataframe.emptyCopy()       // Eliminamos los datos del dataframe
            yaExisteArchivo = true                  // Si el archivo no existía ha sido creado
        }
    }

    /**
     * Convertimos {[Constantes.EXTENSIONES_ARCHIVOS]} a una extensión válida
     * para nuestro archivo
     *
     * @return String: Extensíon a usar en el archivo
     */
    private fun determinarExtension(): String{

        var extension: String = "";

        when{
            propiedades.extensionArchivo == Constantes.EXTENSIONES_ARCHIVOS.CSV -> { extension = ".csv" }
        }

        return extension
    }

    private fun setNomCols(nomCols: List<String>){
        this.nomCols = nomCols
    }

    private fun setTipoActual(tipo: Class<T>){
        this.tipoActual = tipo
    }

    private fun setPropiedades(propiedades: PropiedadesConjuntoInmueble){
        this.propiedades = propiedades
    }
}