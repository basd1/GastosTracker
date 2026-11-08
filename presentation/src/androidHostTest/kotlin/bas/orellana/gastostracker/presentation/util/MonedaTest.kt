package bas.orellana.gastostracker.presentation.util

import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals

class MonedaTest {

    // Según la versión de los datos de idioma, el espacio antes del símbolo puede ser normal o no separable.
    private fun String.normalizarEspacios() = replace(' ', ' ').replace(' ', ' ')

    @Test
    fun `en espanol usa coma decimal, punto de miles y el euro detras`() {
        assertEquals("12.345,50 €", formatMoney(12345.5, "EUR", Locale.forLanguageTag("es-ES")).normalizarEspacios())
    }

    @Test
    fun `siempre muestra dos decimales`() {
        assertEquals("25,00 €", formatMoney(25.0, "EUR", Locale.forLanguageTag("es-ES")).normalizarEspacios())
        assertEquals("0,10 €", formatMoney(0.1, "EUR", Locale.forLanguageTag("es-ES")).normalizarEspacios())
    }

    @Test
    fun `en ingles de EEUU usa el formato americano`() {
        assertEquals("€12,345.50", formatMoney(12345.5, "EUR", Locale.forLanguageTag("en-US")))
    }

    @Test
    fun `respeta la moneda indicada`() {
        assertEquals("$1,234.50", formatMoney(1234.5, "USD", Locale.forLanguageTag("en-US")))
    }

    @Test
    fun `los importes negativos llevan signo`() {
        assertEquals("-12,30 €", formatMoney(-12.3, "EUR", Locale.forLanguageTag("es-ES")).normalizarEspacios())
    }
}
