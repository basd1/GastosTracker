package bas.orellana.gastostracker.presentation.viewmodel

import bas.orellana.gastostracker.domain.model.Categoria
import bas.orellana.gastostracker.domain.model.CategoriaDestino
import bas.orellana.gastostracker.domain.model.GastoModel
import bas.orellana.gastostracker.domain.usecase.AddCategoriaPersonalizadaUseCase
import bas.orellana.gastostracker.domain.usecase.ContarMovimientosCategoriaUseCase
import bas.orellana.gastostracker.domain.usecase.DeleteCategoriaPersonalizadaUseCase
import bas.orellana.gastostracker.domain.usecase.GetCategoriasPersonalizadasUseCase
import bas.orellana.gastostracker.domain.usecase.UpdateCategoriaPersonalizadaUseCase
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
class CategoriasViewModelTest {

    private val gastos = FakeGastoRepository()
    private val ingresos = FakeIngresoRepository()
    private val categorias = FakeCategoriasRepository(gastos, ingresos)

    @BeforeTest
    fun preparar() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun limpiar() = Dispatchers.resetMain()

    private fun crearViewModel() = CategoriasViewModel(
        getCategoriasPersonalizadasUseCase = GetCategoriasPersonalizadasUseCase(categorias),
        addCategoriaPersonalizadaUseCase = AddCategoriaPersonalizadaUseCase(categorias),
        updateCategoriaPersonalizadaUseCase = UpdateCategoriaPersonalizadaUseCase(categorias),
        deleteCategoriaPersonalizadaUseCase = DeleteCategoriaPersonalizadaUseCase(categorias),
        contarMovimientosCategoriaUseCase = ContarMovimientosCategoriaUseCase(categorias)
    )

    private fun gasto(id: String, personalizada: String? = null) =
        GastoModel(id, "Gasto $id", 10.0, todayLocalDate(), null, personalizada)

    private fun anadir(vararg nuevos: GastoModel) {
        gastos.datos.value = gastos.datos.value + nuevos
    }

    @Test
    fun `crea una categoria con el nombre y el color elegidos y vacia el formulario`() = runTest {
        val vm = crearViewModel()
        vm.mostrarDialogo()
        vm.updateNuevaCategoria("  Mascotas ")
        vm.updateColorSeleccionado(0xFF80DEEA)

        vm.addCategoria()

        val creada = categorias.datos.value.single()
        assertEquals("Mascotas", creada.nombre)
        assertEquals(0xFF80DEEA, creada.color)
        assertEquals(listOf(creada), vm.categoriasPersonalizadas.value)
        assertEquals("", vm.state.value.nuevaCategoria)
        assertTrue(vm.state.value.visible)
    }

    @Test
    fun `editar una categoria cambia su nombre y color`() = runTest {
        categorias.addCategoriaPersonalizada("Mascotas", 0xFFEF9A9A)
        val vm = crearViewModel()

        vm.showEditCategoriaDialog(categorias.datos.value.single())
        vm.updateEditNombre("Animales")
        vm.updateEditColor(0xFF80DEEA)
        vm.saveEditCategoria()

        val editada = categorias.datos.value.single()
        assertEquals("Animales", editada.nombre)
        assertEquals(0xFF80DEEA, editada.color)
        assertNull(vm.state.value.editCategoria)
    }

    @Test
    fun `cerrar el dialogo descarta lo que se estaba escribiendo`() = runTest {
        val vm = crearViewModel()
        vm.mostrarDialogo()
        vm.updateNuevaCategoria("Mascotas")

        vm.ocultarDialogo()

        assertFalse(vm.state.value.visible)
        assertEquals("", vm.state.value.nuevaCategoria)
    }

    @Test
    fun `una categoria sin movimientos se borra sin preguntar`() = runTest {
        categorias.addCategoriaPersonalizada("Mascotas", 0xFFEF9A9A)
        val vm = crearViewModel()

        vm.solicitarEliminarCategoria(categorias.datos.value.single())

        assertTrue(categorias.datos.value.isEmpty())
        assertNull(vm.state.value.categoriaAEliminar)
    }

    @Test
    fun `una categoria con movimientos pregunta a donde pasarlos y por defecto los pasa a Otros`() = runTest {
        categorias.addCategoriaPersonalizada("Mascotas", 0xFFEF9A9A)
        val mascotas = categorias.datos.value.single()
        anadir(gasto("g1", personalizada = mascotas.id), gasto("g2", personalizada = mascotas.id))
        val vm = crearViewModel()

        vm.solicitarEliminarCategoria(mascotas)

        val pregunta = vm.state.value
        assertEquals(mascotas, pregunta.categoriaAEliminar)
        assertEquals(2, pregunta.movimientosCategoriaAEliminar)
        assertEquals(CategoriaDestino.POR_DEFECTO, pregunta.destinoReasignacion)
        assertEquals(1, categorias.datos.value.size, "Todavía no se ha borrado")

        vm.confirmarEliminarCategoria()

        assertTrue(categorias.datos.value.isEmpty())
        assertTrue(gastos.datos.value.all { it.categoria == Categoria.OTROS && it.categoriaPersonalizadaId == null })
        assertNull(vm.state.value.categoriaAEliminar)
    }

    @Test
    fun `se puede elegir otra categoria personalizada como destino`() = runTest {
        categorias.addCategoriaPersonalizada("Mascotas", 0xFFEF9A9A)
        categorias.addCategoriaPersonalizada("Animales", 0xFF80DEEA)
        val (mascotas, animales) = categorias.datos.value
        anadir(gasto("g1", personalizada = mascotas.id))
        val vm = crearViewModel()

        vm.solicitarEliminarCategoria(mascotas)
        vm.updateDestinoReasignacion(CategoriaDestino.Personalizada(animales.id))
        vm.confirmarEliminarCategoria()

        assertEquals(listOf(animales), categorias.datos.value)
        assertEquals(animales.id, gastos.datos.value.single().categoriaPersonalizadaId)
    }

    @Test
    fun `cancelar el borrado no toca nada`() = runTest {
        categorias.addCategoriaPersonalizada("Mascotas", 0xFFEF9A9A)
        val mascotas = categorias.datos.value.single()
        anadir(gasto("g1", personalizada = mascotas.id))
        val vm = crearViewModel()

        vm.solicitarEliminarCategoria(mascotas)
        vm.cancelarEliminarCategoria()

        assertEquals(listOf(mascotas), categorias.datos.value)
        assertEquals(mascotas.id, gastos.datos.value.single().categoriaPersonalizadaId)
        assertNull(vm.state.value.categoriaAEliminar)
    }
}
