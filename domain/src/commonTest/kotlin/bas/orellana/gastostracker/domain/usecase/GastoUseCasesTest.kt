package bas.orellana.gastostracker.domain.usecase

import bas.orellana.gastostracker.domain.model.Categoria
import bas.orellana.gastostracker.domain.model.GastoModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GastoUseCasesTest {

    private fun gasto(id: String = "1", monto: Double = 100.0) = GastoModel(
        id = id,
        nombre = "Super",
        monto = monto,
        fecha = LocalDate(2026, 1, 1),
        categoria = Categoria.COMIDA
    )

    @Test
    fun `AddGastoUseCase agrega el gasto al repositorio`() = runTest {
        val repository = FakeGastoRepository()
        val useCase = AddGastoUseCase(repository)

        useCase(gasto())

        assertEquals(listOf(gasto()), repository.getGastos().first())
    }

    @Test
    fun `UpdateGastoUseCase reemplaza el gasto existente`() = runTest {
        val repository = FakeGastoRepository()
        repository.addGasto(gasto(monto = 100.0))
        val useCase = UpdateGastoUseCase(repository)

        useCase(gasto(monto = 250.0))

        assertEquals(250.0, repository.getGastos().first().single().monto)
    }

    @Test
    fun `DeleteGastoUseCase elimina solo el gasto indicado`() = runTest {
        val repository = FakeGastoRepository()
        repository.addGasto(gasto(id = "1"))
        repository.addGasto(gasto(id = "2"))
        val useCase = DeleteGastoUseCase(repository)

        useCase("1")

        assertEquals(listOf("2"), repository.getGastos().first().map { it.id })
    }

    @Test
    fun `DeleteAllGastosUseCase vacia el repositorio`() = runTest {
        val repository = FakeGastoRepository()
        repository.addGasto(gasto(id = "1"))
        repository.addGasto(gasto(id = "2"))
        val useCase = DeleteAllGastosUseCase(repository)

        useCase()

        assertTrue(repository.getGastos().first().isEmpty())
    }

    @Test
    fun `GetGastosUseCase expone el flujo del repositorio`() = runTest {
        val repository = FakeGastoRepository()
        repository.addGasto(gasto())
        val useCase = GetGastosUseCase(repository)

        assertEquals(repository.getGastos().first(), useCase().first())
    }
}
