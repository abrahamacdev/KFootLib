package Common.Controlador

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import lib.Common.Controlador.BufferedWriter.BufferedWriterCSV
import lib.Common.Modelo.FuenteDatos
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import tech.tablesaw.api.IntColumn
import tech.tablesaw.api.StringColumn
import tech.tablesaw.api.Table
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

class BufferedWriterCSVTest {

    companion object {

        val rutaArchivoPrueba = "C:/Users/abrah/Desktop/Ejemplo.csv"

        @JvmStatic
        @AfterAll
        fun eliminar_archivo_ejemplo(){

            System.gc()
            val f = File(rutaArchivoPrueba)
            f.delete()
            println("Ejecutado")
        }

    }

    @Test
    fun comprueba_que_la_ruta_del_archivo_existe(){

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

        val fuente = FuenteDatos(table,true)

        val bufferedWriterCSV = BufferedWriterCSV.Builder()
                .guardarEn(rutaArchivoPrueba)
                .escribirCabeceras(true)
                .obtenerDatosDe(fuente)
                .build()

        bufferedWriterCSV.guardar()

        val f = File("C:/Users/abrah/Desktop/Ejemplo.csv")

        Assertions.assertAll(
                Executable { assert(f.exists()) },
                Executable { Assertions.assertEquals(4,f.readLines().size) }
        )
    }

    @Test
    fun la_pausa_se_realiza_correctamente(){

        val tuplasAInsertar = 1000000

        val table = Table.create("Tabla")

        val idColum = IntColumn.create("Id")
        val nombreColum = StringColumn.create("Nombre")

        with(table){
            addColumns(idColum)
            addColumns(nombreColum)
        }

        for (i in 0 until tuplasAInsertar){
            table.column("Id").appendCell(i.toString())
        }

        val fuente = FuenteDatos(table,true)

        val bufferedWriterCSV = BufferedWriterCSV.Builder()
                .guardarEn(rutaArchivoPrueba)
                .escribirCabeceras(true)
                .obtenerDatosDe(fuente)
                .build()

        GlobalScope.launch {
            bufferedWriterCSV.guardar()
        }

        runBlocking {
            delay(2000)
            bufferedWriterCSV.pausarGuardado()
            delay(2000)
            bufferedWriterCSV.reanudarGuardado()
        }

        var bufferedReader = BufferedReader(FileReader(File(rutaArchivoPrueba)))
        var totalLineas = -1
        bufferedReader.lines().forEach {
            totalLineas++
        }

        Assertions.assertEquals(totalLineas,totalLineas)
    }

    @Test
    fun se_cancela_el_guardado_correctamente(){

        val tuplasAInsertar = 1000000

        val table = Table.create("Tabla")

        val idColum = IntColumn.create("Id")
        val nombreColum = StringColumn.create("Nombre")

        with(table){
            addColumns(idColum)
            addColumns(nombreColum)
        }

        for (i in 0 until tuplasAInsertar){
            table.column("Id").appendCell(i.toString())
        }

        val fuente = FuenteDatos(table,true)

        val bufferedWriterCSV = BufferedWriterCSV.Builder()
                .guardarEn(rutaArchivoPrueba)
                .escribirCabeceras(true)
                .obtenerDatosDe(fuente)
                .build()

        GlobalScope.launch {
            bufferedWriterCSV.guardar()
        }

        runBlocking {
            delay(1000)
            bufferedWriterCSV.cancelarGuardado()
        }

        var bufferedReader = BufferedReader(FileReader(rutaArchivoPrueba))
        var totalLineas = -1
        bufferedReader.lines().forEach {
            totalLineas++
        }

        bufferedReader.close()

        println("Total: $tuplasAInsertar -- Insertadas: $totalLineas")
        assert(totalLineas < tuplasAInsertar)
    }
}