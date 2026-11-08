package bas.orellana.gastostracker.data.repository

import bas.orellana.gastostracker.data.EntornoDatos
import bas.orellana.gastostracker.domain.model.IngresoModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class IngresoRepositoryImplTest {

    private val entorno = EntornoDatos()
    private val repo = entorno.ingresos

    @AfterTest
    fun cerrar() = entorno.close()

    private fun ingreso(id: String, monto: Double, fecha: LocalDate) =
        IngresoModel(id = id, nombre = "Ingreso $id", monto = monto, fecha = fecha)

    @Test
    fun `getTotalIngresos es 0 si no hay ingresos en el rango`() = runTest {
        repo.addIngreso(ingreso("a", 1000.0, LocalDate(2026, 10, 31)))

        assertEquals(0.0, repo.getTotalIngresos(LocalDate(2026, 11, 1), LocalDate(2026, 11, 30)).first())
    }

    @Test
    fun `getTotalIngresos suma los ingresos del rango, extremos incluidos`() = runTest {
        repo.addIngreso(ingreso("a", 1000.0, LocalDate(2026, 11, 1)))
        repo.addIngreso(ingreso("b", 250.5, LocalDate(2026, 11, 30)))
        repo.addIngreso(ingreso("c", 99.0, LocalDate(2026, 12, 1)))

        assertEquals(1250.5, repo.getTotalIngresos(LocalDate(2026, 11, 1), LocalDate(2026, 11, 30)).first())
    }

    @Test
    fun `getIngresosEntre, updateIngreso y deleteIngreso`() = runTest {
        val original = ingreso("a", 1000.0, LocalDate(2026, 11, 1))
        repo.addIngreso(original)
        repo.addIngreso(ingreso("b", 50.0, LocalDate(2026, 12, 5)))

        repo.updateIngreso(original.copy(nombre = "Nómina", monto = 1100.0))
        val noviembre = repo.getIngresosEntre(LocalDate(2026, 11, 1), LocalDate(2026, 11, 30)).first()
        assertEquals(listOf(original.copy(nombre = "Nómina", monto = 1100.0)), noviembre)

        repo.deleteIngreso("a")
        assertEquals(listOf("b"), repo.getIngresos().first().map { it.id })
    }
}
