package bas.orellana.gastostracker.data.repository

import bas.orellana.gastostracker.data.EntornoDatos
import bas.orellana.gastostracker.domain.model.Ajustes
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PreferencesRepositoryTest {

    private val entorno = EntornoDatos()
    private val repo = PreferencesRepository(entorno.preferencias)

    @AfterTest
    fun cerrar() = entorno.close()

    @Test
    fun `sin nada guardado devuelve los valores por defecto`() = runTest {
        assertEquals(Ajustes(), repo.ajustes.first())
    }

    @Test
    fun `guarda y lee todos los ajustes`() = runTest {
        repo.setTemaOscuro(true)
        repo.setMoneda("USD")
        repo.setDiaInicioMes(25)
        repo.setRecordatorio(true, 20, 30)
        repo.setBloqueo(true)
        repo.setUltimoResumenVisto("2026-10")

        assertEquals(
            Ajustes(true, "USD", 25, true, 20, 30, true, "2026-10"),
            repo.ajustes.first()
        )
    }

    @Test
    fun `el dia de inicio se limita a 1-28 y no admite monedas desconocidas`() = runTest {
        repo.setDiaInicioMes(31)
        assertEquals(28, repo.ajustes.first().diaInicioMes)
        assertFailsWith<IllegalArgumentException> { repo.setMoneda("XXX") }
    }
}
