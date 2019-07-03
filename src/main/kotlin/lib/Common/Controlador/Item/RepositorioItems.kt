package lib.Common.Controlador.Item

import KFoot.Constantes
import KFoot.IMPORTANCIA
import KFoot.Logger
import lib.Common.Utiles.Utils
import kotlinx.coroutines.*
import tech.tablesaw.api.Table
import io.reactivex.subjects.PublishSubject
import lib.Common.Controlador.BufferedWriter.BufferedWriterCSV
import lib.Common.Controlador.BufferedWriter.GuardadoAsyncListener
import lib.Common.Controlador.BufferedWriter.IBufferedWriter
import lib.Common.Controlador.BufferedWriter.IBufferedWriterAsync
import lib.Common.Modelo.FuenteDatos
import lib.Common.Utiles.ColumnsUtils
import kotlin.coroutines.CoroutineContext

class RepositorioItems(clazz: Class<Item>, listaInmuebles: List<Item>? = null, val configuracion: ConfiguracionRepositorioItems = ConfiguracionRepositorioItems()){

    // --- Tipo de dato del Repositorio ---
    private var instanciaTipoActual: Item? = null                        // Nos servirá más adelante para obtener la información de los items que se creen
    private var tipoActual: Class<Item> = clazz                          // Tipo de dato que almacena el repositorio
    // -----

    // --- Referente al Dataframe ---
    private lateinit var dataframe: Table                               // Dataframe con los datos
    private lateinit var nomCols: List<String>                          // Nombre de las columnas que almacena el dataframe
    // -----

    // --- Lógica de la clase ---
    private var seHaCambiadoTipo = false                                // Permitira la modificacion de las columnas del dataframe en su estado inicial
    private var fuenteDatos: FuenteDatos = FuenteDatos()                // Fuente de datos que proporcionaremos al writer
    private var bufferedWriter: IBufferedWriterAsync? = null            // Buffered writer
    private var completableDeferred: CompletableDeferred<Unit>? = null  // Completable vinculado al guardado asíncrono
    private var permitirGuardado: Boolean = true                        // Permitirá que se guarden los datos
    // -----

    companion object {

        /**
         * Creamos un {[ConjuntoInmuebleFactory]} del tipo necesitado
         *
         * @return {[ConjuntoInmuebleFactory]}
         */
        inline fun <reified T: Item> create(listaInmueble: List<T>? = null, propiedades: ConfiguracionRepositorioItems = ConfiguracionRepositorioItems()): RepositorioItems{
            return RepositorioItems(T::class.java as Class<Item>, listaInmueble, propiedades)
        }
    }

    init {

        // No podemos crear un repositorio de items básicos
        if(clazz == Item::class.java){
            throw InstantiationError("No se puede crear un Repositorio de Items básicos")
        }

        // Creamos una instancia de la clase obtenida en el constructor
        instanciaTipoActual= clazz.newInstance()

        // Creamos el dataframe y añadimos los inmuebles
        crearDataFrame(listaInmuebles)

        // Comenzamos a emitir ticks para el guardado de los datos
        //establecerGuardadoAutomatico()
    }

    /**
     * Creamos el dataframe y añadimos datos si nos lo pasan
     *
     * @param listaInmuebles: Lista de inmuebles que añadiremos al dataframe
     */
    private fun crearDataFrame(listaInmuebles: List<Item>? = null){

        // Creamos el dataframe
        dataframe = Table.create()

        // Añadimos las columnas al dataframe
        anadirColsDataframe(dataframe, instanciaTipoActual!!)

        // Establecemos los nombres de las columnas según los nombres de los atributos
        // del tipo actual
        this.nomCols = instanciaTipoActual!!.obtenerNombreAtributos()

        // Añadimos la lista de inmueble al dataframe
        anadirListaItems(listaInmuebles)
    }



    /**
     * Añadimos el item pasado por parámetro
     * a nuestro dataframe
     *
     * @param item: Item a añadir al dataframe
     */
    fun anadirItem(item: Item){

        // Comprobamos que el item recibido por parametro sea diferente al ya establecido
        if (item.javaClass != tipoActual){

            // Cambiamos los campos del dataframe
            cambiarTipoDataFrameActual(item)
        }

        // Comprobamos que el item sea del mismo tipo
        // que el que se almacena actualmente
        if (item.javaClass == tipoActual){

            // Recorremos cada atributo del item
            nomCols.forEach { atributo ->

                val valor = item.obtenerValorDe(atributo,item)

                // Evitamos setear valores extraños al dataframe
                if (valor != null){
                    dataframe.column(atributo).appendCell(valor)
                }
            }
        }

    }

    /**
     * Añadimos los items de la lista al dataframe para su posterior
     * guardado
     *
     * @param listaItems: Lista con los items a añadir
     */
    fun anadirListaItems(listaItems: List<Item>?){

        // Añadimos los datos al dataframe
        if (listaItems != null) {
            if (listaItems.size > 0){

                // Recorremos cada uno de los inmuebles
                listaItems.forEach { inmueble ->

                    anadirItem(inmueble)

                }
            }
        }
    }



