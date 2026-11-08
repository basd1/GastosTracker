package bas.orellana.gastostracker.recordatorio

import kotlinx.datetime.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class ProgramadorRecordatorioTest {

    private val hora = 60 * 60 * 1000L

    @Test
    fun `si la hora no ha llegado hoy, avisa hoy`() {
        assertEquals(3 * hora, ProgramadorRecordatorio.milisHastaLaProxima(21, 0, LocalDateTime(2026, 3, 10, 18, 0)))
    }

    @Test
    fun `si la hora ya ha pasado, avisa manana`() {
        assertEquals(23 * hora, ProgramadorRecordatorio.milisHastaLaProxima(21, 0, LocalDateTime(2026, 3, 10, 22, 0)))
        assertEquals(24 * hora, ProgramadorRecordatorio.milisHastaLaProxima(21, 0, LocalDateTime(2026, 3, 10, 21, 0)))
    }
}
