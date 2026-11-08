package bas.orellana.gastostracker.presentation.util

/** Acepta solo dígitos con un separador decimal (coma o punto) y hasta 2 decimales. */
private val IMPORTE_EN_EDICION = Regex("^\\d*[,.]?\\d{0,2}$")

/** Indica si [texto] es una entrada parcial válida mientras el usuario escribe un importe. */
fun esEntradaImporteValida(texto: String): Boolean = texto.matches(IMPORTE_EN_EDICION)

/**
 * Convierte el texto escrito por el usuario en un importe.
 * Acepta coma o punto como separador decimal ("12,50" y "12.50").
 * Devuelve null si el texto no es un número válido o si el importe no es mayor que 0.
 */
fun parseImporte(texto: String): Double? {
    val valor = texto.trim().replace(',', '.').toDoubleOrNull() ?: return null
    return valor.takeIf { it.isFinite() && it > 0 }
}
