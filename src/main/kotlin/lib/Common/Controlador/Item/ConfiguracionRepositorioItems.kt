package lib.Common.Controlador.Item

import KFoot.*
import com.andreapivetta.kolor.Color
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
class ConfiguracionRepositorioItems(private var rutaGuardadoArchivos: String? = KFoot.Utils.obtenerDirDocumentos(),
                                    private var nombreArchivo: String? = null, private var extensionArchivo: Constantes.EXTENSIONES_ARCHIVOS = Constantes.EXTENSIONES_ARCHIVOS.csv) {

    init {

        // Comprobamos que la ruta de guardado de los archivos sea válida
        if (rutaGuardadoArchivos != null){
            if (!rutaDeGuardadoValida(rutaGuardadoArchivos!!)){
                Logger.getLogger().debug(DEBUG.DEBUG_SIMPLE,"La ruta proporcionada para el almacenamiento de los inmuebles no es válida. " +
                        "Se usará $rutaGuardadoArchivos", IMPORTANCIA.ALTA)
            }
        }

        // Obtenemos un nombre por defecto para el archivo si no se ha especificado uno
        if (nombreArchivo == null){
            nombreArchivo = obtenerNombreArchivoDefecto()
        }

    }

    /**
     * Establecemos la ruta en la que se guardará
     * el {[lib.Plugin.Modelo.Dominio.Item]}
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
            Logger.getLogger().debug(DEBUG.DEBUG_SIMPLE,"La ruta proporcionada para el almacenamiento de los inmuebles no es válida. " +
                    "Se usará $rutaGuardadoArchivos", IMPORTANCIA.ALTA)
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
            Logger.getLogger().debug(DEBUG.DEBUG_SIMPLE, "El nombre del archivo en el que se guardarán los inmuebles no es válido", IMPORTANCIA.ALTA)
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
     * Establecemos la nueva ruta del archivo en el que
     * se guardaran los datos
     */
    fun establecerRutaArchivo(rutaCompleta: String){

        val ruta = File(rutaCompleta)

        if (rutaCompleta.matches(Regex("^(?:\\/[A-z_:()0-9]+)*\\.[A-z0-9:_-]+")) && File(ruta.parent).exists()){

            var spliteado = rutaCompleta.split("/")

            // Nueva ruta
            var rutaNueva = ""
            for (i in 0 until spliteado.size-1){
                rutaNueva += spliteado[i] + "/"
            }

            // Espliteamos el nombre del archivo y su extension
            spliteado = spliteado.get(spliteado.size - 1).split(".")

            // Nuevo nombre de archivo
            val nuevoNombre = spliteado.get(0)

            // Nueva extension
            val nuevaExtension = obtenerExtension(spliteado.get(1))
            if (nuevaExtension != null){
                extensionArchivo = nuevaExtension
                nombreArchivo = nuevoNombre
                rutaGuardadoArchivos = rutaNueva
            }
        }
    }



    /**
     * Obtenemos la extension que coincida con la pasada por parametro
     *
     * @param extension: Extension a comprobar si existe
     *
     * @return EXTENSIONES_ARCHIVOS?: La extension en caso de que esta exista
     */
    private fun obtenerExtension(extension: String): Constantes.EXTENSIONES_ARCHIVOS?{

        // COmprobamos si existe alguna extension con el nombre que nos pasan por parametro
        val extensionExistente = Constantes.EXTENSIONES_ARCHIVOS.values().firstOrNull{
            extension.equals(it.name)
        }

        return extensionExistente
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
        if ((intervalos < 3000000000 && unidadTiempo == TimeUnit.NANOSECONDS) || (intervalos < 3000 && unidadTiempo == TimeUnit.MILLISECONDS) || (intervalos < 3 && unidadTiempo == TimeUnit.SECONDS)){
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
        val dtf: DateTimeFormatter = DateTimeFormatter.ofPattern("dd_MM_YYYY_HHmmss")
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