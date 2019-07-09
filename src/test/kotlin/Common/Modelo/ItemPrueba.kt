package Common.Modelo

import lib.Common.Controlador.Item.Item

open class ItemPrueba: Item {

    var tipo = "Desconocido"
    var calle = "Desconocida"

    constructor()

    constructor(tipo: String, calle: String){
        this.tipo = tipo
        this.calle = calle
    }

    override fun toString(): String {
        return "(ItemPrueba) Tipo: $tipo, Calle: $calle"
    }
}