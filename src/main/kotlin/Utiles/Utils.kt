package Utiles

import Modelo.Inmueble
import com.andreapivetta.kolor.Color
import com.andreapivetta.kolor.Kolor
import tech.tablesaw.api.*
import tech.tablesaw.columns.Column

object Utils {


    /**
     * Un tipo primitivo no tiene clase, por lo que tendremos que castearlo directamente
     * a su tipo.
     *
     * Las variables que sean de un tipo no primitivo pueden ser casteadas
     */
    /*val variables = Inmueble().javaClass.declaredFields.forEach {
        println(it.name + " || " + it.type)

        if (it.type == Class.forName("java.lang.String")) {
            nombre = it.name
            tipo = it.type
        }

        if (tipo != null) {

            val a = retornar(nombre, tipo) as String
            println(a.javaClass.name)
        }
    }



    fun retornar(valor: String, clase: Class<*>?): Any{
        return clase!!.cast(valor)
    }*/

    /**
     * Logueamos los mensajes pasados por parámetro según el "nivel de debug" necesitado
     * y el establecido para la sesión actual.
     *
     * @param Constantes.DEBUG nivelRequerido: Nivel de debug requerido para mostrar el mensaje
     * @param String mensaje: Texto a mostrar
     * @param Color color: Color con el que se mostrará el mensaje
     */
    fun debug(nivelRequerido: Constantes.DEBUG, mensaje: String, color: Color = Color.BLACK){
        // Comprobamos que queremos loguear
        if (Constantes.DEBUG.DEBUG_LEVEL.value != Constantes.DEBUG.DEBUG_NONE.value){

            // El mensaje es de un test y estamos en el nivel de "Test"
            if (nivelRequerido.value == Constantes.DEBUG.DEBUG_LEVEL.value && Constantes.DEBUG.DEBUG_LEVEL.value == Constantes.DEBUG.DEBUG_TEST.value){
                println(Kolor.foreground(mensaje,color))
            }

            // Ej: Si el nivel actual es 'Avanzado', todos los de nivel 'Simple' también se mostrarán
            else if (nivelRequerido.value <= Constantes.DEBUG.DEBUG_LEVEL.value && nivelRequerido.value != Constantes.DEBUG.DEBUG_NONE.value){
                println(Kolor.foreground(mensaje,color))
            }
        }
    }

    /**
     * Creamos una columna a partir del {[nombreCol]} y del tipo de la {[clase]}
     *
     * @param String nombreCol: Nombre de la columna a crear
     * @param Class<*> clase: Clase del tipo de columna a crear
     * @return Column<*>?: Columna del tipo de la {[clase]]} pasada por parámetro
     */
    fun castToColumn(nombreCol: String, clase: Class<*>): Column<*>?{

        when {
            clase.name.equals("boolean") -> return BooleanColumn.create(nombreCol)
            clase.name.equals("int") -> return IntColumn.create(nombreCol)
            clase.name.equals("float") -> return FloatColumn.create(nombreCol)
            clase.name.equals("double") -> return DoubleColumn.create(nombreCol)
            clase.name.equals("java.lang.String") -> return StringColumn.create(nombreCol)
        }

        // Tipo de dato no soportado
        debug(Constantes.DEBUG.DEBUG_SIMPLE, "Tipo de dato aún no soportado. Por favor, compruebe los tipos permitidos", Color.RED)

        return null
    }

}