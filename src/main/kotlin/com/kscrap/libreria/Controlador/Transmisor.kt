package com.kscrap.libreria.Controlador

import com.kscrap.libreria.Modelo.Dominio.Inmueble
import com.kscrap.libreria.Modelo.Repositorio.RepositorioInmueble
import io.reactivex.processors.PublishProcessor
import nonapi.io.github.classgraph.utils.ReflectionUtils
import org.reactivestreams.Subscriber
import java.lang.reflect.ParameterizedType

/**
 * Un transmisor es la manera que tenemos de enviar la información a la
 * plataforma de forma predefinida. De momento esta clase no tiene muchos
 * métodos pero nos servirá para crear un método por defecto para la comunicación
 * entre los plugins y la plataforma.
 */
class Transmisor<T: Inmueble>{

    private lateinit var processor: PublishProcessor<T>
    private var numSubscriptores: Int = 0
    private val maxNumSubscriptores: Int = 1
    private lateinit var clazz: Class<T>
    private lateinit var tipo: T

    companion object {

        inline fun <reified T: Inmueble> crear(): Transmisor<T> {
            return Transmisor<T>(T::class.java)
        }

    }

    constructor(clazz: Class<T>){
        this.clazz = clazz
        processor = PublishProcessor.create()
    }

    /**
     * Enviamos el inmueble pasado por parámetro
     * a través del {[processor]}
     *
     * @param inmueble: Inmueble a enviar
     */
    fun enviarInmueble(inmueble: T){
        processor.onNext(inmueble)
    }

    /**
     * Enviamos el conjunto de inmuebles pasados por
     * parámetro a través del {[processor]}
     */
    fun enviarRepositorioInmueble(repositorioInmueble: RepositorioInmueble<T>){
        repositorioInmueble.obtenerInmueblesAlmacenados().forEach {inmueble ->
            enviarInmueble(inmueble as T)
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
    fun subscribirse(subscriptor: Subscriber<in T>){
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

    /**
     * Retornamos el tipo del {[Transmisor]}
     *
     * @return Claas<T>: Inmueble almacenado en el transmisor
     */
    fun getTipoTransmisor(): Class<T>{
        return clazz
    }
}