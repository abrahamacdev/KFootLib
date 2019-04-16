package Modelo

import Utiles.Utils
import com.andreapivetta.kolor.Kolor
import tech.tablesaw.api.Table
import kotlin.collections.ArrayList

class ConjuntoInmueble {

    private var modificado = false                      // Si el @ConjuntoInmueble a sido modificado
    public lateinit var tipoActual: Class<*>           // Tipo de dato que se almacena en el dataframe
    public lateinit var nomCols: ArrayList<String>     // Nombre de las columnas que almacena el dataframe
    public lateinit var dataframe: Table               // Dataframe con los datos

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
        val lista = Inmueble.obtenerNombreTipoAtributos() // Nombre de los atributos y sus tipos

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
        this.nomCols = Inmueble.obtenerNombreAtributos() as ArrayList<String>

        // Establecemos el tipo actual del dataframe para que sea de tipo "Inmueble"
        println(Inmueble::class.java.canonicalName)
        println(Kolor.javaClass.canonicalName)
        this.tipoActual = Class.forName(Inmueble::class.java.canonicalName)
    }



}