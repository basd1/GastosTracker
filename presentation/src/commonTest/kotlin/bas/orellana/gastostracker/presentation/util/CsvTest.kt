package bas.orellana.gastostracker.presentation.util

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CsvTest {

    @Test
    fun `formato para Excel en espanol con BOM, punto y coma y coma decimal`() {
        val csv = generarCsv(
            cabecera = listOf("Fecha", "Tipo", "Concepto", "Categoría", "Importe", "Nota", "Método de pago"),
            filas = listOf(
                FilaCsv(LocalDate(2026, 11, 1), "Gasto", "Pan", "Comida", -1.5, nota = "Integral", metodoPago = "Tarjeta"),
                FilaCsv(LocalDate(2026, 11, 8), "Ingreso", "Nómina", "Otros", 1800.0)
            )
        )

        assertTrue(csv.startsWith("﻿"))
        val lineas = csv.removePrefix("﻿").split("\r\n")
        assertEquals("Fecha;Tipo;Concepto;Categoría;Importe;Nota;Método de pago", lineas[0])
        assertEquals("08/11/2026;Ingreso;Nómina;Otros;1800,00;;", lineas[1])
        assertEquals("01/11/2026;Gasto;Pan;Comida;-1,50;Integral;Tarjeta", lineas[2])
    }

    @Test
    fun `escapa separadores, comillas y saltos de linea`() {
        assertEquals("normal", escaparCsv("normal"))
        assertEquals("\"a;b\"", escaparCsv("a;b"))
        assertEquals("\"dijo \"\"hola\"\"\"", escaparCsv("dijo \"hola\""))
        assertEquals("\"dos\nlíneas\"", escaparCsv("dos\nlíneas"))
    }
}
