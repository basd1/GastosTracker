package bas.orellana.gastostracker.presentation.viewmodel

import bas.orellana.gastostracker.domain.model.Categoria
import bas.orellana.gastostracker.domain.model.GastoModel
import bas.orellana.gastostracker.domain.model.Presupuesto
import bas.orellana.gastostracker.domain.usecase.EliminarPresupuestoUseCase
import bas.orellana.gastostracker.domain.usecase.GetCategoriasPersonalizadasUseCase
import bas.orellana.gastostracker.domain.usecase.GetEstadoPresupuestosUseCase
import bas.orellana.gastostracker.domain.usecase.GuardarPresupuestoUseCase
import bas.orellana.gastostracker.domain.model.Mes
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
class PresupuestosViewModelTest {

    private val gastos = FakeGastoRepository()
    private val ingresos = FakeIngresoRepository()
    private val categorias = FakeCategoriasRepository(gastos, ingresos)
    private val presupuestos = FakePresupuestoRepository()

    @BeforeTest
    fun preparar() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun limpiar() = Dispatchers.resetMain()

    private fun crearViewModel() = PresupuestosViewModel(
        getEstadoPresupuestosUseCase = GetEstadoPresupuestosUseCase(presupuestos, gastos),
        guardarPresupuestoUseCase = GuardarPresupuestoUseCase(presupuestos),
        eliminarPresupuestoUseCase = EliminarPresupuestoUseCase(presupuestos),
        getCategoriasPersonalizadasUseCase = GetCategoriasPersonalizadasUseCase(categorias)
    )

    @Test
    fun `crear un presupuesto empieza por el global y acepta coma decimal`() = runTest {
        val vm = crearViewModel()

        vm.nuevoPresupuesto()
        assertEquals(Presupuesto.CLAVE_GLOBAL, vm.state.value.editor?.clave)
        vm.cambiarMonto("1200,50")
        vm.guardar()

        assertEquals(listOf(Presupuesto.global(1200.5)), presupuestos.datos.value)
        assertNull(vm.state.value.editor)
        assertFalse(vm.state.value.globalDisponible)
    }

    @Test
    fun `no se guarda un importe invalido`() = runTest {
        val vm = crearViewModel()
        vm.nuevoPresupuesto()
        vm.elegirCategoria(Categoria.OCIO, null)
        vm.cambiarMonto("0")
        vm.guardar()

        assertTrue(presupuestos.datos.value.isEmpty())
    }

    @Test
    fun `editar no cambia la categoria y se puede eliminar`() = runTest {
        presupuestos.datos.value = listOf(Presupuesto(Categoria.COMIDA, null, 300.0))
        val vm = crearViewModel()

        vm.editar(presupuestos.datos.value.single())
        assertEquals("300", vm.state.value.editor?.monto)
        vm.elegirCategoria(Categoria.OCIO, null)
        assertEquals(Categoria.COMIDA, vm.state.value.editor?.categoria)
        vm.cambiarMonto("250")
        vm.guardar()
        assertEquals(listOf(Presupuesto(Categoria.COMIDA, null, 250.0)), presupuestos.datos.value)

        vm.editar(presupuestos.datos.value.single())
        vm.eliminarEditado()
        assertTrue(presupuestos.datos.value.isEmpty())
    }

    @Test
    fun `las categorias con presupuesto no se ofrecen al crear otro`() = runTest {
        presupuestos.datos.value = listOf(Presupuesto(Categoria.COMIDA, null, 300.0))
        val vm = crearViewModel()

        val disponibles = vm.state.value.categoriasDisponibles
        assertFalse(Categoria.COMIDA in disponibles)
        assertTrue(Categoria.OCIO in disponibles)
    }

    @Test
    fun `el estado sigue al mes elegido`() = runTest {
        presupuestos.datos.value = listOf(Presupuesto(Categoria.COMIDA, null, 100.0))
        val anterior = Mes.actual().anterior()
        gastos.datos.value = listOf(GastoModel("g", "Súper", 150.0, anterior.primerDia, Categoria.COMIDA))
        val vm = crearViewModel()

        assertEquals(0.0, vm.state.value.estados.single().gastado)
        vm.mesAnterior()
        assertEquals(150.0, vm.state.value.estados.single().gastado)
    }
}
