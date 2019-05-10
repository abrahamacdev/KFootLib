package com.kscrap.libreria

import com.kscrap.libreria.Modelo.Dominio.Inmueble
import com.kscrap.libreria.Utiles.TIPOS_CONTRATOS

class Vivienda: Inmueble {

    var numHabitaciones = -1

    constructor()

    constructor(calle: String, ciudad: String, m2: Int, precio: Double, moneda: String, contrato: TIPOS_CONTRATOS, numTelefono: String, urlDetalle: String, numHabitaciones: Int): super(calle,ciudad,m2,precio,moneda,contrato,numTelefono,urlDetalle){
        this.numHabitaciones = numHabitaciones
    }

    override fun toString(): String {
        return super.toString()
    }
}