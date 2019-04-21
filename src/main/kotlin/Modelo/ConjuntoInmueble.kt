package Modelo

import Utiles.Utils
import tech.tablesaw.api.Table
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ConjuntoInmueble {

    private var modificado = false                      // Si el @ConjuntoInmueble a sido modificado
    private lateinit var tipoActual: Class<*>           // Tipo de dato que se almacena en el dataframe
    private lateinit var nomCols: List<String>          // Nombre de las columnas que almacena el dataframe
    private lateinit var dataframe: Table               // Dataframe con los datos
    private var yaExisteArchivo: Boolean = false;       // Comprobamos si el archivo ya ha sido creado

    constructor(listaInmuebles: List<Inmueble> = listOf(), guardadoAutomatico: Boolean = true, masOpciones: List<Pair<String, Any>>? = null){

        // Hay inmuebles en la lista
        if (listaInmuebles.size == 0){
            crearDataFrameVacio()
        }
    }

    /**
     * Creamos un dataframe con los datos del {[Inmueble]} que se esté almacenando
     */
    fun crearDataFrameVacio(){

        dataframe = Table.create()
        val lista = Inmueble.obtenerNombreTipoAtributos() // Nombre de los atributos y su tipo

        with(dataframe){

            addColumns(
                //Hacemos uso del operador "spread" (*), que nos permite pasar un Array a un vararg
                 *lista.map {
                    Utils.castToColumn(it.first,it.second)!!
                }.toTypedArray()
            )
        }

        // Establecemos los nombres de las columnas a los nombres de los atributos
        // de un "Inmueble"
        setNomCols(Inmueble.obtenerNombreAtributos())

        // Establecemos el tipo actual del dataframe para que sea de tipo "Inmueble"
        setTipoActual(Class.forName(Inmueble::class.java.canonicalName))
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

    private fun setTipoActual(tipo: Class<*>){
        this.tipoActual = tipo
    }

}