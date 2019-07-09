package Common.Controlador.Item

import Common.Controlador.BufferedWriter.BufferedWriterCSVTest
import Common.Modelo.FuenteItemsTest
import Common.Modelo.ItemPrueba
import Common.Modelo.ItemPruebaDos
import Common.Modelo.ItemPruebaTres
import KFoot.Constantes
import KFoot.Utils
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import lib.Common.Controlador.Item.ConfiguracionRepositorioItems
import lib.Common.Controlador.Item.Item
import lib.Common.Controlador.Item.RepositorioItems
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.io.*
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.sign
import kotlin.system.measureTimeMillis

class RepositorioItemsTest {

    companion object {

        val ruta = "C:/Users/abrah/Desktop"
        val nombreArchivo = "Prueba"
        val extension = Constantes.EXTENSIONES_ARCHIVOS.csv
        val f = File("$ruta/${nombreArchivo}.${extension.name}")

        val vigiliarRam = false
        var observableRam: Observable<Long>? = null
        var disposable: Disposable? = null

        val configuracion = ConfiguracionRepositorioItems(rutaGuardadoArchivos = ruta, nombreArchivo = nombreArchivo, extensionArchivo = extension)

        @JvmStatic
        @AfterAll
        fun finalizar(){

            if (BufferedWriterCSVTest.observableRam != null && BufferedWriterCSVTest.disposable != null){
                BufferedWriterCSVTest.disposable!!.dispose()
            }

            f.delete()
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

    @Test
    fun guarda_en_csv_correctamente(){

        // Creamos una lista de items
        val item1 = ItemPrueba("Vivienda", "Calle Sol")
        val item2 = ItemPrueba("Garaje", "Calle Luna")
        val listaItems = listOf<ItemPrueba>(item1,item2)

        // Creamos un repositorio de items de prueba
        val repositorio = RepositorioItems.create<ItemPrueba>(listaItems, configuracion)

        // Añadimos un item más al repositorio
        val item3 = ItemPrueba("Terreno", "Calle Júpiter")
        repositorio.anadirItem(item3)

        // Comenzamos un guardado asíncrono
        repositorio.guardarAsync()

        // Esperamos a que se termine el guardado
        repositorio.esperarFinalizacionGuardado()

        // Contamos las líneas del archivo, con las cacereas incluidas
        var lineas = 0
        val buffered = BufferedReader(InputStreamReader(FileInputStream(f)))
        var texto: String? = buffered.readLine()
        while (texto != null){
            lineas++
            texto = buffered.readLine()
        }
        buffered.close()

        // Comprobamos que haya 4 líneas correspondientes a las cabeceras
        // y los 3 items de prueba creados
        assert(lineas==4)
    }

    @Test
    fun guarda_en_csv_con_cambio_de_tipo_correctamente(){

        // Creamos unos items de prueba
        val item1 = ItemPrueba("Vivienda", "Calle Sol")
        val item2 = ItemPruebaDos(100000, 2)

        // Creamos un repositorio
        val repositorio = RepositorioItems.create<ItemPrueba>(propiedades =  configuracion)

        // Añadimos los items al repositorio
        repositorio.anadirItem(item1)
        repositorio.anadirItem(item2)

        // Comenzamos un guardado asíncrono
        repositorio.guardarAsync()

        // Esperamos a que se termine de guardar los datos
        repositorio.esperarFinalizacionGuardado()

        // Contamos las líneas que hemos escrito
        var lineas = 0
        val buffered = BufferedReader(InputStreamReader(FileInputStream(f)))
        var texto: String? = buffered.readLine()
        while (texto != null){
            lineas++
            texto = buffered.readLine()
        }
        buffered.close()

        // Comprobamos que haya 3 líneas correspondientes a las cabeceras
        // y los 2 items de prueba creados
        assert(lineas==3)
    }

    @Test
    fun cambia_de_item_correctamente(){

        // Creamos unos items de prueba
        val itemPrueba = ItemPrueba("Vivienda", "Calle Sol")
        val itemPruebaDos = ItemPruebaDos(100000, 2)

        // Creamos un repositorio
        val repositorio = RepositorioItems.create<ItemPrueba>(propiedades = configuracion)

        // Añadimos los items al repositorio
        repositorio.anadirItem(itemPrueba)
        repositorio.anadirItem(itemPruebaDos)

        assert(repositorio.getTipoActual() == itemPruebaDos.javaClass && repositorio.getCount() == 2)
    }

    @Test
    fun cambia_de_item_una_sola_vez(){

        val itemPrueba = ItemPrueba("Vivienda", "Calle Sol")
        val itemPruebaDos = ItemPruebaDos(100000, 2)
        val itemPruebaTres = ItemPruebaTres("Calle Luna", 10000.0)

        val repositorio = RepositorioItems.create<ItemPrueba>(propiedades = configuracion)

        repositorio.anadirItem(itemPrueba)
        repositorio.anadirItem(itemPruebaDos)
        repositorio.anadirItem(itemPruebaTres)

        assert(repositorio.getTipoActual() == itemPruebaDos.javaClass && repositorio.getCount() == 2)

    }

    @Test
    fun benchmark(){

        val repositorio = RepositorioItems.create<ItemPrueba>(propiedades = configuracion)
        val total = 1000000

        for (i in 0 until total){
            repositorio.anadirItem(ItemPruebaDos(i,i))
        }

        System.gc()

        var maxRam: Long = -1
        Observable.interval(100,TimeUnit.MILLISECONDS).subscribe{
            val temp = Utils.memoriaUsada()

            if (maxRam < temp){
                maxRam = temp
            }
        }

        val t = measureTimeMillis {

            repositorio.guardarAsync()

            repositorio.esperarFinalizacionGuardado()
        }

        val reader = BufferedReader(FileReader(f))
        var lineas = 0
        var text = reader.readLine()
        while (text != null){

            lineas++
            text = reader.readLine()
        }
        reader.close()

        if (lineas-1 == total){
            println("Se ha guardado ${total} items en ${t}ms (${t / 1000.0}s). El máximo de memoria RAM consumida ascendía a $maxRam MB")
        }
    }
}