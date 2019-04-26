import Modelo.Repositorio.RepositorioInmueble
import Modelo.Dominio.Inmueble
import Modelo.Repositorio.PropiedadesRepositorioInmueble
import Utiles.Constantes
import Utiles.TIPOS_CONTRATOS

fun main(args: Array<String>){

    val inmueble = Inmueble(
        "Calle Sol",
        "Chiclana",
        300,
        300000.0,
        "€",
        TIPOS_CONTRATOS.TIPO_CONTRATO_ALQUILER,
        "",
        "www.google.com"
    )
    val inmueble2 = Inmueble(
        "Calle Luna",
        "San Fernando",
        450,
        450000.0,
        "€",
        TIPOS_CONTRATOS.TIPO_CONTRATO_ALQUILER,
        "",
        "www.google.com"
    )

    val propiedadesConjuntoInmueble= PropiedadesRepositorioInmueble()
    with(propiedadesConjuntoInmueble){
        guardalosEn("/home/admin/Documentos")
        conNombre("Prueba")
        conExtension(Constantes.EXTENSIONES_ARCHIVOS.CSV)
    }

    val c = RepositorioInmueble.create<Inmueble>(listOf(inmueble,inmueble2), propiedadesConjuntoInmueble)
    c.guardar()

}