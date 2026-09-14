package bas.orellana.gastostracker.domain.usecase

import bas.orellana.gastostracker.domain.model.Categoria
import bas.orellana.gastostracker.domain.model.IngresoModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class IngresoUseCasesTest {

    private fun ingreso(id: String = "1", monto: Double = 1000.0) = IngresoModel(
        id = id,
        nombre = "Sueldo",
        monto = monto,
        fecha = LocalDate(2026, 1, 1),
        categoria = Categoria.OTROS
    )

    @Test
    fun `AddIngresoUseCase agrega el ingreso al repositorio`() = runTest {
        val repository = FakeIngresoRepository()
        val useCase = AddIngresoUseCase(repository)

        useCase(ingreso())

        assertEquals(listOf(ingreso()), repository.getIngresos().first())
    }

    @Test
    fun `UpdateIngresoUseCase reemplaza el ingreso existente`() = runTest {
        val repository = FakeIngresoRepository()
        repository.addIngreso(ingreso(monto = 1000.0))
        val useCase = UpdateIngresoUseCase(repository)

        useCase(ingreso(monto = 1500.0))

        assertEquals(1500.0, repository.getIngresos().first().single().monto)
    }

    @Test
    fun `DeleteIngresoUseCase elimina solo el ingreso indicado`() = runTest {
        val repository = FakeIngresoRepository()
        repository.addIngreso(ingreso(id = "1"))
        repository.addIngreso(ingreso(id = "2"))
        val useCase = DeleteIngresoUseCase(repository)

        useCase("1")

        assertEquals(listOf("2"), repository.getIngresos().first().map { it.id })
    }

    @Test
    fun `GetIngresosUseCase expone el flujo del repositorio`() = runTest {
        val repository = FakeIngresoRepository()
        repository.addIngreso(ingreso())
        val useCase = GetIngresosUseCase(repository)

        assertEquals(repository.getIngresos().first(), useCase().first())
    }
}
