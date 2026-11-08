package bas.orellana.gastostracker.recordatorio

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Test

/** La zona va fija en cada test: el resultado no depende de la del equipo que los ejecuta. */
class ProgramadorRecordatorioTest {

    private val hora = 60 * 60 * 1000L
    private val madrid = TimeZone.of("Europe/Madrid")

    private fun milis(ahora: LocalDateTime, zona: TimeZone = TimeZone.UTC) =
        ProgramadorRecordatorio.milisHastaLaProxima(21, 0, zona, ahora)

    @Test
    fun `si la hora no ha llegado hoy, avisa hoy`() {
        assertEquals(3 * hora, milis(LocalDateTime(2026, 3, 10, 18, 0)))
    }

    @Test
    fun `si la hora ya ha pasado, avisa manana`() {
        assertEquals(23 * hora, milis(LocalDateTime(2026, 3, 10, 22, 0)))
        assertEquals(24 * hora, milis(LocalDateTime(2026, 3, 10, 21, 0)))
    }

    @Test
    fun `cuenta el cambio de hora`() {
        // De 23:00 a 21:00 del día siguiente son 22 horas de reloj.
        // 29 de marzo de 2026: en España se adelanta el reloj, pasan 21 horas reales.
        assertEquals(21 * hora, milis(LocalDateTime(2026, 3, 28, 23, 0), madrid))
        // 25 de octubre de 2026: se atrasa, pasan 23.
        assertEquals(23 * hora, milis(LocalDateTime(2026, 10, 24, 23, 0), madrid))
    }
}