    /**
     * Guardamos los datos del dataframe según la configuracion
     * pasada en el constructor
     * @see [configuracion]
     */
    fun guardarAsync() {

        if (permitirGuardado){

            when {
                // Se guardará en un archivo CSV
                configuracion.getExtensionArchivo() == Constantes.EXTENSIONES_ARCHIVOS.csv -> {
                    guardarCSV()
                }
            }
        }
    }

    /**
     * Guardamos los datos del dataframe en el archivo
     * CSV proporcionado en la [configuracion]
     */
    private fun guardarCSV(){

        // Si ya hay un guardado en curso evitamos hacer otro
        if (bufferedWriter == null && completableDeferred == null){

            //Creamos nuestra fuente de datos
            fuenteDatos = FuenteDatos(dataframe)

            // Creamos el writer
            bufferedWriter = BufferedWriterCSV.Builder()
                    .escribirCabecerasSiNoExisteArchivo()
                    .guardarEn("${configuracion.getRutaGuardadoArchivos()}/${configuracion.getNombreArchivo()}.${configuracion.getExtensionArchivo()}")
                    .obtenerDatosDe(fuenteDatos)
                    .build()

            // Comprobamos que se haya podrido crear el writer
            if (bufferedWriter != null){

                // Comenzamos el guardado de forma asíncrona
                bufferedWriter!!.guardarAsync({
                    it -> completableDeferred = it
                },{},{
                    bufferedWriter = null
                    completableDeferred = null
                })
            }

            else{
                Logger.getLogger().debug(KFoot.DEBUG.DEBUG_TEST,"No se ha podido crear el BufferedWriterCSV, comprueba que se pasen los parámetros necesarios al constructor",IMPORTANCIA.ALTA)
            }
        }
    }



    /**
     * Añadimos al dataframe nuevas columnas a partir
     * del ([item]) pasado por parámetros
     *
     * @param df: Tabla a la que se le asignaran las columnas
     * @param item: Item del que estraeremos las columnas a establecer en [df]
     */
    private fun anadirColsDataframe(df: Table, item: Item){

        val listaAtributos = item.obtenerNombreTipoAtributos()

        with(df){
            addColumns(
                    //Hacemos uso del operador "spread" (*), que nos permite pasar un Array a un vararg
                    *listaAtributos.map {
                        ColumnsUtils.castearAColumna(it.first,it.second)!!
                    }.toTypedArray()
            )
        }
    }

    /**
     * Cambiamos el tipo de dato que se almacena en el dataframe
     * por el tipo de inmueble pasado como parametro
     *
     * @param nuevoItem: Nuevo inmueble que se va a almacenar en el dataframe
     */
    private fun cambiarTipoDataFrameActual(nuevoItem: Item){

        // Comprobamos que no se haya cambiado ya el tipo
        if(!seHaCambiadoTipo){

            // Comprobamos que el item nuevo sea una subclase del actual
            if (KFoot.Utils.esSubclase(nuevoItem.javaClass,tipoActual)){

                // Dataframe con las columnas del nuevo tipo
                val tmpDataFrame = Table.create()

                // Añadimos al dataframe temporal las columnas del nuevo item
                anadirColsDataframe(tmpDataFrame, nuevoItem)

                // Clonamos los datos del dataframe viejo al nuevo
                clonarDatos(tmpDataFrame, nuevoItem)

                // Establecemos el nuevo dataframe con las nuevas columnas
                dataframe = tmpDataFrame

                // Nuevo tipo de inmueble que almacena el dataframe
                this.tipoActual = nuevoItem.javaClass

                // Instancia del inmueble almacenado
                this.instanciaTipoActual = nuevoItem

                // Guardamos los viejos nombres de columnas
                val viejosNomCols = this.nomCols

                // Establecemos los nuevos nombres de las columnas
                this.nomCols = nuevoItem.obtenerNombreAtributos()

                // Volvemos a comprobar si podemos guardar los datos
                if (nuevsCabsContienenViejsCabs(nomCols,viejosNomCols)){
                    permitirGuardado = true
                }

                // Las nuevas columnas no coinciden con las del antiguo dataframe
                else {
                    permitirGuardado = false
                }

                // Ya no podremos cambiar más el tipo de dato de este dataframe
                this.seHaCambiadoTipo = true

                Logger.getLogger().debug(KFoot.DEBUG.DEBUG_TEST, "Se ha cambiado el tipo de dato almacenado en el dataframe correctamente", IMPORTANCIA.BAJA)
            }
        }

        // El item recibido es diferente al actual y ya hemos modificado el tipo de dato almacenado
        else {
            Logger.getLogger().debug(KFoot.DEBUG.DEBUG_SIMPLE, "El tipo de item almacenado en el repositorio ya ha sido modificado. El item no se guardara.", IMPORTANCIA.ALTA)
            return
        }
    }

