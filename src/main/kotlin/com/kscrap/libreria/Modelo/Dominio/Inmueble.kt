package com.kscrap.libreria.Modelo.Dominio

import com.kscrap.libreria.Utiles.TIPOS_CONTRATOS

/**
 * Esta clase ofrece un modelo básico a seguir por los inmuebles
 * más avanzados que se vayan a crear.
 * * Es <b>obligatorio</b> que todos los inmuebles que hereden esta clase
 * tengan un constructor vacío.
 * ** Es <b>obligatorio</b> que todos los inmuebles que hereden esta clase
 * tengan variables que solo guarden datos referente al inmueble.
 */
open class Inmueble {

    var calle: String = ""
    var ciudad: String = ""
    var m2: Int = -1
    var precio: Double = -1.0
    var moneda: String = ""
    var contrato: String = TIPOS_CONTRATOS.TIPO_CONTRATO_DESCONOCIDO.value
    var numTelefono: String = ""
    var urlDetalle: String = ""

    constructor()

    constructor(calle: String, ciudad: String, m2: Int, precio: Double, moneda: String, contrato: TIPOS_CONTRATOS, numTelefono: String, urlDetalle: String) {
        this.calle = calle
        this.ciudad = ciudad
        this.m2 = m2
        this.precio = precio
        this.moneda = moneda
        this.contrato = contrato.value
        this.numTelefono = numTelefono
        this.urlDetalle = urlDetalle
    }

    /**
     * Devuelve los nombres de las variables almacenadas en el objeto (calle,ciudad,m2...ect)
     *
     * @return List<String> lista con los nombres de las variables
     */
    fun obtenerNombreAtributos(): List<String> {
        return this.javaClass.declaredFields.map { it.name }
    }

    /**
     * Devolvemos el nombre de las variables almacenadas en el objeto junto
     * a su respectivo tipo
     *
     * @return List<Pair<String, Class<*>>> lista con el nombre de la variable y su tipo
     */
    fun obtenerNombreTipoAtributos(): List<Pair<String, Class<*>>>{
        return this.javaClass.declaredFields.map {
            Pair<String, Class<*>>(it.name, it.type)
        }
    }

    override fun toString(): String {
        return "Inmueble localizado en $ciudad. Tiene ${m2}m2 y cuesta ${precio}$moneda"
    }
}