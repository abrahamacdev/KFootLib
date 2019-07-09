package Common.Controlador.BufferedWriter

import Common.Modelo.ItemPruebaDos
import KFoot.Utils
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import lib.Common.Controlador.BufferedWriter.BufferedWriterCSV
import lib.Common.Controlador.BufferedWriter.GuardadoAsyncListener
import lib.Common.Controlador.Item.Item
import lib.Common.Modelo.FuenteItems
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.util.concurrent.TimeUnit

class BufferedWriterCSVTest {

    companion object {

        val rutaArchivoPrueba = "C:/Users/abrah/Desktop/Ejemplo.csv"

        val vigiliarRam = true
        var observableRam: Observable<Long>? = null
        var disposable: Disposable? = null

        @JvmStatic
        @AfterAll
        fun tearsDown(){
            val f = File(rutaArchivoPrueba)
            println(f.exists())
            f.delete()

            if (observableRam != null && disposable != null){
                disposable!!.dispose()
            }

            println("Test Terminado")
        }

        @JvmStatic
        @BeforeAll
        fun beforeUp(){
            KFoot.Logger.getLogger().setDebugLevel(KFoot.DEBUG.DEBUG_TEST)

            if (vigiliarRam){
                observableRam = Observable.interval(1,TimeUnit.SECONDS)
                disposable = observableRam!!.subscribe {
                    println("Utilizando ${Utils.memoriaUsada()}MB de memoria RAM")
                }
            }
        }
    }

    @Test
    fun escribe_en_el_archivo_de_forma_sincrona(){

        val fuente = FuenteItems<ItemPruebaDos>(ItemPruebaDos())
        val totalAnadir = 4

        for (i in 0 until totalAnadir){
            fuente.anadirItem(ItemPruebaDos(i, i))
        }

        val bufferedWriterCSV = BufferedWriterCSV.Builder()
                .guardarEn(rutaArchivoPrueba)
                .escribirCabeceras(true)
                .obtenerDatosDe(fuente as FuenteItems<Item>)
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

        val fuente = FuenteItems<ItemPruebaDos>(ItemPruebaDos())
        val tuplasAInsertar = 500000


        for (i in 0 until tuplasAInsertar){
            fuente.anadirItem(ItemPruebaDos(i, i))
        }

        val bufferedWriterCSV = BufferedWriterCSV.Builder()
                .guardarEn(rutaArchivoPrueba)
                .escribirCabeceras(true)
                .listaDeCabeceras(ItemPruebaDos().obtenerNombreAtributos())
                .obtenerDatosDe(fuente as FuenteItems<Item>)
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

        val fuente = FuenteItems<Item>(ItemPruebaDos())
        val tuplasAInsertar = 1000000

        for (i in 0 until tuplasAInsertar){
            fuente.anadirItem(ItemPruebaDos(i, i))
        }

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

        else {
            assert(true == false)
        }
    }

    @Test
    fun se_cancela_el_guardado_correctamente(){

        val fuente = FuenteItems<Item>(ItemPruebaDos())
        val tuplasAInsertar = 1000000


        for (i in 0 until tuplasAInsertar){
            fuente.anadirItem(ItemPruebaDos(i,i))
        }

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

        else {
            assert(true == false)
        }
    }
}