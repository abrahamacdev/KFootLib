package Modelo

import Utiles.TIPOS_CONTRATOS

open class Inmueble() {

    private var calle: String = ""
    private var ciudad: String = ""
    private var m2: Int = -1
    private var precio: Double = -1.0
    private var moneda: String = ""
    private var contrato: String = TIPOS_CONTRATOS.TIPO_CONTRATO_DESCONOCIDO.value
    private var numTelefono: String = ""
    private var urlDetalle: String = ""


    constructor(calle: String, ciudad: String, m2: Int, precio: Double, moneda: String, contrato: TIPOS_CONTRATOS, numTelefono: String, urlDetalle: String) : this() {
        this.calle = calle
        this.ciudad = ciudad
        this.m2 = m2
        this.precio = precio
        this.moneda = moneda
        this.contrato = contrato.value
        this.numTelefono = numTelefono
        this.urlDetalle = urlDetalle
    }

    companion object {

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

    }

    override fun toString(): String {
        return "Inmueble localizado en $ciudad. Tiene ${m2}m2 y cuesta ${precio}$moneda"
    }
}