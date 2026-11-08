package bas.orellana.gastostracker.presentation.viewmodel

import bas.orellana.gastostracker.domain.model.Categoria
import bas.orellana.gastostracker.domain.model.CategoriaDestino
import bas.orellana.gastostracker.domain.model.GastoModel
import bas.orellana.gastostracker.domain.usecase.AddCategoriaPersonalizadaUseCase
import bas.orellana.gastostracker.domain.usecase.AddGastoUseCase
import bas.orellana.gastostracker.domain.usecase.ContarMovimientosCategoriaUseCase
import bas.orellana.gastostracker.domain.usecase.DeleteAllGastosUseCase
import bas.orellana.gastostracker.domain.usecase.DeleteCategoriaPersonalizadaUseCase
import bas.orellana.gastostracker.domain.usecase.DeleteGastoUseCase
import bas.orellana.gastostracker.domain.usecase.GetCategoriasPersonalizadasUseCase
import bas.orellana.gastostracker.domain.usecase.GetGastosEntreUseCase
import bas.orellana.gastostracker.domain.usecase.GetEstadoPresupuestosUseCase
import bas.orellana.gastostracker.domain.usecase.GuardarRecurrenteUseCase
import bas.orellana.gastostracker.domain.usecase.GenerarRecurrentesUseCase
import bas.orellana.gastostracker.domain.model.Presupuesto
import bas.orellana.gastostracker.domain.model.NivelPresupuesto
import bas.orellana.gastostracker.domain.usecase.GetTotalesGastosPorCategoriaUseCase
import bas.orellana.gastostracker.domain.usecase.UpdateCategoriaPersonalizadaUseCase
import bas.orellana.gastostracker.domain.usecase.UpdateGastoUseCase
import bas.orellana.gastostracker.domain.util.todayLocalDate
import bas.orellana.gastostracker.presentation.state.SortOrder
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
import bas.orellana.gastostracker.domain.model.Mes
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val gastos = FakeGastoRepository()
    private val ingresos = FakeIngresoRepository()
    private val categorias = FakeCategoriasRepository(gastos, ingresos)
    private val presupuestos = FakePresupuestoRepository()
    private val recurrentes = FakeRecurrenteRepository(gastos, ingresos)

    private val hoy = todayLocalDate()
    private val inicioMes = LocalDate(hoy.year, hoy.month, 1)
    private val inicioMesAnterior = inicioMes.minus(1, DateTimeUnit.MONTH)

    @BeforeTest
    fun preparar() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun limpiar() = Dispatchers.resetMain()

    private fun crearViewModel() = HomeViewModel(
        getGastosEntreUseCase = GetGastosEntreUseCase(gastos),
        addGastoUseCase = AddGastoUseCase(gastos),
        updateGastoUseCase = UpdateGastoUseCase(gastos),
        deleteGastoUseCase = DeleteGastoUseCase(gastos),
        deleteAllGastosUseCase = DeleteAllGastosUseCase(gastos),
        getCategoriasPersonalizadasUseCase = GetCategoriasPersonalizadasUseCase(categorias),
        addCategoriaPersonalizadaUseCase = AddCategoriaPersonalizadaUseCase(categorias),
        updateCategoriaPersonalizadaUseCase = UpdateCategoriaPersonalizadaUseCase(categorias),
        deleteCategoriaPersonalizadaUseCase = DeleteCategoriaPersonalizadaUseCase(categorias),
        contarMovimientosCategoriaUseCase = ContarMovimientosCategoriaUseCase(categorias),
        getTotalesGastosPorCategoriaUseCase = GetTotalesGastosPorCategoriaUseCase(gastos),
        getEstadoPresupuestosUseCase = GetEstadoPresupuestosUseCase(presupuestos, gastos),
        guardarRecurrenteUseCase = GuardarRecurrenteUseCase(recurrentes),
        generarRecurrentesUseCase = GenerarRecurrentesUseCase(recurrentes)
    )

    private fun gasto(
        id: String,
        nombre: String = "Gasto $id",
        monto: Double = 10.0,
        fecha: LocalDate = inicioMes,
        categoria: Categoria? = null,
        personalizada: String? = null
    ) = GastoModel(id, nombre, monto, fecha, categoria, personalizada)

    private fun anadir(vararg nuevos: GastoModel) {
        gastos.datos.value = gastos.datos.value + nuevos
    }

    private val HomeViewModel.idsVisibles get() = state.value.gastosVisibles.map { it.id }

    // ---------- Carga ----------

    @Test
    fun `muestra los gastos del repositorio y se actualiza cuando cambian`() = runTest {
        anadir(gasto("a"))
        val vm = crearViewModel()
        assertEquals(listOf("a"), vm.state.value.gastos.map { it.id })
        assertFalse(vm.state.value.isLoading)

        anadir(gasto("b"))

        assertEquals(setOf("a", "b"), vm.state.value.gastos.map { it.id }.toSet())
    }

    // ---------- Búsqueda ----------

    @Test
    fun `la busqueda filtra por nombre sin distinguir mayusculas e ignora espacios de los extremos`() = runTest {
        anadir(gasto("1", nombre = "Café con leche"), gasto("2", nombre = "Gasolina"), gasto("3", nombre = "CAFETERÍA"))
        val vm = crearViewModel()

        vm.updateSearchQuery("  café ")
        assertEquals(setOf("1"), vm.idsVisibles.toSet())

        vm.updateSearchQuery("caf")
        assertEquals(setOf("1", "3"), vm.idsVisibles.toSet())
        assertTrue(vm.state.value.hayBusquedaOFiltro)

        vm.updateSearchQuery("   ")
        assertEquals(3, vm.idsVisibles.size)
        assertFalse(vm.state.value.hayBusquedaOFiltro)
    }

    // ---------- Filtros ----------

    @Test
    fun `filtra por categoria predefinida o personalizada, nunca por las dos a la vez`() = runTest {
        anadir(
            gasto("comida", categoria = Categoria.COMIDA),
            gasto("ocio", categoria = Categoria.OCIO),
            gasto("gato", personalizada = "cat-gato")
        )
        val vm = crearViewModel()

        vm.updateFilterCategoria(Categoria.COMIDA)
        assertEquals(listOf("comida"), vm.idsVisibles)

        vm.updateFilterCategoriaPersonalizada("cat-gato")
        assertNull(vm.state.value.filterCategoria)
        assertEquals(listOf("gato"), vm.idsVisibles)

        vm.updateFilterCategoria(Categoria.OCIO)
        assertNull(vm.state.value.filterCategoriaPersonalizadaId)
        assertEquals(listOf("ocio"), vm.idsVisibles)
    }

    @Test
    fun `la busqueda y el filtro se combinan y clearFilters quita los dos`() = runTest {
        anadir(
            gasto("1", nombre = "Súper", categoria = Categoria.COMIDA),
            gasto("2", nombre = "Restaurante", categoria = Categoria.COMIDA),
            gasto("3", nombre = "Súper ocio", categoria = Categoria.OCIO)
        )
        val vm = crearViewModel()

        vm.updateFilterCategoria(Categoria.COMIDA)
        vm.updateSearchQuery("súper")
        assertEquals(listOf("1"), vm.idsVisibles)

        vm.clearFilters()

        assertEquals(3, vm.idsVisibles.size)
        assertEquals("", vm.state.value.searchQuery)
        assertFalse(vm.state.value.hayFiltroCategoria)
    }

    // ---------- Orden ----------

    @Test
    fun `ordena por fecha, importe y nombre`() = runTest {
        anadir(
            gasto("b", nombre = "banana", monto = 5.0, fecha = inicioMes.plus(1, DateTimeUnit.DAY)),
            gasto("a", nombre = "Agua", monto = 20.0, fecha = inicioMes.plus(2, DateTimeUnit.DAY)),
            gasto("c", nombre = "coco", monto = 1.0, fecha = inicioMes)
        )
        val vm = crearViewModel()

        val esperado = mapOf(
            SortOrder.DATE_DESC to listOf("a", "b", "c"),
            SortOrder.DATE_ASC to listOf("c", "b", "a"),
            SortOrder.AMOUNT_DESC to listOf("a", "b", "c"),
            SortOrder.AMOUNT_ASC to listOf("c", "b", "a"),
            SortOrder.NAME_ASC to listOf("a", "b", "c"),
            SortOrder.NAME_DESC to listOf("c", "b", "a")
        )
        esperado.forEach { (orden, ids) ->
            vm.updateSortOrder(orden)
            assertEquals(ids, vm.idsVisibles, "Orden $orden")
        }
    }

    @Test
    fun `con la misma fecha se mantiene el orden del repositorio, el ultimo anadido primero`() = runTest {
        anadir(gasto("primero"), gasto("segundo"), gasto("tercero"))
        val vm = crearViewModel()

        assertEquals(listOf("tercero", "segundo", "primero"), vm.idsVisibles)
    }


    // ---------- Selector de mes ----------

    @Test
    fun `solo muestra los gastos del mes seleccionado y se puede cambiar de mes`() = runTest {
        anadir(gasto("actual", fecha = inicioMes), gasto("anterior", fecha = inicioMesAnterior))
        val vm = crearViewModel()

        assertEquals(Mes.actual(), vm.state.value.mes)
        assertEquals(listOf("actual"), vm.state.value.gastos.map { it.id })

        vm.mesAnterior()
        assertEquals(Mes.actual().anterior(), vm.state.value.mes)
        assertEquals(listOf("anterior"), vm.state.value.gastos.map { it.id })

        vm.mesSiguiente()
        vm.mesSiguiente()
        assertTrue(vm.state.value.gastos.isEmpty())
    }

    @Test
    fun `elegir un mes en el selector lo aplica y cierra el dialogo`() = runTest {
        anadir(gasto("viejo", fecha = LocalDate(2024, 3, 15)))
        val vm = crearViewModel()

        vm.showMonthPicker()
        assertTrue(vm.state.value.showMonthPicker)
        vm.seleccionarMes(Mes(2024, 3))

        assertFalse(vm.state.value.showMonthPicker)
        assertEquals(listOf("viejo"), vm.state.value.gastos.map { it.id })
    }

    @Test
    fun `las alertas comparan el mes seleccionado con su mes anterior`() = runTest {
        val mesPasado = Mes.actual().anterior()
        anadir(
            gasto("hace2", monto = 10.0, fecha = mesPasado.anterior().primerDia, categoria = Categoria.OCIO),
            gasto("hace1", monto = 30.0, fecha = mesPasado.primerDia, categoria = Categoria.OCIO)
        )
        val vm = crearViewModel()
        assertTrue(vm.state.value.alerts.isEmpty())

        vm.seleccionarMes(mesPasado)

        val alerta = vm.state.value.alerts.single()
        assertEquals(Categoria.OCIO, alerta.categoria)
        assertEquals(30.0, alerta.gastoActual)
    }


    // ---------- Presupuestos ----------

    @Test
    fun `muestra como van los presupuestos en el mes seleccionado`() = runTest {
        presupuestos.datos.value = listOf(Presupuesto(Categoria.COMIDA, null, 100.0))
        anadir(
            gasto("c1", monto = 90.0, fecha = inicioMes, categoria = Categoria.COMIDA),
            gasto("c0", monto = 20.0, fecha = inicioMesAnterior, categoria = Categoria.COMIDA)
        )
        val vm = crearViewModel()

        val estado = vm.state.value.presupuestos.single()
        assertEquals(90.0, estado.gastado)
        assertEquals(NivelPresupuesto.AVISO, estado.nivel)

        vm.mesAnterior()
        assertEquals(NivelPresupuesto.BIEN, vm.state.value.presupuestos.single().nivel)
    }


    // ---------- Recurrentes ----------

    @Test
    fun `guardar con repetir cada mes crea el recurrente y el gasto de este mes`() = runTest {
        val vm = crearViewModel()
        vm.showAddGastoDialog()
        vm.updateAddGastoFecha(inicioMes)
        vm.updateAddGastoRepetir(true)

        vm.saveGasto("Netflix", "12,99", Categoria.OCIO, null)

        val recurrente = recurrentes.datos.value.single()
        assertEquals(1, recurrente.diaDelMes)
        assertEquals(inicioMes, recurrente.fechaInicio)
        val gasto = gastos.datos.value.single()
        assertEquals(12.99, gasto.monto)
        assertEquals(recurrente.idMovimiento(Mes.actual()), gasto.id)
        assertFalse(vm.state.value.showAddGastoDialog)
    }

    // ---------- Alertas ----------

    @Test
    fun `avisa de las categorias en las que este mes se gasta mas que el anterior`() = runTest {
        anadir(
            gasto("c1", monto = 50.0, fecha = inicioMesAnterior, categoria = Categoria.COMIDA),
            gasto("c2", monto = 80.0, fecha = inicioMes, categoria = Categoria.COMIDA),
            // Menos que el mes pasado: sin alerta.
            gasto("o1", monto = 40.0, fecha = inicioMesAnterior, categoria = Categoria.OCIO),
            gasto("o2", monto = 10.0, fecha = inicioMes, categoria = Categoria.OCIO),
            // Sin gasto el mes pasado: sin alerta.
            gasto("s1", monto = 30.0, fecha = inicioMes, categoria = Categoria.SALUD)
        )
        val vm = crearViewModel()

        val alerta = vm.state.value.alerts.single()
        assertEquals(Categoria.COMIDA, alerta.categoria)
        assertEquals(80.0, alerta.gastoActual)
        assertEquals(50.0, alerta.gastoAnterior)
    }

    @Test
    fun `el ahorro nunca genera alertas`() = runTest {
        anadir(
            gasto("a1", monto = 100.0, fecha = inicioMesAnterior, categoria = Categoria.AHORRO),
            gasto("a2", monto = 300.0, fecha = inicioMes, categoria = Categoria.AHORRO)
        )
        val vm = crearViewModel()

        assertTrue(vm.state.value.alerts.isEmpty())
    }

    @Test
    fun `las alertas de categorias personalizadas llevan la categoria`() = runTest {
        categorias.addCategoriaPersonalizada("Mascotas", 0xFFEF9A9A)
        val id = categorias.datos.value.single().id
        anadir(
            gasto("p1", monto = 10.0, fecha = inicioMesAnterior, personalizada = id),
            gasto("p2", monto = 20.0, fecha = inicioMes, personalizada = id)
        )
        val vm = crearViewModel()

        val alerta = vm.state.value.alerts.single()
        assertEquals("Mascotas", alerta.categoriaPersonalizada?.nombre)
        assertTrue(alerta.clave.endsWith(id))
    }

    @Test
    fun `una alerta descartada no vuelve a salir aunque cambien los datos`() = runTest {
        anadir(
            gasto("c1", monto = 50.0, fecha = inicioMesAnterior, categoria = Categoria.COMIDA),
            gasto("c2", monto = 80.0, fecha = inicioMes, categoria = Categoria.COMIDA)
        )
        val vm = crearViewModel()

        vm.dismissAlert(vm.state.value.alerts.single().clave)
        assertTrue(vm.state.value.alerts.isEmpty())

        anadir(gasto("c3", monto = 5.0, fecha = inicioMes, categoria = Categoria.COMIDA))
        assertTrue(vm.state.value.alerts.isEmpty())
    }

    // ---------- Guardar ----------

    @Test
    fun `guarda un gasto con coma decimal`() = runTest {
        val vm = crearViewModel()
        vm.showAddGastoDialog()

        vm.saveGasto("Pan", "12,50", Categoria.COMIDA, null)

        val guardado = gastos.datos.value.single()
        assertEquals(12.5, guardado.monto)
        assertEquals(Categoria.COMIDA, guardado.categoria)
        assertFalse(vm.state.value.showAddGastoDialog)
    }

    @Test
    fun `no guarda un gasto con importe invalido o cero`() = runTest {
        val vm = crearViewModel()

        vm.saveGasto("Pan", "0", null, null)
        vm.saveGasto("Pan", "abc", null, null)
        vm.saveGasto("Pan", "", null, null)

        assertTrue(gastos.datos.value.isEmpty())
    }

    @Test
    fun `editar un gasto conserva su id`() = runTest {
        anadir(gasto("a", nombre = "Pan", monto = 1.0))
        val vm = crearViewModel()

        vm.showEditGastoDialog(gastos.datos.value.single())
        assertEquals("1", vm.addGastoState.value.precio)
        vm.saveGasto("Pan integral", "1,75", null, null)

        val editado = gastos.datos.value.single()
        assertEquals("a", editado.id)
        assertEquals("Pan integral", editado.nombre)
        assertEquals(1.75, editado.monto)
    }

    // ---------- Borrar categorías personalizadas ----------

    @Test
    fun `una categoria sin movimientos se borra sin preguntar`() = runTest {
        categorias.addCategoriaPersonalizada("Mascotas", 0xFFEF9A9A)
        val vm = crearViewModel()

        vm.solicitarEliminarCategoria(categorias.datos.value.single())

        assertTrue(categorias.datos.value.isEmpty())
        assertNull(vm.manageCategoriasState.value.categoriaAEliminar)
    }

    @Test
    fun `una categoria con movimientos pregunta a donde pasarlos y por defecto los pasa a Otros`() = runTest {
        categorias.addCategoriaPersonalizada("Mascotas", 0xFFEF9A9A)
        val mascotas = categorias.datos.value.single()
        anadir(gasto("g1", personalizada = mascotas.id), gasto("g2", personalizada = mascotas.id))
        val vm = crearViewModel()

        vm.solicitarEliminarCategoria(mascotas)

        val pregunta = vm.manageCategoriasState.value
        assertEquals(mascotas, pregunta.categoriaAEliminar)
        assertEquals(2, pregunta.movimientosCategoriaAEliminar)
        assertEquals(CategoriaDestino.POR_DEFECTO, pregunta.destinoReasignacion)
        assertEquals(1, categorias.datos.value.size, "Todavía no se ha borrado")

        vm.confirmarEliminarCategoria()

        assertTrue(categorias.datos.value.isEmpty())
        assertTrue(gastos.datos.value.all { it.categoria == Categoria.OTROS && it.categoriaPersonalizadaId == null })
        assertNull(vm.manageCategoriasState.value.categoriaAEliminar)
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
        assertNull(vm.manageCategoriasState.value.categoriaAEliminar)
    }

    @Test
    fun `al borrar la categoria por la que se filtra se quita el filtro`() = runTest {
        categorias.addCategoriaPersonalizada("Mascotas", 0xFFEF9A9A)
        val mascotas = categorias.datos.value.single()
        val vm = crearViewModel()
        vm.updateFilterCategoriaPersonalizada(mascotas.id)

        vm.solicitarEliminarCategoria(mascotas)

        assertNull(vm.state.value.filterCategoriaPersonalizadaId)
    }
}
