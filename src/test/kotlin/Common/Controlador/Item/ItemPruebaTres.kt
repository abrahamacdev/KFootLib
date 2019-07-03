package Common.Controlador.Item

class ItemPruebaTres: ItemPrueba {

    private var localicacion = ""
    private var precio = -1.0

    constructor()

    constructor(localizacion: String, precio: Double){
        this.precio = precio
        this.localicacion = localicacion
    }

    override fun toString(): String {
        return "(ItemPruebaTres) Precio: $precio, NumHab: $localicacion, Tipo: ${super.tipo}, Calle: ${super.calle}"
    }
}