    /**
     * Comprobamos que las columnas de los dataframes pasados por parametro
     * coincidan
     * *Solo comprueba que los nombres de las columnas coincidan
     *
     * @param nuevoDataframe: Nuevo dataframe
     * @param viejoDataframe: VIejo dataframe
     */
    private fun colsDataframesCoinciden(nuevoDataframe: Table, viejoDataframe: Table): Boolean{

        // Comprobamos primero que la cuenta de columnas coincidan
        if (nuevoDataframe.columnCount() == nomCols.size){

            // Array con los nombres de las columnas del nuuevo dataframe
            val cabecerasNuevoDataframe = nuevoDataframe.columnArray()

            // Array con los nombres de las columnas del viejo dataframe
            val cabecerasViejoDataframe = viejoDataframe.columnArray()

            // Comprobamos si todas las cols del archivo tienen los mismos
            // nombres que las cols del dataframe actual
            val noCoincide = cabecerasNuevoDataframe.firstOrNull {
                !cabecerasViejoDataframe.contains(it)
            }

            // Comprobamos si hay algún valor en
            // la variable #noCoincide
            if (noCoincide == null){
                return true
            }
        }
        return false
    }

    /**
     * Comprobamos que la lista de las nuevas cabeceras del dataframe contenga
     * todas las cabeceras antiguas
     *
     * @param nuevasCabeceras: Nuevas cabeceras
     * @param viejasCabeceras: VIejos cabeceras
     */
    private fun nuevsCabsContienenViejsCabs(nuevasCabeceras: List<String>, viejasCabeceras: List<String>): Boolean{

        // Comprobamos primero que la cuenta de columnas coincidan
        if (nuevasCabeceras.size >= viejasCabeceras.size){

            // Comprobamos si todas las cols del archivo tienen los mismos
            // nombres que las cols del dataframe actual
            val noCoincide = viejasCabeceras.firstOrNull {
                !nuevasCabeceras.contains(it)
            }

            // Estan todas las cols
            if (noCoincide == null){
                return true
            }
        }
        return false
    }

    /**
     *  Clonamos los datos del dataframe actual al pasado
     *  por parametro
     *
     *  @param nuevoDataframe: Nuevo dataframe al que le añadiremos los datos
     *  @param nuevoItem: Tipo de inmueble nuevo
     *
     */
    private fun clonarDatos(nuevoDataframe: Table, nuevoItem: Item){

        val nuevaClase = nuevoItem.javaClass

        // Recorremos las filas del dataframe
        dataframe.forEach{fila ->

            val nuevaInstancia = nuevaClase.newInstance()

            // Recorremos cada columna
            fila.columnNames().forEach{nomCol ->

                // Obtenemos el valor de la celda actual
                val valor = fila.getObject(nomCol)

                // Establecemos los valores a la nueva instancia
                nuevaInstancia.establecerValor(nomCol,valor,nuevaInstancia)
            }

            // Añadimos los valores al nuevo dataframe
            nuevoDataframe.columns().forEach{ col ->

                // Obtenemos el valor del objeto
                val valor = nuevaInstancia.obtenerValorDe(col.name(),nuevaInstancia)

                // Añadimos el valor al nuevo dataframe
                nuevoDataframe.column(col.name()).appendCell(valor)
            }
        }
    }



    /**
     * Covertimos los datos almacenados en el datafram en una lista
     * de inmuebles
     *
     * @return ArrayList<T>: Lista con los inmuebles
     */
    fun obtenerInmueblesAlmacenados(): ArrayList<Item> {

        val listaItems: ArrayList<Item> = ArrayList()

        // Recorremos cada tupla del dataframe
        dataframe.forEach{fila ->

            // Creamos una instancia del tipo de inmueble que se almacena en la tabla
            val objInmueble = tipoActual.newInstance() as Item

            // Recorremos cada columna de la tupla actual
            fila.columnNames().forEach {nombreCol ->

                // Obtenemos el campo actual de la tupla que estemos recorriendo
                var valorColumna = fila.getObject(nombreCol)

                // Establecemos el valor de la columna actual al respectivo atributo del inmueble
                objInmueble.establecerValor(nombreCol,valorColumna,objInmueble)

            }

            // Añadimos el inmueble a la lista
            listaItems.add(objInmueble as Item)

        }

        return listaItems
    }

    /**
     * Comprobamos si aún quedan datos en el
     * {[dataframe]}
     *
     * @param Boolean si aún quedan datos
     */
    fun todoGuardado(): Boolean{
        return dataframe.count() == 0 && completableDeferred != null && completableDeferred!!.isCompleted
    }

    /**
     * Esperamos hasta la finalización del guardado asíncrono
     * si existe uno en curso
     */
    fun esperarFinalizacionGuardado(){
        if (bufferedWriter != null){
            bufferedWriter!!.esperarHastaFinalizacion()
        }
    }

    fun getTipoActual(): Class<Item> {
        return tipoActual
    }

    fun getCount():Int {
        return dataframe.count()
    }
}