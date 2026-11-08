package bas.orellana.gastostracker.presentation.viewmodel

import bas.orellana.gastostracker.domain.model.GastoModel
import bas.orellana.gastostracker.domain.model.TipoCuenta
import bas.orellana.gastostracker.domain.usecase.EliminarCuentaUseCase
import bas.orellana.gastostracker.domain.usecase.GetSaldosCuentasUseCase
import bas.orellana.gastostracker.domain.usecase.GuardarCuentaUseCase
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
class CuentasViewModelTest {

    private val gastos = FakeGastoRepository()
    private val ingresos = FakeIngresoRepository()
    private val cuentas = FakeCuentaRepository(gastos, ingresos)

    @BeforeTest
    fun preparar() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun limpiar() = Dispatchers.resetMain()

    private fun crearViewModel() = CuentasViewModel(
        GetSaldosCuentasUseCase(cuentas),
        GuardarCuentaUseCase(cuentas),
        EliminarCuentaUseCase(cuentas)
    )

    @Test
    fun `crear una tarjeta con saldo inicial negativo y ver su saldo`() = runTest {
        val vm = crearViewModel()
        vm.nueva()
        vm.actualizarEditor { it.copy(nombre = "Visa", tipo = TipoCuenta.TARJETA_CREDITO, saldoInicial = "-150,5") }
        vm.guardar()

        val cuenta = cuentas.cuentas.value.single()
        assertEquals(-150.5, cuenta.saldoInicial)
        gastos.addGasto(GastoModel("g", "Cena", 49.5, todayLocalDate(), cuentaId = cuenta.id))
        assertEquals(-200.0, vm.state.value.saldoTotal)
    }

    @Test
    fun `un saldo inicial que no es un numero no deja guardar`() = runTest {
        val vm = crearViewModel()
        vm.nueva()
        vm.actualizarEditor { it.copy(nombre = "Banco", saldoInicial = "-") }

        assertFalse(vm.state.value.editor!!.esValido)
        vm.guardar()
        assertTrue(cuentas.cuentas.value.isEmpty())
    }

    @Test
    fun `eliminar pide confirmacion y deja los gastos sin cuenta`() = runTest {
        val vm = crearViewModel()
        vm.nueva()
        vm.actualizarEditor { it.copy(nombre = "Banco") }
        vm.guardar()
        val cuenta = cuentas.cuentas.value.single()
        gastos.addGasto(GastoModel("g", "Luz", 60.0, todayLocalDate(), cuentaId = cuenta.id))

        vm.editar(cuenta)
        vm.pedirEliminar()
        assertEquals(cuenta, vm.state.value.cuentaAEliminar)
        vm.confirmarEliminar()

        assertTrue(cuentas.cuentas.value.isEmpty())
        assertNull(gastos.datos.value.single().cuentaId)
    }
}
