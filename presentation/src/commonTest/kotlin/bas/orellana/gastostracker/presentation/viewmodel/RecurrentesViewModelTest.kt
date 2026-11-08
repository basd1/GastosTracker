package bas.orellana.gastostracker.presentation.viewmodel

import bas.orellana.gastostracker.domain.model.Recurrente
import bas.orellana.gastostracker.domain.model.TipoMovimiento
import bas.orellana.gastostracker.domain.usecase.EliminarRecurrenteUseCase
import bas.orellana.gastostracker.domain.usecase.GenerarRecurrentesUseCase
import bas.orellana.gastostracker.domain.usecase.GetCategoriasPersonalizadasUseCase
import bas.orellana.gastostracker.domain.usecase.GetRecurrentesUseCase
import bas.orellana.gastostracker.domain.usecase.GuardarRecurrenteUseCase
import bas.orellana.gastostracker.domain.util.todayLocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class RecurrentesViewModelTest {

    private val gastos = FakeGastoRepository()
    private val ingresos = FakeIngresoRepository()
    private val categorias = FakeCategoriasRepository(gastos, ingresos)
    private val recurrentes = FakeRecurrenteRepository(gastos, ingresos)

    @BeforeTest
    fun preparar() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun limpiar() = Dispatchers.resetMain()

    private fun crearViewModel() = RecurrentesViewModel(
        getRecurrentesUseCase = GetRecurrentesUseCase(recurrentes),
        getCategoriasPersonalizadasUseCase = GetCategoriasPersonalizadasUseCase(categorias),
        guardarRecurrenteUseCase = GuardarRecurrenteUseCase(recurrentes),
        eliminarRecurrenteUseCase = EliminarRecurrenteUseCase(recurrentes),
        generarRecurrentesUseCase = GenerarRecurrentesUseCase(recurrentes)
    )

    private fun recurrente(id: String, tipo: TipoMovimiento, monto: Double, activo: Boolean = true) =
        Recurrente(id, tipo, "R $id", monto, diaDelMes = 1, fechaInicio = todayLocalDate(), activo = activo)

    @Test
    fun `los totales mensuales solo cuentan los activos`() = runTest {
        recurrentes.datos.value = listOf(
            recurrente("a", TipoMovimiento.GASTO, 700.0),
            recurrente("b", TipoMovimiento.GASTO, 15.0, activo = false),
            recurrente("c", TipoMovimiento.INGRESO, 1800.0)
        )
        val vm = crearViewModel()

        assertEquals(700.0, vm.state.value.totalGastosFijos)
        assertEquals(1800.0, vm.state.value.totalIngresosFijos)
        assertEquals(2, vm.state.value.gastosFijos.size)
    }

    @Test
    fun `crear un fijo valida el dia y empieza hoy`() = runTest {
        val vm = crearViewModel()
        vm.nuevo(TipoMovimiento.GASTO)
        vm.actualizarEditor { it.copy(nombre = "Gimnasio", monto = "30", dia = "32") }
        assertFalse(vm.state.value.editor!!.esValido)
        vm.guardar()
        assertTrue(recurrentes.datos.value.isEmpty())

        vm.actualizarEditor { it.copy(dia = "15") }
        vm.guardar()

        val creado = recurrentes.datos.value.single()
        assertEquals(15, creado.diaDelMes)
        assertEquals(todayLocalDate(), creado.fechaInicio)
        assertNull(vm.state.value.editor)
    }

    @Test
    fun `editar conserva el id y se puede pausar y eliminar`() = runTest {
        recurrentes.datos.value = listOf(recurrente("a", TipoMovimiento.GASTO, 700.0))
        val vm = crearViewModel()

        vm.editar(recurrentes.datos.value.single())
        vm.actualizarEditor { it.copy(monto = "750,5") }
        vm.guardar()
        assertEquals(750.5, recurrentes.datos.value.single().monto)
        assertEquals("a", recurrentes.datos.value.single().id)

        vm.cambiarActivo(recurrentes.datos.value.single(), false)
        assertFalse(recurrentes.datos.value.single().activo)

        vm.editar(recurrentes.datos.value.single())
        vm.eliminarEditado()
        assertTrue(recurrentes.datos.value.isEmpty())
    }
}
