package bas.orellana.gastostracker.presentation.viewmodel

import bas.orellana.gastostracker.domain.model.Aportacion
import bas.orellana.gastostracker.domain.model.MetaAhorro
import bas.orellana.gastostracker.domain.usecase.EliminarAportacionUseCase
import bas.orellana.gastostracker.domain.usecase.EliminarMetaUseCase
import bas.orellana.gastostracker.domain.usecase.GetAportacionesUseCase
import bas.orellana.gastostracker.domain.usecase.GetProgresoMetasUseCase
import bas.orellana.gastostracker.domain.usecase.GetTotalAhorradoUseCase
import bas.orellana.gastostracker.domain.usecase.GuardarAportacionUseCase
import bas.orellana.gastostracker.domain.usecase.GuardarMetaUseCase
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
class MetasViewModelTest {

    private val ahorro = FakeAhorroRepository()
    private val hoy = todayLocalDate()

    @BeforeTest
    fun preparar() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun limpiar() = Dispatchers.resetMain()

    private fun crearViewModel() = MetasViewModel(
        getProgresoMetasUseCase = GetProgresoMetasUseCase(ahorro),
        getTotalAhorradoUseCase = GetTotalAhorradoUseCase(ahorro),
        guardarMetaUseCase = GuardarMetaUseCase(ahorro),
        guardarAportacionUseCase = GuardarAportacionUseCase(ahorro),
        ajustesRepository = FakeAjustesRepository()
    )

    private fun crearDetalle(id: String) = MetaDetalleViewModel(
        metaId = id,
        getProgresoMetasUseCase = GetProgresoMetasUseCase(ahorro),
        getAportacionesUseCase = GetAportacionesUseCase(ahorro),
        guardarMetaUseCase = GuardarMetaUseCase(ahorro),
        eliminarMetaUseCase = EliminarMetaUseCase(ahorro),
        guardarAportacionUseCase = GuardarAportacionUseCase(ahorro),
        eliminarAportacionUseCase = EliminarAportacionUseCase(ahorro)
    )

    @Test
    fun `crear una meta con objetivo opcional`() = runTest {
        val vm = crearViewModel()

        vm.nuevaMeta()
        vm.actualizarEditorMeta { it.copy(nombre = "Japón", objetivo = "3000") }
        vm.guardarMeta()
        vm.nuevaMeta()
        vm.actualizarEditorMeta { it.copy(nombre = "Colchón") }
        vm.guardarMeta()

        assertEquals(listOf(3000.0, null), ahorro.metas.value.map { it.objetivo })
        assertNull(vm.state.value.editorMeta)
        // Cada meta nueva coge un color distinto.
        assertEquals(2, ahorro.metas.value.map { it.color }.toSet().size)
    }

    @Test
    fun `un objetivo invalido no deja guardar`() = runTest {
        val vm = crearViewModel()
        vm.nuevaMeta()
        vm.actualizarEditorMeta { it.copy(nombre = "Japón", objetivo = "0") }

        assertFalse(vm.state.value.editorMeta!!.esValido)
        vm.guardarMeta()
        assertTrue(ahorro.metas.value.isEmpty())
    }

    @Test
    fun `aportar y retirar suman y restan en la meta y en el total`() = runTest {
        val meta = MetaAhorro("japon", "Japón", 1000.0, null, 0xFFEF9A9A, hoy)
        ahorro.metas.value = listOf(meta)
        val vm = crearViewModel()

        vm.aportar(meta)
        vm.actualizarEditorAportacion { it.copy(monto = "200,50") }
        vm.guardarAportacion()
        vm.aportar(meta, retirada = true)
        vm.actualizarEditorAportacion { it.copy(monto = "50") }
        vm.guardarAportacion()

        assertEquals(150.5, vm.state.value.metas.single().ahorrado)
        assertEquals(150.5, vm.state.value.totalAhorrado)
        assertEquals(150.5, vm.state.value.ahorradoEsteMes)
    }

    @Test
    fun `el detalle edita, archiva y elimina la meta con sus movimientos`() = runTest {
        ahorro.metas.value = listOf(MetaAhorro("japon", "Japón", null, null, 0xFFEF9A9A, hoy))
        ahorro.aportaciones.value = listOf(Aportacion("a", "japon", 100.0, hoy))
        val vm = crearDetalle("japon")

        assertEquals(1, vm.state.value.aportaciones.size)
        vm.editarMeta()
        vm.actualizarEditorMeta { it.copy(nombre = "Viaje a Japón") }
        vm.guardarMeta()
        assertEquals("Viaje a Japón", ahorro.metas.value.single().nombre)

        vm.cambiarArchivada()
        assertTrue(ahorro.metas.value.single().archivada)

        vm.pedirEliminar()
        vm.eliminarMeta()
        assertTrue(vm.state.value.eliminada)
        assertTrue(ahorro.metas.value.isEmpty())
        assertTrue(ahorro.aportaciones.value.isEmpty())
    }
}
