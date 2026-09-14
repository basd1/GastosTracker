package bas.orellana.gastostracker.presentation.util

import kotlin.math.abs
import kotlin.math.round

/**
 * Reemplaza String.format("%.Nf", value), que no existe en Kotlin/Native.
 */
fun formatDecimal(value: Double, decimals: Int): String {
    var factor = 1.0
    repeat(decimals) { factor *= 10 }

    val rounded = round(value * factor) / factor
    val negative = rounded < 0
    val absValue = abs(rounded)
    val intPart = absValue.toLong()
    val sign = if (negative) "-" else ""

    if (decimals == 0) return "$sign$intPart"

    val fracPart = round((absValue - intPart) * factor).toLong()
    val fracStr = fracPart.toString().padStart(decimals, '0')
    return "$sign$intPart.$fracStr"
}

fun formatDecimal(value: Float, decimals: Int): String = formatDecimal(value.toDouble(), decimals)

/**
 * Reemplaza BigDecimal(value).stripTrailingZeros().toPlainString(), usado para precargar
 * el monto en los diálogos de edición sin ceros decimales de más (25.0 -> "25").
 */
fun Double.toPlainStringTrimmed(decimals: Int = 2): String {
    val formatted = formatDecimal(this, decimals)
    return if ('.' in formatted) formatted.trimEnd('0').trimEnd('.') else formatted
}
