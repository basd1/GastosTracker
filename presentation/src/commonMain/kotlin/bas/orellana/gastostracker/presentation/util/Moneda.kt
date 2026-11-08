package bas.orellana.gastostracker.presentation.util

/** Moneda de la app mientras no sea configurable (ver Bloque 6 del plan). Código ISO 4217. */
const val MONEDA_POR_DEFECTO = "EUR"

/**
 * Formatea un importe como moneda según el idioma y la región del dispositivo:
 * `1.234,50 €` en España, `€1,234.50` en EE. UU., etc.
 * Es el único sitio de la app que decide cómo se muestra un importe.
 */
expect fun formatMoney(importe: Double, codigoMoneda: String = MONEDA_POR_DEFECTO): String

/** Símbolo de la moneda en el idioma del dispositivo (`€`, `US$`...), para los campos de importe. */
expect fun simboloMoneda(codigoMoneda: String = MONEDA_POR_DEFECTO): String
