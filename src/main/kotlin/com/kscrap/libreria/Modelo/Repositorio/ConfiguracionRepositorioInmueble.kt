package com.kscrap.libreria.Modelo.Repositorio

import com.andreapivetta.kolor.Color
import com.kscrap.libreria.Utiles.Constantes
import com.kscrap.libreria.Utiles.Utils
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

/**
 * {[guardadoAutomatico]} ->    Permite ejecutar un guardado periodicamente
 * {[intervalos]} ->            Cada cuanto tiempo se ejecutará el guardado automático (se necesita activar "guardadoAutomatico"). Por defecto se guardará cada 30 segundos
 * {[unidadTiempo]} ->          Unidad de tiempo a emplear para las emisiones periódicas (se necesita activar "guardadoAutomatico"). Por defecto se guardará cada 30 segundos
 * {[rutaGuardadoArchivos]} ->  Ruta en la que se guardará los archivos. Por defecto se guardará en el directorio "Documentos"
 * {[nombreArchivo]} ->         Nombre que tendra el archivo. Por defecto se le asignará uno si el valor de este es "null"
 * {[extensionArchivo]} ->             Extensión que usará el archivo. Por defecto será "csv"
 */
class ConfiguracionRepositorioInmueble(private var rutaGuardadoArchivos: String? = Utils.obtenerDirDocumentos(),
                  private var nombreArchivo: String? = null, private var extensionArchivo: Constantes.EXTENSIONES_ARCHIVOS = Constantes.EXTENSIONES_ARCHIVOS.csv) {

    init {

        // Comprobamos que la ruta de guardado de los archivos sea válida
        if (rutaGuardadoArchivos != null){
            if (!rutaDeGuardadoValida(rutaGuardadoArchivos!!)){
                Utils.debug(Constantes.DEBUG.DEBUG_SIMPLE,"La ruta proporcionada para el almacenamiento de los inmuebles no es válida. " +
                        "Se usará $rutaGuardadoArchivos", Color.RED)
            }
        }

        // Obtenemos un nombre por defecto para el archivo si no se ha especificado uno
        if (nombreArchivo == null){
            nombreArchivo = obtenerNombreArchivoDefecto()
        }

    }
    /**
     * Establecemos la ruta en la que se guardará
     * el {[RepositorioInmueble]}
     *
     * @param ruta String: Ruta a utilizar para el guardado
     */
    fun guardaLosDatosEn(ruta: String){

        // Comprobamos que la ruta sea válida
        if (rutaDeGuardadoValida(ruta)){

            // Eliminamos la "/" final
            if (ruta.endsWith("/")){
                rutaGuardadoArchivos = ruta.removeRange(ruta.length - 1, ruta.length)
                return
            }

            rutaGuardadoArchivos = ruta
        }

        // Mostramos un mensaje de error
        else {
            Utils.debug(Constantes.DEBUG.DEBUG_SIMPLE,"La ruta proporcionada para el almacenamiento de los inmuebles no es válida. " +
                    "Se usará $rutaGuardadoArchivos", Color.RED)
        }
    }

    /**
     * Establecemos el nombre que tendrá el archivo
     *
     * @param nombreArchivo: Nombre que se utilizará para nombrar al archivo
     */
    fun archivoConNombre(nombreArchivo: String){

        // Comprobamos que el nombre del archivo sea válido
        if (nombreArchivoValido(nombreArchivo)){
            this.nombreArchivo = nombreArchivo
        }

        // Mostramos un mensaje de error
        else {
            Utils.debug(Constantes.DEBUG.DEBUG_SIMPLE, "El nombre del archivo en el que se guardarán los inmuebles no es válido", Color.RED)
        }
    }

    /**
     * Establecemos la extensión que tendrá el archivo
     *
     * @param extension: Extensión que tendrá el archivo
     */
    fun archivoConExtension(extension: Constantes.EXTENSIONES_ARCHIVOS){
        this.extensionArchivo = extension
    }

    /**
     * Comprobamos si los intervalos que se setearán para el guardado automático son
     * válidos
     *
     * @param intervalos: Cantidada de tiempo entre ticks
     * @param unidadTiempo: Unidad de tiempo que se utilizará para emitir los ticks
     *
     * @return Boolean: Si los intervalos a setear son válidos
     */
    private fun intervalosGuardadoAutValidos(intervalos: Long, unidadTiempo: TimeUnit): Boolean{
        if ((intervalos < 3000000000 && unidadTiempo == TimeUnit.NANOSECONDS) || (intervalos < 3000 && unidadTiempo == TimeUnit.MILLISECONDS) ){
            return false
        }

        return true
    }

    /**
     * Comprobamos que la ruta en la que se guardará el archivo es válida
     *
     * @param rutaGuardadoArchivos: Ruta en la que se guardarán los archivos
     *
     * @return Boolean: Si la ruta es válida
     */
    private fun rutaDeGuardadoValida(ruta: String): Boolean{
        val dir = File(ruta)
        return dir.exists() && dir.isDirectory
    }

    /**
     * Comprobamos si el nombre del archivo es válido
     *
     * @param nombreArchivo: Nombre que se usará para el archivo
     *
     * @return Boolean: Si el nombre del archivo es válido
     */
    private fun nombreArchivoValido(nombreArchivo: String): Boolean {

        val patron = Regex("^\\w+(?:[-](?:\\w|[_])+)*\$")

        return nombreArchivo.matches(patron)
    }

    /**
     * Obtenemos un nombre por defecto para el archivo en caso de que no
     * se proporcione uno al crear el objeto
     *
     * @return String: Nombre del archivo por defecto
     */
    private fun obtenerNombreArchivoDefecto(): String{
        val dtf: DateTimeFormatter = DateTimeFormatter.ofPattern("dd_MM_HH_mm")
        val ahora: LocalDateTime = LocalDateTime.now()
        return "KScrap_${dtf.format(ahora)}"
    }

    fun getRutaGuardadoArchivos(): String?{
        return this.rutaGuardadoArchivos
    }

    fun getNombreArchivo(): String?{
        return this.nombreArchivo
    }

    fun getExtensionArchivo(): Constantes.EXTENSIONES_ARCHIVOS{
        return this.extensionArchivo
    }
}