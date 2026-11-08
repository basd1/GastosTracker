package bas.orellana.gastostracker.presentation.util

import kotlinx.datetime.LocalDate

/** Una línea del CSV de movimientos. Los gastos y lo apartado van en negativo; los ingresos, en positivo. */
data class FilaCsv(
    val fecha: LocalDate,
    val tipo: String,
    val concepto: String,
    val categoria: String,
    val importe: Double,
    val nota: String = "",
    val metodoPago: String = ""
)

/**
 * CSV pensado para abrirse con doble clic en Excel en español: separador `;`, coma decimal,
 * fechas dd/mm/aaaa, saltos de línea CRLF y BOM UTF-8 (sin BOM, Excel rompe las tildes).
 * Ordenado por fecha, del más reciente al más antiguo.
 */
fun generarCsv(cabecera: List<String>, filas: List<FilaCsv>): String = buildString {
    append('﻿')
    append(cabecera.joinToString(";") { escaparCsv(it) })
    append("\r\n")
    filas.sortedByDescending { it.fecha }.forEach { fila ->
        append(
            listOf(
                fila.fecha.formatDDMMYYYY(),
                fila.tipo,
                fila.concepto,
                fila.categoria,
                formatDecimal(fila.importe, 2).replace('.', ','),
                fila.nota,
                fila.metodoPago
            ).joinToString(";") { escaparCsv(it) }
        )
        append("\r\n")
    }
}

/** Entre comillas si lleva separador, comillas o saltos de línea; las comillas se duplican. */
internal fun escaparCsv(valor: String): String =
    if (valor.any { it == ';' || it == '"' || it == '\n' || it == '\r' }) "\"" + valor.replace("\"", "\"\"") + "\""
    else valor
