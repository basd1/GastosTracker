package bas.orellana.gastostracker.domain.model

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MesTest {

    @Test
    fun `anterior y siguiente cruzan el cambio de año`() {
        assertEquals(Mes(2025, 12), Mes(2026, 1).anterior())
        assertEquals(Mes(2027, 1), Mes(2026, 12).siguiente())
        assertEquals(Mes(2024, 11), Mes(2026, 2).mas(-15))
    }

    @Test
    fun `el rango incluye el primer y el ultimo dia, tambien en bisiestos`() {
        assertEquals(LocalDate(2028, 2, 1)..LocalDate(2028, 2, 29), Mes(2028, 2).rango)
        assertEquals(28, Mes(2026, 2).numeroDeDias)
        assertTrue(LocalDate(2026, 11, 30) in Mes(2026, 11))
        assertFalse(LocalDate(2026, 12, 1) in Mes(2026, 11))
    }

    @Test
    fun `se escribe y se lee como texto`() {
        assertEquals("2026-03", Mes(2026, 3).toString())
        assertEquals(Mes(2026, 3), Mes.parse("2026-03"))
        assertNull(Mes.parse("2026-13"))
        assertNull(Mes.parse("hola"))
    }

    @Test
    fun `se ordenan cronologicamente`() {
        assertTrue(Mes(2025, 12) < Mes(2026, 1))
        assertEquals(listOf(Mes(2025, 1), Mes(2025, 7), Mes(2026, 1)), listOf(Mes(2026, 1), Mes(2025, 1), Mes(2025, 7)).sorted())
    }
}
