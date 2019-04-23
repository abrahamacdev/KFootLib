import Modelo.ConjuntoInmueble
import Modelo.Inmueble

fun main(args: Array<String>){

    val inmueble = Inmueble()

    val c = ConjuntoInmueble.create<Inmueble>(listOf(inmueble))

}