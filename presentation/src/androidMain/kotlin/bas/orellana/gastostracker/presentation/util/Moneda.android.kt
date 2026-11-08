package bas.orellana.gastostracker.presentation.util

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

actual fun formatMoney(importe: Double, codigoMoneda: String): String =
    formatMoney(importe, codigoMoneda, Locale.getDefault())

actual fun simboloMoneda(codigoMoneda: String): String =
    Currency.getInstance(codigoMoneda).getSymbol(Locale.getDefault())

/** NumberFormat es caro de crear y no es thread-safe: uno por hilo, idioma y moneda. */
private val formateadores = object : ThreadLocal<MutableMap<Pair<Locale, String>, NumberFormat>>() {
    override fun initialValue(): MutableMap<Pair<Locale, String>, NumberFormat> = mutableMapOf()
}

internal fun formatMoney(importe: Double, codigoMoneda: String, locale: Locale): String {
    val formateador = formateadores.get()!!.getOrPut(locale to codigoMoneda) {
        NumberFormat.getCurrencyInstance(locale).apply { currency = Currency.getInstance(codigoMoneda) }
    }
    return formateador.format(importe)
}
