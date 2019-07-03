package lib.Common.Controlador.Item

import java.lang.reflect.Field
import kotlin.collections.ArrayList

/**
 * Esta clase servirá como base para la elaboración de
 * items más elaborados. Nos ayudará a crear una abstracción
 * común con la que guardar datos
 *
 * * Un punto muy importante a tener en cuenta es que todos los atributos de las clases
 * hijas deben de contener única y exclusivamente valores que se almacenarán, es decir,
 * no pueden tener atributos de lógica de la clase puesto que estos se acabarán almacenando
 *
 * ** Otro punto importante es que todos los Items tienen que tener un constructor vacío
 */
abstract class Item {

    /**
     * Devuelve los nombres de las variables almacenadas en el objeto (calle,ciudad,m2...ect)
     *
     * @return List<String> lista con los nombres de las variables
     */
    final fun obtenerNombreAtributos(): List<String> {

        val campos = obtenerTodosCampos()

        val nombres = ArrayList<String>()

        campos.forEach {
            nombres.add(it.name)
        }

        return nombres
    }

    /**
     * Devolvemos el nombre de las variables almacenadas en el objeto junto
     * a su respectivo tipo
     *
     * @return List<Pair<String, Class<*>>> lista con el nombre de la variable y su tipo
     */
    final fun obtenerNombreTipoAtributos(): List<Pair<String, Class<*>>>{

        val atributos = obtenerTodosCampos()

        val nombreTipo = ArrayList<Pair<String,Class<*>>>()

        atributos.forEach {
            nombreTipo.add(Pair<String,Class<*>>(it.name, it.type))
        }

        return nombreTipo
    }


    /**
     * Buscamos en el objeto actual, un campo con el nombre
     * solicitado.
     *
     * @param nombre: Nombre del campo a buscar
     *
     * @return Field? campo si del objeto si es que existe
     */
    final fun buscarCampo(nombre: String): Field?{

        var superr: Class<*>? = this.javaClass
        while (superr != null && !superr.name.equals(Any::javaClass.name) ){
            val campo = superr.declaredFields.firstOrNull{
                it.name.equals(nombre)
            }

            if (campo != null){
                return campo
            }

            superr = superr.superclass
        }
        return null
    }

    /**
     * Retornamos todos los campos del objeto actual
     *
     * @return ArrayList<Field>
     */
    final fun obtenerTodosCampos(): ArrayList<Field>{

        val campos = ArrayList<Field>()

        var superr: Class<*>? = this.javaClass
        while (superr != null && !superr.name.equals(Any::javaClass.name) ){
            val campo = superr.declaredFields.forEach{
                campos.add(it)
            }

            superr = superr.superclass
        }

        return campos
    }


    /**
     * Devolvemos el valor de la variable solicitada si esta existe
     *
     * @param atributo: Atributo a buscar en el objeto
     * @param obj: Instancia del objeto
     *
     * @return String Valor del atributo si este existe
     */
    final fun obtenerValorDe(atributo: String, obj: Any): String?{

        val campo = buscarCampo(atributo)

        if (campo != null){
            campo.isAccessible = true
            return campo.get(obj).toString()
        }

        return null
    }

    /**
     *  Establecemos el valor pasado por parámetro
     *  en el campo solicitado
     *
     *  @param campo: Campo al que se le establecerá el valor si existe
     *  @param valor: Valor que se le establecerá
     *  @param obj: Instancia del objeto
     */
    final fun establecerValor(campo: Field, valor: Any, obj: Any){

        val campoDelObjeto = obtenerTodosCampos().firstOrNull{
            it.name.equals(campo.name)
        }

        if (campoDelObjeto != null){
            campoDelObjeto.isAccessible = true
            campoDelObjeto.set(obj,valor)
        }
    }

    /**
     *  Establecemos el valor pasado por parámetro
     *  en el campo solicitado
     *
     *  @param campo: Campo al que se le establecerá el valor si existe
     *  @param valor: Valor que se le establecerá
     *  @param obj: Instancia del objeto
     */
    final fun establecerValor(campo: String, valor: Any, obj: Any){

        val tmpCampo = buscarCampo(campo)

        if (tmpCampo != null){
            val campoDelObjeto = obtenerTodosCampos().firstOrNull{
                it.name.equals(tmpCampo.name)
            }

            if (campoDelObjeto != null){
                campoDelObjeto.isAccessible = true
                campoDelObjeto.set(obj,valor)
            }
        }
    }



    override fun toString(): String {
        return ""
    }
}