package bas.orellana.gastostracker.presentation.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TicketTest {

    @Test
    fun `coge el total y no el subtotal`() {
        val ticket = """
            SUPERMERCADO EL SOL
            Pan          1,20
            Leche        0,95
            SUBTOTAL     2,15
            IVA 10%      0,22
            TOTAL        2,37
            Entregado   10,00
            Cambio       7,63
        """.trimIndent()

        assertEquals(2.37, extraerImporteTotal(ticket))
    }

    @Test
    fun `entiende miles con punto o coma y el total en la linea siguiente`() {
        assertEquals(1234.56, extraerImporteTotal("TOTAL A PAGAR\n1.234,56 €"))
        assertEquals(1234.56, extraerImporteTotal("Total: 1,234.56"))
    }

    @Test
    fun `sin palabra total usa el importe mas alto`() {
        assertEquals(15.5, extraerImporteTotal("Café 1,50\nMenú 15,50\nAgua 1,00"))
    }

    @Test
    fun `sin importes devuelve null`() {
        assertNull(extraerImporteTotal("Gracias por su visita\n08/11/2026"))
        assertNull(extraerImporteTotal(""))
    }
}
