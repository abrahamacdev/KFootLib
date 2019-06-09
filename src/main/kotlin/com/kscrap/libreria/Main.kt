package com.kscrap.libreria

import com.kscrap.libreria.Modelo.Dominio.Inmueble
import com.kscrap.libreria.Modelo.Repositorio.ConfiguracionRepositorioInmueble
import com.kscrap.libreria.Modelo.Repositorio.RepositorioInmueble
import com.kscrap.libreria.Utiles.Constantes
import com.kscrap.libreria.Utiles.TIPOS_CONTRATOS
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit

fun main(args: Array<String>) = runBlocking<Unit> {

    val inmueble = Inmueble(
        "Calle Saturno",
        "Chiclana",
        300,
        300000.0,
        "€",
        TIPOS_CONTRATOS.TIPO_CONTRATO_ALQUILER,
        "",
        "www.google.com"
    )

    val vivienda2 = Vivienda(
            "Calle Venus",
            "Sanlucar",
            450,
            450000.0,
            "€",
            TIPOS_CONTRATOS.TIPO_CONTRATO_ALQUILER,
            "",
            "www.google.com",
            2
    )

    val listaInmuebles = listOf<Inmueble>(inmueble, vivienda2)

    val propiedades = ConfiguracionRepositorioInmueble()
    with(propiedades){
        guardaLosDatosEn("/home/abraham/Documentos")
        archivoConExtension(Constantes.EXTENSIONES_ARCHIVOS.csv)
    }

    propiedades.establecerRutaArchivo("/home/abraham/Documentos/archivo.csv")

    val c = RepositorioInmueble.create<Inmueble>(propiedades =  propiedades)
    c.anadirListaInmuebles(listaInmuebles)

    val sujeto: PublishSubject<Nothing> = PublishSubject.create()
    sujeto.subscribe({},{},{
        println("Guardado realizado")
    })

    c.guardar(sujeto)
}