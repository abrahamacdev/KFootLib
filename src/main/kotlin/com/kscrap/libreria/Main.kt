package com.kscrap.libreria

import com.kscrap.libreria.Controlador.Transmisor
import com.kscrap.libreria.Modelo.Dominio.Inmueble
import com.kscrap.libreria.Modelo.Repositorio.ConfiguracionRepositorioInmueble
import com.kscrap.libreria.Modelo.Repositorio.RepositorioInmueble
import com.kscrap.libreria.Utiles.Constantes
import com.kscrap.libreria.Utiles.TIPOS_CONTRATOS
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit

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
    val inmueble2 = Vivienda(
        "Calle Luna",
        "San Fernando",
        450,
        450000.0,
        "€",
        TIPOS_CONTRATOS.TIPO_CONTRATO_ALQUILER,
        "",
        "www.google.com",
            2
    )

    val listaInmuebles = listOf<Inmueble>(inmueble,inmueble2)

    val propiedades = ConfiguracionRepositorioInmueble()
    with(propiedades){
        guardaCada(3, TimeUnit.SECONDS)
        guardaLosDatosEn("/home/admin/Documentos")
        archivoConNombre("Prueba")
        archivoConExtension(Constantes.EXTENSIONES_ARCHIVOS.CSV)
    }

    val c = RepositorioInmueble.create<Inmueble>(propiedades =  propiedades)
    c.anadirListaInmuebles(listaInmuebles)

    // Transmisor.crear<Inmueble>().getTipoTransmisor()

    while (!c.todoGuardado()){

        runBlocking {
            delay(500)
        }
    }
}