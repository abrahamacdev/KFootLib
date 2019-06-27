package Common.Controlador.Item

import lib.Common.Controlador.Item.Item

class ItemPrueba: Item {

    private var tipo = ""
    private var calle = ""

    constructor()

    constructor(tipo: String, calle: String){
        this.tipo = tipo
        this.calle = calle
    }
}