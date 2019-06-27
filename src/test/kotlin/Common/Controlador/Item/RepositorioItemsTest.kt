package Common.Controlador.Item

import KFoot.Constantes
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import lib.Common.Controlador.Item.ConfiguracionRepositorioItems
import lib.Common.Controlador.Item.RepositorioItems
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import java.io.*
import java.text.Normalizer

class RepositorioItemsTest {

    companion object {

        val ruta = "C:/Users/abrah/Desktop"
        val nombreArchivo = "Prueba"
        val extension = Constantes.EXTENSIONES_ARCHIVOS.csv
        val f = File("$ruta/${nombreArchivo}.${extension.name}")

        @JvmStatic
        @AfterAll
        fun finalizar(){

            f.delete()
        }

    }

    @Test
    fun guarda_en_csv_correctamente(){

        val configuacion = ConfiguracionRepositorioItems(rutaGuardadoArchivos = ruta, nombreArchivo = nombreArchivo, extensionArchivo = extension)

        val item1 = ItemPrueba("Vivienda","Calle Sol")
        val item2 = ItemPrueba("Garaje","Calle Luna")
        val listaItems = listOf<ItemPrueba>(item1,item2)

        val repositorio = RepositorioItems.create<ItemPrueba>(listaItems,configuacion)

        val item3 = ItemPrueba("Terreno","Calle Júpiter")

        repositorio.anadirItem(item3)

        repositorio.guardar()

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

        assert(lineas==4)
    }

}