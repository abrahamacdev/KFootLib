package Modelo

import Utiles.Constantes
import Utiles.Utils
import com.andreapivetta.kolor.Color
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * {[guardadoAutomatico]} ->    Permite ejecutar un guardado periodicamente
 * {[intervalos]} ->            Cada cuanto tiempo se ejecutará el guardado automático (se necesita activar "guardadoAutomatico"). Por defecto se guardará cada 30 segundos
 * {[unidadTiempo]} ->          Unidad de tiempo a emplear para las emisiones periódicas (se necesita activar "guardadoAutomatico"). Por defecto se guardará cada 30 segundos
 * {[rutaGuardadoArchivos]} ->  Ruta en la que se guardará los archivos. Por defecto se guardará en el directorio "Documentos"
 * {[nombreArchivo]} ->         Nombre que tendra el archivo. Por defecto se le asignará uno
 * {[extension]} ->             Extensión que usará el archivo. Por defecto será "csv"
 */
data class PropiedadesConjuntoInmueble(var guardadoAutomatico: Boolean = false, var intervalos: Int = 30,
                                       var unidadTiempo: TimeUnit = TimeUnit.SECONDS, var rutaGuardadoArchivos: String? = Utils.obtenerDirDocumentos(),
                                       var nombreArchivo: String? = null, var extensionArchivo: Constantes.EXTENSIONES_ARCHIVOS = Constantes.EXTENSIONES_ARCHIVOS.CSV) {

    /**
     * Activamos la opción de guardar de forma automática
     * la información del dataframe cada {[intervalos]}
     *
     * @param intervalos: Cada cuanto tiempo se guardará la información
     * @param timeUnit; Unidad de tiempo que se utilizara entre ticks
     */
    fun guardaCada(intervalos: Int = 30, unidadTiempo: TimeUnit = TimeUnit.SECONDS){
        this.guardadoAutomatico = true
        this.intervalos = intervalos
        this.unidadTiempo = unidadTiempo
    }

    /**
     * Establecemos la ruta en la que se guardará
     * el {[ConjuntoInmueble]}
     *
     * @param ruta String: Ruta a utilizar para el guardado
     */
    fun guardalosEn(ruta: String){

        val dir = File(ruta)

        if (dir.exists() && dir.isDirectory){
            rutaGuardadoArchivos = ruta
        }
        else {
            Utils.debug(Constantes.DEBUG.DEBUG_SIMPLE,"La ruta proporcionada para el almacenamiento de los inmuebles no es válida. " +
                    "Se usará $rutaGuardadoArchivos", Color.RED)
        }
    }

    /**
     * Guardamos el nombre del archivo y la extensión
     *
     * @param nombreArchivo: Nombre que tendrá el archivo
     * @param extension: Extensión con la que se guardará el archivo
     */
    fun conNombreYExtension(nombreArchivo: String, extension: Constantes.EXTENSIONES_ARCHIVOS = Constantes.EXTENSIONES_ARCHIVOS.CSV){

        if (nombreArchivo.matches(Regex("^\\w+(?:[-](?:\\w|[_])+)*\$"))){
            this.nombreArchivo = nombreArchivo
        }

        this.extensionArchivo = extension
    }
}