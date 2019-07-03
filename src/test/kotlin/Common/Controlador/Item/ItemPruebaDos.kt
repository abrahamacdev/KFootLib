package Common.Controlador.Item

import lib.Common.Controlador.Item.Item

class ItemPruebaDos: ItemPrueba {

    var precio = -1
    var numHab = -1

    constructor()

    constructor(precio: Int, numHab: Int){
        this.precio = precio
        this.numHab = numHab
    }

    override fun toString(): String {
        return "(ItemPruebaDos) Precio: $precio, NumHab: $numHab, Tipo: ${super.tipo}, Calle: ${super.calle}"
    }
}