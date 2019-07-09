package Common.Modelo

import Common.Controlador.BufferedWriter.BufferedWriterCSVTest
import KFoot.Utils
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import lib.Common.Modelo.FuenteItems
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit

class FuenteItemsTest {

    companion object {

        val vigiliarRam = true
        var observableRam: Observable<Long>? = null
        var disposable: Disposable? = null

        @JvmStatic
        @AfterAll
        fun finalizar(){

            if (BufferedWriterCSVTest.observableRam != null && BufferedWriterCSVTest.disposable != null){
                BufferedWriterCSVTest.disposable!!.dispose()
            }

        }

        @JvmStatic
        @BeforeAll
        fun beforeUp(){
            KFoot.Logger.getLogger().setDebugLevel(KFoot.DEBUG.DEBUG_TEST)

            if (vigiliarRam){
                observableRam = Observable.interval(1, TimeUnit.SECONDS)
                disposable = observableRam!!.subscribe {
                    println("Utilizando ${Utils.memoriaUsada()}MB de memoria RAM")
                }
            }
        }

    }

    /*@Test
    fun crea_fuente_de_datos_a_partir_de_una_tabla(){

        val table = Table.create("Tabla")

        val idColum = IntColumn.create("Id")
        val nombreColum = StringColumn.create("Nombre")

        with(table){
            addColumns(idColum)
            addColumns(nombreColum)
        }

        table.column("Id").appendCell("1")
        table.column("Id").appendCell("2")
        table.column("Id").appendCell("3")

        table.column("Nombre").appendCell("Pepe")
        table.column("Nombre").appendCell("Juan")

        val fuente = FuenteItems(table)

        var  i = 0

        while (fuente.hayMasFilas()){

            val fila = fuente.siguienteFila()
            if (fila != null){
                val id = fila.getOrNull(0)
                val nombre = fila.getOrNull(1)
                when {
                    i == 0 -> {
                        Assertions.assertEquals("1",id)
                        Assertions.assertEquals("Pepe",nombre)
                    }
                    i == 1 -> {
                        Assertions.assertEquals("2",id)
                        Assertions.assertEquals("Juan",nombre)
                    }

                    i == 2 -> {
                        Assertions.assertEquals("3",id)
                        Assertions.assertEquals("",nombre)
                    }
                }
                i++
            }
        }
    }*/

    @Test
    fun se_agregan_items_de_forma_correcta(){

        val f = FuenteItems<ItemPruebaDos>(ItemPruebaDos())

        var numItems = 0
        var totalItems = 5


        for (i in 0 until totalItems){

            f.anadirItem(ItemPruebaDos(i, i))
        }

        while (f.hayMasFilas()){
            numItems++
            f.siguienteFila()
            //println(Arrays.toString(f.siguienteFila()!!.toArray()))
        }

        assert(numItems == totalItems)
    }

    @Test
    fun la_fuente_de_datos_no_supera_150_MB(){

        val f = FuenteItems<ItemPruebaDos>(ItemPruebaDos())
        val cantidad = 1000000
        var limiteRam = 150

        for (i in 0 until cantidad){
            f.anadirItem(ItemPruebaDos(i, i))
        }

        assert(Utils.memoriaUsada() <= limiteRam, { println("La memoria utilizada superaba el límite (${Utils.memoriaUsada()}MB)") })
        println("La memoria total necesitada para añadir $cantidad de items asciende a ${Utils.memoriaUsada()}MB")
    }
}