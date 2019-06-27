package Common

import kotlinx.coroutines.*
import lib.Common.Controlador.BufferedWriter.BufferedWriterCSV
import lib.Common.Modelo.FuenteDatos
import tech.tablesaw.api.IntColumn
import tech.tablesaw.api.StringColumn
import tech.tablesaw.api.Table

fun main() = runBlocking{

    val table = Table.create("Tabla")

    val idColum = IntColumn.create("Id")
    val nombreColum = StringColumn.create("Nombre")

    with(table){
        addColumns(idColum)
        addColumns(nombreColum)
    }

    for (i in 0 until 1000000){
        table.column("Id").appendCell(i.toString())
    }

    table.column("Nombre").appendCell("Pepe")
    table.column("Nombre").appendCell("Juan")

    val fuente = FuenteDatos(table)

    val buffereWriter = BufferedWriterCSV.Builder()
            .guardarEn("C:/Users/abrah/Desktop/Prueba.csv")
            .escribirCabeceras(true)
            .obtenerDatosDe(fuente)
            .build()

    val deferred = buffereWriter.guardarAsync()

    runBlocking {
        delay(2000)
        buffereWriter.pausarGuardado()
        delay(2000)
        buffereWriter.reanudarGuardado()
    }

    println("El resultado es ${deferred!!.await()}")

}