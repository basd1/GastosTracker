package bas.orellana.gastostracker.data.repository

import bas.orellana.gastostracker.data.EntornoDatos
import bas.orellana.gastostracker.domain.model.Aportacion
import bas.orellana.gastostracker.domain.model.MetaAhorro
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AhorroRepositoryImplTest {

    private val entorno = EntornoDatos()
    private val repo = entorno.ahorro
    private val hoy = LocalDate(2026, 11, 8)

    @AfterTest
    fun cerrar() = entorno.close()

    private fun meta(id: String, objetivo: Double? = null) = MetaAhorro(id, "Meta $id", objetivo, null, 0xFFEF9A9A, hoy)

    @Test
    fun `cada meta suma sus aportaciones y retiradas`() = runTest {
        repo.guardarMeta(meta("japon", 3000.0))
        repo.guardarMeta(meta("coche"))
        repo.guardarAportacion(Aportacion("1", "japon", 500.0, hoy))
        repo.guardarAportacion(Aportacion("2", "japon", -100.0, hoy))
        repo.guardarAportacion(Aportacion("3", "coche", 50.0, hoy))

        val metas = repo.getProgresoMetas().first()
        assertEquals(listOf("japon", "coche"), metas.map { it.meta.id })
        assertEquals(listOf(400.0, 50.0), metas.map { it.ahorrado })
    }

    @Test
    fun `una meta sin aportaciones lleva 0 y el total del mes cuenta solo ese mes`() = runTest {
        repo.guardarMeta(meta("vacia"))
        repo.guardarAportacion(Aportacion("1", "vacia", 30.0, LocalDate(2026, 10, 31)))
        repo.guardarAportacion(Aportacion("2", "vacia", 20.0, LocalDate(2026, 11, 1)))

        assertEquals(20.0, repo.getTotalAhorrado(LocalDate(2026, 11, 1), LocalDate(2026, 11, 30)).first())
        repo.eliminarAportacion("1")
        repo.eliminarAportacion("2")
        assertEquals(0.0, repo.getProgresoMetas().first().single().ahorrado)
    }

    @Test
    fun `eliminar una meta borra sus aportaciones`() = runTest {
        repo.guardarMeta(meta("japon"))
        repo.guardarAportacion(Aportacion("1", "japon", 500.0, hoy))

        repo.eliminarMeta("japon")

        assertTrue(repo.getProgresoMetas().first().isEmpty())
        assertTrue(repo.getTodasLasAportaciones().first().isEmpty())
    }
}
