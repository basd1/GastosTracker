package bas.orellana.gastostracker.presentation.viewmodel

import bas.orellana.gastostracker.domain.model.Categoria
import bas.orellana.gastostracker.domain.model.GastoModel
import bas.orellana.gastostracker.domain.model.IngresoModel
import bas.orellana.gastostracker.domain.usecase.AddIngresoUseCase
import bas.orellana.gastostracker.domain.usecase.DeleteIngresoUseCase
import bas.orellana.gastostracker.domain.usecase.GetIngresosUseCase
import bas.orellana.gastostracker.domain.usecase.GetTotalIngresosUseCase
import bas.orellana.gastostracker.domain.usecase.GetTotalesGastosPorCategoriaUseCase
import bas.orellana.gastostracker.domain.usecase.UpdateIngresoUseCase
import bas.orellana.gastostracker.domain.util.todayLocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class BalanceViewModelTest {

    private val gastos = FakeGastoRepository()
    private val ingresos = FakeIngresoRepository()

    private val hoy = todayLocalDate()
    private val inicioMes = LocalDate(hoy.year, hoy.month, 1)
    private val finMes = inicioMes.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)
    private val mesAnterior = inicioMes.minus(1, DateTimeUnit.MONTH)

    @BeforeTest
    fun preparar() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun limpiar() = Dispatchers.resetMain()

    private fun crearViewModel() = BalanceViewModel(
        getIngresosUseCase = GetIngresosUseCase(ingresos),
        addIngresoUseCase = AddIngresoUseCase(ingresos),
        updateIngresoUseCase = UpdateIngresoUseCase(ingresos),
        deleteIngresoUseCase = DeleteIngresoUseCase(ingresos),
        getTotalIngresosUseCase = GetTotalIngresosUseCase(ingresos),
        getTotalesGastosPorCategoriaUseCase = GetTotalesGastosPorCategoriaUseCase(gastos)
    )

    private fun ingreso(id: String, monto: Double, fecha: LocalDate) = IngresoModel(id, "Ingreso $id", monto, fecha)
    private fun gasto(id: String, monto: Double, fecha: LocalDate, categoria: Categoria? = null) =
        GastoModel(id, "Gasto $id", monto, fecha, categoria)

    @Test
    fun `por defecto muestra el mes actual`() = runTest {
        val vm = crearViewModel()

        assertEquals(hoy.monthNumber, vm.state.value.selectedMonth)
        assertEquals(hoy.year, vm.state.value.selectedYear)
    }

    @Test
    fun `los totales del mes separan ingresos, gastos y ahorro, extremos del mes incluidos`() = runTest {
        ingresos.datos.value = listOf(
            ingreso("nomina", 1500.0, inicioMes),
            ingreso("extra", 200.0, finMes),
            ingreso("otro-mes", 999.0, mesAnterior)
        )
        gastos.datos.value = listOf(
            gasto("super", 100.0, inicioMes, Categoria.COMIDA),
            gasto("sin-cat", 20.5, finMes),
            gasto("hucha", 300.0, inicioMes, Categoria.AHORRO),
            gasto("otro-mes", 50.0, mesAnterior, Categoria.COMIDA)
        )

        val vm = crearViewModel()

        assertEquals(1700.0, vm.state.value.totalIngresosMes)
        assertEquals(120.5, vm.state.value.totalGastosMes)
        assertEquals(300.0, vm.state.value.totalAhorroMes)
    }

    @Test
    fun `al cambiar de mes se recalculan los totales`() = runTest {
        ingresos.datos.value = listOf(ingreso("actual", 1000.0, inicioMes), ingreso("anterior", 800.0, mesAnterior))
        gastos.datos.value = listOf(gasto("anterior", 60.0, mesAnterior), gasto("ahorro", 40.0, mesAnterior, Categoria.AHORRO))
        val vm = crearViewModel()

        vm.updateSelectedMonth(mesAnterior.monthNumber, mesAnterior.year)

        assertEquals(800.0, vm.state.value.totalIngresosMes)
        assertEquals(60.0, vm.state.value.totalGastosMes)
        assertEquals(40.0, vm.state.value.totalAhorroMes)
        assertFalse(vm.state.value.showMonthPicker)
    }

    @Test
    fun `los totales se actualizan al anadir movimientos`() = runTest {
        val vm = crearViewModel()
        assertEquals(0.0, vm.state.value.totalIngresosMes)

        vm.updateFecha(inicioMes)
        vm.saveIngreso("Nómina", "1234,5")

        assertEquals(1234.5, vm.state.value.totalIngresosMes)
        assertEquals(listOf("Nómina"), vm.state.value.ingresos.map { it.nombre })
    }

    @Test
    fun `no guarda un ingreso con importe invalido`() = runTest {
        val vm = crearViewModel()

        vm.saveIngreso("Nómina", "-5")
        vm.saveIngreso("Nómina", "0,00")

        assertTrue(ingresos.datos.value.isEmpty())
    }

    @Test
    fun `editar un ingreso conserva su id y precarga el importe con coma`() = runTest {
        ingresos.datos.value = listOf(ingreso("a", 1500.5, inicioMes))
        val vm = crearViewModel()

        vm.showEditIngresoDialog(ingresos.datos.value.single())
        assertEquals("1500,5", vm.addIngresoState.value.monto)
        vm.saveIngreso("Nómina", "1600")

        val editado = ingresos.datos.value.single()
        assertEquals("a", editado.id)
        assertEquals(1600.0, editado.monto)
    }

    @Test
    fun `borrar y restaurar un ingreso`() = runTest {
        val original = ingreso("a", 100.0, inicioMes)
        ingresos.datos.value = listOf(original)
        val vm = crearViewModel()

        vm.deleteIngreso("a")
        assertTrue(vm.state.value.ingresos.isEmpty())
        assertEquals(0.0, vm.state.value.totalIngresosMes)

        vm.restoreIngreso(original)
        assertEquals(listOf(original), vm.state.value.ingresos)
    }
}
