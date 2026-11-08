package bas.orellana.gastostracker.presentation.util

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import bas.orellana.gastostracker.domain.model.Ajustes

/** Moneda por defecto mientras no se han leído los ajustes. Código ISO 4217. */
const val MONEDA_POR_DEFECTO = Ajustes.MONEDA_POR_DEFECTO

/**
 * Moneda elegida en Ajustes. Es estado de Compose: al cambiarla, todo lo que se pintó con
 * [formatMoney] o [simboloMoneda] (sin pasar la moneda) se vuelve a pintar solo.
 * La sincroniza `AppNavHost` con los ajustes guardados.
 */
object MonedaActual {
    var codigo: String by mutableStateOf(MONEDA_POR_DEFECTO)
}

/**
 * Formatea un importe como moneda según el idioma y la región del dispositivo:
 * `1.234,50 €` en España, `€1,234.50` en EE. UU., etc.
 * Es el único sitio de la app que decide cómo se muestra un importe.
 */
expect fun formatMoney(importe: Double, codigoMoneda: String = MonedaActual.codigo): String

/** Símbolo de la moneda en el idioma del dispositivo (`€`, `US$`...), para los campos de importe. */
expect fun simboloMoneda(codigoMoneda: String = MonedaActual.codigo): String
