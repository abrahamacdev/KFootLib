package Common.Controlador.Item

import KFoot.Constantes
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import lib.Common.Controlador.Item.ConfiguracionRepositorioItems
import lib.Common.Controlador.Item.RepositorioItems
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.io.*
import java.text.Normalizer

class RepositorioItemsTest {

    companion object {

        val ruta = "C:/Users/abrah/Desktop"
        val nombreArchivo = "Prueba"
        val extension = Constantes.EXTENSIONES_ARCHIVOS.csv
        val f = File("$ruta/${nombreArchivo}.${extension.name}")

        val configuracion = ConfiguracionRepositorioItems(rutaGuardadoArchivos = ruta, nombreArchivo = nombreArchivo, extensionArchivo = extension)

        @JvmStatic
        @AfterAll
        fun finalizar(){

            f.delete()
        }

        @JvmStatic
        @BeforeAll
        fun beforeUp(){
            KFoot.Logger.getLogger().setDebugLevel(KFoot.DEBUG.DEBUG_TEST)
        }

    }

    @Test
    fun guarda_en_csv_correctamente(){

        val item1 = ItemPrueba("Vivienda","Calle Sol")
        val item2 = ItemPrueba("Garaje","Calle Luna")
        val listaItems = listOf<ItemPrueba>(item1,item2)

        val repositorio = RepositorioItems.create<ItemPrueba>(listaItems, configuracion)

        val item3 = ItemPrueba("Terreno","Calle Júpiter")

        repositorio.anadirItem(item3)

        repositorio.guardarAsync()

        runBlocking {
            delay(1000)
        }

        val buffered = BufferedReader(InputStreamReader(FileInputStream(f)))

        var lineas = 0
        var texto: String? = buffered.readLine()
        while (texto != null){
            lineas++
            texto = buffered.readLine()
        }

        // Comprobamos que haya 4 líneas correspondientes a las cabeceras
        // y los 3 items de prueba creados
        assert(lineas==4)
    }

    @Test
    fun guarda_en_csv_con_cambio_de_tipo_correctamente(){

        val item1 = ItemPrueba("Vivienda","Calle Sol")
        val item2 = ItemPruebaDos(100000,2)

        val repositorio = RepositorioItems.create<ItemPrueba>(propiedades =  configuracion)

        repositorio.anadirItem(item1)
        repositorio.anadirItem(item2)

        repositorio.guardarAsync()

        repositorio.esperarFinalizacionGuardado()

        val buffered = BufferedReader(InputStreamReader(FileInputStream(f)))

        var lineas = 0
        var campos = 0
        var texto: String? = buffered.readLine()
        campos = texto!!.split(",").size
        while (texto != null){
            lineas++
            texto = buffered.readLine()
        }

        // Comprobamos que haya 3 líneas correspondientes a las cabeceras
        // y los 2 items de prueba creados
        assert(lineas==2)
    }


    @Test
    fun cambia_de_item_correctamente(){

        val itemPrueba = ItemPrueba("Vivienda","Calle Sol")
        val itemPruebaDos = ItemPruebaDos(100000,2)

        val repositorio = RepositorioItems.create<ItemPrueba>(propiedades = configuracion)

        repositorio.anadirItem(itemPrueba)

        repositorio.anadirItem(itemPruebaDos)

        assert(repositorio.getTipoActual() == itemPruebaDos.javaClass && repositorio.getCount() == 2)
    }

    @Test
    fun cambia_de_item_una_sola_vez(){

        val itemPrueba = ItemPrueba("Vivienda","Calle Sol")
        val itemPruebaDos = ItemPruebaDos(100000,2)
        val itemPruebaTres = ItemPruebaTres("Calle Luna", 10000.0)

        val repositorio = RepositorioItems.create<ItemPrueba>(propiedades = configuracion)

        repositorio.anadirItem(itemPrueba)
        repositorio.anadirItem(itemPruebaDos)
        repositorio.anadirItem(itemPruebaTres)

        assert(repositorio.getTipoActual() == itemPruebaDos.javaClass && repositorio.getCount() == 2)

    }
}