package lib.Common.Controlador.BufferedWriter

import KFoot.Constantes
import com.andreapivetta.kolor.Color
import lib.Common.Modelo.FuenteDatos
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

class BufferedWriterCSV: IBufferedWriter {

    // Ruta del archivo en el que se guardará la información
    private var archivo: File? = null

    // Si es escribirán las cabeceras en el archivo o no
    private var escribirCabeceras: Boolean = false

    // FuenteDatos de datos
    private var fuenteDatos: FuenteDatos? = null

    // Writer del archivo
    private var bufferedWriter: BufferedWriter? = null

    // Separador del archivo CSV
    private var separador = ","

    private var guardando = false
    private var pausado = false
    private var cancelado = false

    /**
     * Nos servirá para saber si ya hemos escrito las cabeceras
     * @see [escribirCabeceras]
    */
    private var cabeceraEscrita = false

    class Builder(){

        private var rutaArchivo: File? = null
        private var escribirCabeceras: Boolean = false
        private var fuenteDatos: FuenteDatos? = null
        private var separador = ","

        /**
         * Servirá para escribir las cabeceras en el archivo
         *
         * @param escribir: Si se escribirán las cabeceras en el archivo o no
         */
        fun escribirCabeceras(escribir: Boolean = false): Builder {
            this.escribirCabeceras = escribir
            return this
        }

        /**
         * Establecemos la ruta del archivo en el que
         * se guadarán los datos
         *
         * @param rutaArchivo: Ruta absoluta del archivo en el que se guardarán los datos
         */
        fun guardarEn(rutaArchivo: String): Builder {
            val f = File(rutaArchivo)

            // Es un archivo .csv
            if (f.absolutePath.endsWith(".csv")){

                // Comprobamos que la ruta del archivo existe
                var ruta = File(f.path.split(Regex("\\w*[-_#@]*\\.csv$"))[0])

                // Comprobamos que la ruta exista
                if (ruta.isDirectory && ruta.exists()){
                    this.rutaArchivo = File(rutaArchivo)
                }
                else {
                    KFoot.Utils.debug(Constantes.DEBUG.DEBUG_SIMPLE,"La ruta proporcionada para el guardado de los datos no es válida", Color.RED)
                }
            }
            return this
        }

        /**
         * Establecemos el separador que se usará en el
         * archivo CSV
         *
         * @param separador: Caracter que se usará como separador
         */
        fun usarComoSeparador(separador: Char){
            this.separador = separador.toString()
        }

        /**
         * Objeto del que extraeremos los datos para
         * guardarlos en el archivo CSV
         *
         * @param fuenteDatos: Fuente de datos
         */
        fun obtenerDatosDe(fuenteDatos: FuenteDatos): Builder {
            this.fuenteDatos = fuenteDatos
            return this
        }

        /**
         * Construimos el [BufferedWriterCSV] con el que
         * se guardarán los datos en el archivo csv
         *
         * @return BufferedWriterCSV: Objeto que se utilizará para guardar la información
         */
        fun build(): BufferedWriterCSV {
            return BufferedWriterCSV(this.fuenteDatos, this.rutaArchivo, this.escribirCabeceras, this.separador)
        }
    }

    private constructor(fuenteDatos: FuenteDatos?, rutaArchivo: File? = null, escribirCabeceras: Boolean = false, separador: String){
        this.archivo = rutaArchivo
        this.escribirCabeceras = escribirCabeceras
        this.separador = separador
        this.fuenteDatos = fuenteDatos
    }



    /**
     * Guardamos los datos almacenados en la [fuenteDatos]
     * en el archivo con ruta [archivo]
     */
    override fun guardar() {

        // Comprobamos si supera todas las comprobaciones previas al guardado
        if (superaComprobacionesPrevias()){

            // Cambiamos el estado del writer
            guardando = true

            // Abrimos el writer
            bufferedWriter = BufferedWriter(FileWriter(archivo,true))

            // Escribimos la información en el archivos
            escribir()
        }
    }

    /**
     * Escribimos toda la informacion de la [fuenteDatos] en
     * el archivo CSV con ruta [archivo]
     */
    private fun escribir(){

        // Comprobamos que halla una fuente de datos y un buffer abierto
        if (fuenteDatos != null && bufferedWriter != null){

            // Comprobamos que el guardado no se halla cancelado|pausado y halla filas por escribir
            while (!cancelado && !pausado && fuenteDatos!!.hayMasFilas()){

                // Escribimos las cabeceras en el archivo
                if (escribirCabeceras && !cabeceraEscrita){

                    var cabecera = ""
                    for (i in 0 until fuenteDatos!!.getCabeceras().size - 1){
                        cabecera += fuenteDatos!!.getCabeceras().get(i) + separador
                    }
                    cabecera += fuenteDatos!!.getCabeceras().get(fuenteDatos!!.getCabeceras().size - 1) + "\n"
                    bufferedWriter!!.write(cabecera)

                    // Establecemos que ya hemos escrito la cabecera
                    cabeceraEscrita = true
                }

                // Escribimos el cuerpo del CSV
                var fila = fuenteDatos!!.siguienteFila()
                var cuerpo = ""
                for (i in 0 until fila!!.size - 1){
                    cuerpo += fila.get(i) + separador
                }
                cuerpo += fila.get(fila.size - 1) + "\n"
                bufferedWriter!!.write(cuerpo)

                // Hacemos efectivos los cambios
                bufferedWriter!!.flush()
            }

            // Terminamos el guardado de los datos
            if (cancelado || !fuenteDatos!!.hayMasFilas()){
                guardadoTerminado()
            }
        }
    }

    /**
     * Realizamos una serie de comprobaciones antes de comenzar
     * a guardar la informacion en el archivo CSV
     */
    private fun superaComprobacionesPrevias(): Boolean {

        // Comprobamos que no se halla iniciado ya un guardado
        // o se halla cancelado el que se estaba realizando
        if (!guardando && !cancelado){

            // Comprobamos que haya una fuente de datos
            if (fuenteDatos != null){
                return true
            }
        }
        return false
    }


    override fun pausarGuardado() {
        pausado = true
    }

    override fun reanudarGuardado() {
        pausado = false
        escribir()
    }

    override fun cancelarGuardado() {
        cancelado = true
    }

    /**
     * Serie de tareas que se realizarán una vez que
     * se halla terminado el guardado o se halla cancelado
     */
    private fun guardadoTerminado(){
        guardando = false

        if (bufferedWriter != null){

            bufferedWriter!!.close()
            bufferedWriter = null
        }


        println(guardando)
        println(cancelado)
        println(pausado)
        println(bufferedWriter)
    }
}