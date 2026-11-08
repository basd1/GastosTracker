package bas.orellana.gastostracker.presentation.util

import platform.Foundation.NSLocale
import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterCurrencyStyle
import platform.Foundation.currentLocale

private fun formateadorMoneda(codigoMoneda: String) = NSNumberFormatter().apply {
    numberStyle = NSNumberFormatterCurrencyStyle
    currencyCode = codigoMoneda
    locale = NSLocale.currentLocale
}

actual fun formatMoney(importe: Double, codigoMoneda: String): String =
    formateadorMoneda(codigoMoneda).stringFromNumber(NSNumber(double = importe))
        ?: "${formatDecimal(importe, 2)} $codigoMoneda"

actual fun simboloMoneda(codigoMoneda: String): String =
    formateadorMoneda(codigoMoneda).currencySymbol
