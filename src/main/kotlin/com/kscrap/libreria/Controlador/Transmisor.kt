package com.kscrap.libreria.Controlador

import com.kscrap.libreria.Modelo.Dominio.Inmueble
import com.kscrap.libreria.Modelo.Repositorio.RepositorioInmueble
import io.reactivex.processors.PublishProcessor
import org.reactivestreams.Subscriber

/**
 * Un transmisor es la manera que tenemos de enviar la información a la
 * plataforma de forma predefinida. De momento esta clase no tiene muchos
 * métodos pero nos servirá para crear un método por defecto para la comunicación
 * entre los plugins y la plataforma.
 */
class Transmisor<T: Inmueble>{

    private lateinit var processor: PublishProcessor<Inmueble>
    private var numSubscriptores: Int = 0
    private val maxNumSubscriptores: Int = 1
    private lateinit var clazz: Class<T>

    companion object {

        inline fun <reified T: Inmueble> crear(): Transmisor<T> {
            return Transmisor<T>(T::class.java)
        }

    }

    constructor(clazz: Class<T>){
        this.clazz = clazz
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

    /**
     * Enviamos un error a través del {[processor]}
     *
     * @param error: Error a transmitir
     */
    fun enviarError(error: Throwable) {
        processor.onError(error)
    }

    /**
     * Terminamos el envío de datos a través del {[processor]}
     */
    fun terminarEnvio(){
        processor.onComplete()
    }

    /**
     * Añadimos el subscriptor al {[processor]}.
     * *El número de subscriptores posibles está
     * limitado para evitar subscripciones innecesarias
     *
     * @param subscriptor: Subscriptor a subscribir
     */
    fun subscribirse(subscriptor: Subscriber<in Inmueble>){
        if (numSubscriptores < maxNumSubscriptores){
            processor.subscribe(subscriptor)
            numSubscriptores++
        }
    }

    /**
     * Retornamos si el {[processor]} aún transmite información
     * o ya ha terminado
     *
     * @return Boolean: Si la transmisión ya ha terminado
     */
    fun transmisionTerminada(): Boolean{
        return processor.hasComplete()
    }
}