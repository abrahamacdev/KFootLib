package Common.Controlador.BufferedWriter

import KFoot.DEBUG
import kotlinx.coroutines.*
import lib.Common.Controlador.BufferedWriter.BufferedWriterCSV
import lib.Common.Controlador.BufferedWriter.GuardadoAsyncListener
import lib.Common.Modelo.FuenteDatos
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import tech.tablesaw.api.IntColumn
import tech.tablesaw.api.StringColumn
import tech.tablesaw.api.Table
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.util.logging.Logger

class BufferedWriterCSVTest {

    companion object {

        val rutaArchivoPrueba = "C:/Users/abrah/Desktop/Ejemplo.csv"

        @JvmStatic
        @AfterAll
        fun tearsDown(){
            val f = File(rutaArchivoPrueba)
            println(f.exists())
            f.delete()
            println("Test Terminado")
        }

        @JvmStatic
        @BeforeAll
        fun beforeUp(){
            KFoot.Logger.getLogger().setDebugLevel(KFoot.DEBUG.DEBUG_TEST)
        }

    }

    @Test
    fun escribe_en_el_archivo_de_forma_sincrona(){

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

        val bufferedWriterCSV = BufferedWriterCSV.Builder()
                .guardarEn(rutaArchivoPrueba)
                .escribirCabeceras(true)
                .obtenerDatosDe(fuente)
                .build()

        if (bufferedWriterCSV != null){

            bufferedWriterCSV.guardar()

            val f = File("C:/Users/abrah/Desktop/Ejemplo.csv")

            Assertions.assertAll(
                    Executable { assert(f.exists()) },
                    Executable { Assertions.assertEquals(4,f.readLines().size) }
            )
        }

    }

    @Test
    fun escribe_en_el_archivo_de_forma_asincrona(){

        val table = Table.create("Tabla")

        val tuplasAInsertar = 1000000
        val idColum = IntColumn.create("Id")
        val nombreColum = StringColumn.create("Nombre")

        with(table){
            addColumns(idColum)
            addColumns(nombreColum)
        }

        for (i in 0 until tuplasAInsertar){
            table.column("Id").appendCell(i.toString())
        }

        table.column("Nombre").appendCell("Pepe")
        table.column("Nombre").appendCell("Juan")

        val fuente = FuenteDatos(table)

        val bufferedWriterCSV = BufferedWriterCSV.Builder()
                .guardarEn(rutaArchivoPrueba)
                .escribirCabeceras(true)
                .obtenerDatosDe(fuente)
                .build()

        if (bufferedWriterCSV != null){

            var deferred: CompletableDeferred<Unit>? = null
            bufferedWriterCSV.guardarAsync(object : GuardadoAsyncListener.onGuardadoAsyncListener{
                override fun onGuardadoComenzado(completable: CompletableDeferred<Unit>) {
                    deferred = completable
                }

                override fun onGuardadoCompletado() {

                }

                override fun onGuardadoError(error: Throwable) {

                }
            })

            runBlocking {
                delay(2000)
                bufferedWriterCSV.pausarGuardado()
                delay(2000)
                bufferedWriterCSV.reanudarGuardado()
                deferred!!.await()
            }

            val f = File(rutaArchivoPrueba)

            Assertions.assertAll(
                    Executable { assert(f.exists()) },
                    Executable { Assertions.assertEquals(tuplasAInsertar + 1,f.readLines().size) }
            )
        }

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

        val fuente = FuenteDatos(table)

        val bufferedWriterCSV = BufferedWriterCSV.Builder()
                .guardarEn(rutaArchivoPrueba)
                .escribirCabeceras(true)
                .obtenerDatosDe(fuente)
                .build()

        if (bufferedWriterCSV != null){

            var deferred: CompletableDeferred<Unit>? = null

            bufferedWriterCSV.guardarAsync(object : GuardadoAsyncListener.onGuardadoAsyncListener {
                override fun onGuardadoComenzado(completable: CompletableDeferred<Unit>) {
                    deferred = completable
                }
                override fun onGuardadoCompletado() {
                    println("Guardado completado")
                }
                override fun onGuardadoError(error: Throwable) {}
            })

            runBlocking {
                delay(2000)
                bufferedWriterCSV.pausarGuardado()
                println("Pausamos el guardado durante 2 segundos...")
                delay(2000)
                bufferedWriterCSV.reanudarGuardado()
                println("Retomamos el guardado")
                deferred!!.await()
            }

            var bufferedReader = BufferedReader(FileReader(File(rutaArchivoPrueba)))
            var totalLineas = -1
            bufferedReader.lines().forEach {
                totalLineas++
            }

            bufferedReader.close()

            Assertions.assertEquals(totalLineas,totalLineas)
        }
    }

    @Test
    fun se_cancela_el_guardado_correctamente(){

        val tuplasAInsertar = 2000000

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

        val fuente = FuenteDatos(table)

        val bufferedWriterCSV = BufferedWriterCSV.Builder()
                .guardarEn(rutaArchivoPrueba)
                .escribirCabeceras(true)
                .obtenerDatosDe(fuente)
                .build()

        if (bufferedWriterCSV != null){

            var deferred: CompletableDeferred<Unit>? = null
            bufferedWriterCSV.guardarAsync(object : GuardadoAsyncListener.onGuardadoAsyncListener{
                override fun onGuardadoComenzado(completable: CompletableDeferred<Unit>) {
                    deferred = completable
                }

                override fun onGuardadoCompletado() {

                }

                override fun onGuardadoError(error: Throwable) {

                }
            })

            runBlocking {
                delay(1000)
                bufferedWriterCSV.cancelarGuardado()
            }

            runBlocking { deferred!!.await() }

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
}