package com.kscrap.libreria.Modelo.Repositorio

import com.kscrap.libreria.Modelo.Dominio.Inmueble
import io.reactivex.processors.PublishProcessor


class Transmisor<T: Inmueble> (val clazz: Class<T>){

    private lateinit var processor: PublishProcessor<Inmueble>

    companion object {

        inline fun <reified T: Inmueble> crear(): Transmisor<T>{
            return Transmisor<T>(T::class.java)
        }

    }

    /**
     * Enviamos el inmueble pasado por parámetro
     * a través del {[processor]}
     *
     * @param inmueble: Inmueble a enviar
     */
    fun enviarInmueble(inmueble: Inmueble){
        processor.onNext(inmueble)
    }

    /**
     * Enviamos el conjunto de inmuebles pasados por
     * parámetro a través del {[processor]}
     */
    fun enviarRepositorioInmueble(repositorioInmueble: RepositorioInmueble<Inmueble>){
        repositorioInmueble.obtenerInmueblesAlmacenados().forEach {inmueble ->
            enviarInmueble(inmueble)
        }
    }

}