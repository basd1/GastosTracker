package bas.orellana.gastostracker.presentation.util

/** Importes con 2 decimales: `12,50`, `1.234,56`, `1,234.56`, `12.50 €`... */
private val IMPORTE = Regex("""(?<![\d.,])(\d{1,3}(?:[.,\s]\d{3})*|\d+)[.,](\d{2})(?![\d])""")

/** Palabras que suelen acompañar al total en un ticket, de más a menos fiables. */
private val PALABRAS_TOTAL = listOf("total a pagar", "importe total", "total eur", "total €", "a pagar", "total", "importe")

/**
 * Busca el importe total en el texto reconocido de un ticket:
 * 1. un importe en una línea con "total", "a pagar"... (sin contar "subtotal"), o en la línea siguiente;
 * 2. si no hay, el importe más alto del ticket.
 * Devuelve null si no encuentra ningún importe.
 */
fun extraerImporteTotal(texto: String): Double? {
    val lineas = texto.lines().map { it.trim() }.filter { it.isNotEmpty() }
    for (palabra in PALABRAS_TOTAL) {
        lineas.forEachIndexed { indice, linea ->
            val minusculas = linea.lowercase()
            if (palabra in minusculas && "subtotal" !in minusculas) {
                (importes(linea).lastOrNull() ?: lineas.getOrNull(indice + 1)?.let { importes(it).lastOrNull() })
                    ?.let { return it }
            }
        }
    }
    return lineas.flatMap { importes(it) }.maxOrNull()
}

private fun importes(linea: String): List<Double> = IMPORTE.findAll(linea).mapNotNull { coincidencia ->
    val entero = coincidencia.groupValues[1].filter { it.isDigit() }
    "$entero.${coincidencia.groupValues[2]}".toDoubleOrNull()?.takeIf { it > 0 }
}.toList()
