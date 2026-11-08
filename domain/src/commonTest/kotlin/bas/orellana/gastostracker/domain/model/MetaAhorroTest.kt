package bas.orellana.gastostracker.domain.model

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MetaAhorroTest {

    private val hoy = LocalDate(2026, 11, 8)

    private fun meta(objetivo: Double? = 3000.0, fecha: LocalDate? = LocalDate(2027, 4, 30)) =
        MetaAhorro("japon", "Viaje a Japón", objetivo, fecha, 0xFFEF9A9A, LocalDate(2026, 1, 1))

    @Test
    fun `progreso y restante`() {
        val progreso = ProgresoMeta(meta(), ahorrado = 1200.0)

        assertEquals(0.4, progreso.progreso)
        assertEquals(1800.0, progreso.restante)
        assertFalse(progreso.completada)
    }

    @Test
    fun `la cuota mensual reparte lo que falta entre los meses que quedan, contando el actual`() {
        // De noviembre de 2026 a abril de 2027: 6 meses.
        assertEquals(300.0, ProgresoMeta(meta(), ahorrado = 1200.0).cuotaMensual(hoy))
        // Fecha objetivo este mismo mes: todo lo que falta, ahora.
        assertEquals(500.0, ProgresoMeta(meta(fecha = LocalDate(2026, 11, 30)), 2500.0).cuotaMensual(hoy))
    }

    @Test
    fun `sin objetivo o sin fecha no hay cuota, y completada es 0`() {
        assertNull(ProgresoMeta(meta(objetivo = null), 100.0).cuotaMensual(hoy))
        assertNull(ProgresoMeta(meta(fecha = null), 100.0).cuotaMensual(hoy))
        assertNull(ProgresoMeta(meta(objetivo = null), 100.0).progreso)
        val completada = ProgresoMeta(meta(), 3500.0)
        assertTrue(completada.completada)
        assertEquals(0.0, completada.restante)
        assertEquals(0.0, completada.cuotaMensual(hoy))
    }

    @Test
    fun `una meta con la fecha pasada y sin completar esta vencida y no tiene cuota`() {
        val vencida = ProgresoMeta(meta(fecha = LocalDate(2026, 10, 31)), 100.0)

        assertTrue(vencida.vencida(hoy))
        assertNull(vencida.cuotaMensual(hoy))
    }

    @Test
    fun `una aportacion negativa es una retirada`() {
        assertTrue(Aportacion("a", "japon", -50.0, hoy).esRetirada)
        assertFalse(Aportacion("b", "japon", 50.0, hoy).esRetirada)
    }
}
