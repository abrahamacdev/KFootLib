package Common.Modelo

import lib.Common.Modelo.FuenteDatos
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import tech.tablesaw.api.IntColumn
import tech.tablesaw.api.StringColumn
import tech.tablesaw.api.Table

class FuenteDatosTest {

    @Test
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

        val fuente = FuenteDatos(table)

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
    }

}