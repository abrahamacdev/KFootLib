import Modelo.ConjuntoInmueble
import Modelo.Inmueble
import Modelo.PropiedadesConjuntoInmueble

fun main(args: Array<String>){

    val inmueble = Inmueble()

    val propiedadesConjuntoInmueble= PropiedadesConjuntoInmueble()
    with(propiedadesConjuntoInmueble){
    }

    val c = ConjuntoInmueble.create<Inmueble>(listOf(inmueble,inmueble,inmueble))
    c.guardar()

}