package bas.orellana.gastostracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bas.orellana.gastostracker.domain.model.Categoria
import bas.orellana.gastostracker.domain.model.CategoriaDestino
import bas.orellana.gastostracker.domain.model.CategoriaPersonalizada
import bas.orellana.gastostracker.domain.model.GastoModel
import bas.orellana.gastostracker.domain.model.Mes
import bas.orellana.gastostracker.domain.model.MetodoPago
import bas.orellana.gastostracker.domain.model.TotalPorCategoria
import bas.orellana.gastostracker.domain.usecase.AddCategoriaPersonalizadaUseCase
import bas.orellana.gastostracker.domain.usecase.AddGastoUseCase
import bas.orellana.gastostracker.domain.usecase.ContarMovimientosCategoriaUseCase
import bas.orellana.gastostracker.domain.usecase.DeleteCategoriaPersonalizadaUseCase
import bas.orellana.gastostracker.domain.usecase.DeleteAllGastosUseCase
import bas.orellana.gastostracker.domain.usecase.UpdateCategoriaPersonalizadaUseCase
import bas.orellana.gastostracker.domain.usecase.DeleteGastoUseCase
import bas.orellana.gastostracker.domain.usecase.GetCategoriasPersonalizadasUseCase
import bas.orellana.gastostracker.domain.usecase.GetGastosEntreUseCase
import bas.orellana.gastostracker.domain.usecase.GetEstadoPresupuestosUseCase
import bas.orellana.gastostracker.domain.usecase.GuardarRecurrenteUseCase
import bas.orellana.gastostracker.domain.usecase.GenerarRecurrentesUseCase
import bas.orellana.gastostracker.domain.repository.AjustesRepository
import bas.orellana.gastostracker.domain.model.calcularResumenMensual
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import bas.orellana.gastostracker.presentation.util.diaInicioMes
import bas.orellana.gastostracker.domain.model.Recurrente
import bas.orellana.gastostracker.domain.model.TipoMovimiento
import bas.orellana.gastostracker.domain.usecase.GetTotalesGastosPorCategoriaUseCase
import bas.orellana.gastostracker.domain.usecase.UpdateGastoUseCase
import bas.orellana.gastostracker.presentation.state.AddGastoState
import bas.orellana.gastostracker.presentation.state.CategoryAlert
import bas.orellana.gastostracker.presentation.state.HomeState
import bas.orellana.gastostracker.presentation.state.ManageCategoriasState
import bas.orellana.gastostracker.presentation.state.SortOrder
import bas.orellana.gastostracker.presentation.state.FiltroGastos
import bas.orellana.gastostracker.presentation.navigation.PeticionFiltro
import bas.orellana.gastostracker.presentation.util.parseImporte
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import bas.orellana.gastostracker.presentation.util.randomId
import bas.orellana.gastostracker.presentation.util.toPlainStringTrimmed

class HomeViewModel(
    private val getGastosEntreUseCase: GetGastosEntreUseCase,
    private val addGastoUseCase: AddGastoUseCase,
    private val updateGastoUseCase: UpdateGastoUseCase,
    private val deleteGastoUseCase: DeleteGastoUseCase,
    private val deleteAllGastosUseCase: DeleteAllGastosUseCase,
    private val getCategoriasPersonalizadasUseCase: GetCategoriasPersonalizadasUseCase,
    private val addCategoriaPersonalizadaUseCase: AddCategoriaPersonalizadaUseCase,
    private val updateCategoriaPersonalizadaUseCase: UpdateCategoriaPersonalizadaUseCase,
    private val deleteCategoriaPersonalizadaUseCase: DeleteCategoriaPersonalizadaUseCase,
    private val contarMovimientosCategoriaUseCase: ContarMovimientosCategoriaUseCase,
    private val getTotalesGastosPorCategoriaUseCase: GetTotalesGastosPorCategoriaUseCase,
    private val getEstadoPresupuestosUseCase: GetEstadoPresupuestosUseCase,
    private val guardarRecurrenteUseCase: GuardarRecurrenteUseCase,
    private val generarRecurrentesUseCase: GenerarRecurrentesUseCase,
    private val ajustesRepository: AjustesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val mesSeleccionado = MutableStateFlow(_state.value.mes)

    /** Rango de fechas del filtro; si hay uno, sustituye al mes en la lista. */
    private val rangoPropio = MutableStateFlow<ClosedRange<LocalDate>?>(null)

    /** Día en que empieza el mes contable (Ajustes). */
    private val diaInicio = ajustesRepository.diaInicioMes(viewModelScope)

    private val _categoriasPersonalizadas = MutableStateFlow<List<CategoriaPersonalizada>>(emptyList())
    val categoriasPersonalizadas: StateFlow<List<CategoriaPersonalizada>> = _categoriasPersonalizadas.asStateFlow()

    private val _addGastoState = MutableStateFlow(AddGastoState())
    val addGastoState: StateFlow<AddGastoState> = _addGastoState.asStateFlow()

    private val _manageCategoriasState = MutableStateFlow(ManageCategoriasState())
    val manageCategoriasState: StateFlow<ManageCategoriasState> = _manageCategoriasState.asStateFlow()

    init {
        loadGastos()
        loadCategoriasPersonalizadas()
        observarAlertas()
        observarPresupuestos()
        seguirDiaInicio()
        observarResumenMensual()
    }

    /** Gastos del mes seleccionado (o del rango de fechas del filtro), filtrados en SQL. */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadGastos() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            mesSeleccionado
                .combine(diaInicio) { mes, dia -> mes.periodo(dia) }
                .combine(rangoPropio) { periodo, rango -> rango ?: periodo }
                .flatMapLatest { rango -> getGastosEntreUseCase(rango.start, rango.endInclusive) }
                .catch { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
                .collect { gastos ->
                    _state.update {
                        it.copy(
                            gastos = gastos,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }

    private fun loadCategoriasPersonalizadas() {
        viewModelScope.launch {
            getCategoriasPersonalizadasUseCase()
                .catch { }
                .collect { categorias ->
                    _categoriasPersonalizadas.value = categorias
                }
        }
    }

    /** Compara lo gastado por categoría en el mes seleccionado con el mes anterior, sumando en SQL. */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observarAlertas() {
        viewModelScope.launch {
            mesSeleccionado.combine(diaInicio) { mes, dia -> mes to dia }
                .flatMapLatest { (mes, dia) ->
                    val anterior = mes.anterior()
                    combine(
                        mes.periodo(dia).let { getTotalesGastosPorCategoriaUseCase(it.start, it.endInclusive) },
                        anterior.periodo(dia).let { getTotalesGastosPorCategoriaUseCase(it.start, it.endInclusive) },
                        getCategoriasPersonalizadasUseCase()
                    ) { actual, totalesAnterior, categorias -> calculateAlerts(mes, actual, totalesAnterior, categorias) }
                }
                .catch { }
                .collect { alerts ->
                    _state.update { state ->
                        state.copy(alerts = alerts.filter { it.clave !in state.dismissedAlertKeys })
                    }
                }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observarPresupuestos() {
        viewModelScope.launch {
            mesSeleccionado.combine(diaInicio) { mes, dia -> mes.periodo(dia) }
                .flatMapLatest { periodo -> getEstadoPresupuestosUseCase(periodo.start, periodo.endInclusive) }
                .catch { }
                .collect { estados -> _state.update { it.copy(presupuestos = estados) } }
        }
    }


    /**
     * Al leer (o cambiar) el día de inicio del mes: si se estaba viendo el periodo actual, se pasa al
     * periodo actual con el nuevo día (con el día 25, el 26 de noviembre ya es «diciembre»).
     */
    private fun seguirDiaInicio() {
        viewModelScope.launch {
            var anterior = 1
            diaInicio.collect { dia ->
                _state.update { it.copy(diaInicioMes = dia) }
                if (mesSeleccionado.value == Mes.actual(anterior) && Mes.actual(dia) != mesSeleccionado.value) {
                    mesSeleccionado.value = Mes.actual(dia)
                    _state.update { it.copy(mes = Mes.actual(dia)) }
                }
                anterior = dia
            }
        }
    }


    /**
     * Al empezar un mes nuevo, enseña el resumen del mes anterior (una sola vez: al descartarlo se
     * guarda en Ajustes que ya se vio).
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observarResumenMensual() {
        viewModelScope.launch {
            combine(diaInicio, ajustesRepository.ajustes.map { it.ultimoResumenVisto }.distinctUntilChanged()) { dia, visto -> dia to visto }
                .flatMapLatest { (dia, visto) ->
                    val mes = Mes.actual(dia).anterior()
                    if (visto == mes.toString()) {
                        flowOf(null)
                    } else {
                        val periodo = mes.periodo(dia)
                        val anterior = mes.anterior().periodo(dia)
                        combine(
                            getTotalesGastosPorCategoriaUseCase(periodo.start, periodo.endInclusive),
                            getTotalesGastosPorCategoriaUseCase(anterior.start, anterior.endInclusive)
                        ) { totales, totalesAnterior -> calcularResumenMensual(mes, totales, totalesAnterior) }
                    }
                }
                .catch { }
                .collect { resumen -> _state.update { it.copy(resumenMensual = resumen) } }
        }
    }

    fun descartarResumenMensual() {
        val resumen = _state.value.resumenMensual ?: return
        _state.update { it.copy(resumenMensual = null) }
        viewModelScope.launch { ajustesRepository.setUltimoResumenVisto(resumen.mes.toString()) }
    }

    fun showAddGastoDialog() {
        _addGastoState.value = AddGastoState()
        _state.update { it.copy(showAddGastoDialog = true) }
    }

    fun showEditGastoDialog(gasto: GastoModel) {
        _addGastoState.value = AddGastoState(
            concepto = gasto.nombre,
            precio = gasto.monto.toPlainStringTrimmed().replace('.', ','),
            categoriaSeleccionada = gasto.categoria,
            categoriaPersonalizadaSeleccionada = gasto.categoriaPersonalizadaId,
            fecha = gasto.fecha,
            gastoToEdit = gasto,
            nota = gasto.nota.orEmpty(),
            metodoPago = gasto.metodoPago,
            cuentaId = gasto.cuentaId,
            fotoTicket = gasto.fotoTicket
        )
        _state.update { it.copy(showAddGastoDialog = true) }
    }

    fun hideAddGastoDialog() {
        _state.update { it.copy(showAddGastoDialog = false) }
    }

    fun showManageCategoriasDialog() {
        _state.update { it.copy(showManageCategoriasDialog = true) }
    }

    fun hideManageCategoriasDialog() {
        _state.update { it.copy(showManageCategoriasDialog = false) }
    }

    fun saveGasto(nombre: String, precio: String, categoria: Categoria?, categoriaPersonalizadaId: String?) {
        val monto = parseImporte(precio) ?: return
        val fecha = _addGastoState.value.fecha
        val gastoToEdit = _addGastoState.value.gastoToEdit
        val nota = _addGastoState.value.nota.trim().ifEmpty { null }
        val metodoPago = _addGastoState.value.metodoPago
        val cuentaId = _addGastoState.value.cuentaId
        val fotoTicket = _addGastoState.value.fotoTicket
        if (gastoToEdit == null && _addGastoState.value.repetirMensual) {
            // Gasto fijo: se guarda el recurrente y la generación crea el gasto de este mes (si ya toca).
            val recurrente = Recurrente(
                id = randomId(),
                tipo = TipoMovimiento.GASTO,
                nombre = nombre,
                monto = monto,
                categoria = categoria,
                categoriaPersonalizadaId = categoriaPersonalizadaId,
                diaDelMes = fecha.dayOfMonth,
                fechaInicio = fecha
            )
            viewModelScope.launch {
                guardarRecurrenteUseCase(recurrente)
                generarRecurrentesUseCase()
                _state.update { it.copy(showAddGastoDialog = false) }
            }
            return
        }
        val gasto = if (gastoToEdit != null) {
            gastoToEdit.copy(
                nombre = nombre,
                monto = monto,
                fecha = fecha,
                categoria = categoria,
                categoriaPersonalizadaId = categoriaPersonalizadaId,
                nota = nota,
                metodoPago = metodoPago,
                cuentaId = cuentaId,
                fotoTicket = fotoTicket
            )
        } else {
            GastoModel(
                id = randomId(),
                nombre = nombre,
                monto = monto,
                fecha = fecha,
                categoria = categoria,
                categoriaPersonalizadaId = categoriaPersonalizadaId,
                nota = nota,
                metodoPago = metodoPago,
                cuentaId = cuentaId,
                fotoTicket = fotoTicket
            )
        }
        viewModelScope.launch {
            if (gastoToEdit != null) {
                updateGastoUseCase(gasto)
            } else {
                addGastoUseCase(gasto)
            }
            _state.update { it.copy(showAddGastoDialog = false) }
        }
    }

    fun deleteGasto(id: String) {
        viewModelScope.launch {
            deleteGastoUseCase(id)
            _state.update { state -> state.copy(gastos = state.gastos.filter { it.id != id }) }
        }
    }

    fun restoreGasto(gasto: GastoModel) {
        viewModelScope.launch {
            addGastoUseCase(gasto)
        }
    }

    fun deleteAllGastos() {
        viewModelScope.launch {
            deleteAllGastosUseCase()
        }
    }

    fun addCategoriaPersonalizada(nombre: String, color: Long) {
        viewModelScope.launch {
            addCategoriaPersonalizadaUseCase(nombre, color)
        }
    }

    /**
     * Si la categoría no tiene movimientos se borra directamente; si tiene, se pregunta antes
     * a qué categoría pasarlos (ver [confirmarEliminarCategoria]).
     */
    fun solicitarEliminarCategoria(categoria: CategoriaPersonalizada) {
        viewModelScope.launch {
            val movimientos = contarMovimientosCategoriaUseCase(categoria.id)
            if (movimientos == 0) {
                eliminarCategoria(categoria.id, CategoriaDestino.POR_DEFECTO)
            } else {
                _manageCategoriasState.update {
                    it.copy(
                        categoriaAEliminar = categoria,
                        movimientosCategoriaAEliminar = movimientos,
                        destinoReasignacion = CategoriaDestino.POR_DEFECTO
                    )
                }
            }
        }
    }

    fun updateDestinoReasignacion(destino: CategoriaDestino) {
        _manageCategoriasState.update { it.copy(destinoReasignacion = destino) }
    }

    fun confirmarEliminarCategoria() {
        val state = _manageCategoriasState.value
        val categoria = state.categoriaAEliminar ?: return
        cancelarEliminarCategoria()
        viewModelScope.launch { eliminarCategoria(categoria.id, state.destinoReasignacion) }
    }

    fun cancelarEliminarCategoria() {
        _manageCategoriasState.update {
            it.copy(
                categoriaAEliminar = null,
                movimientosCategoriaAEliminar = 0,
                destinoReasignacion = CategoriaDestino.POR_DEFECTO
            )
        }
    }

    private suspend fun eliminarCategoria(id: String, destino: CategoriaDestino) {
        deleteCategoriaPersonalizadaUseCase(id, destino)
        _categoriasPersonalizadas.update { categorias -> categorias.filter { it.id != id } }
        // Si se estaba filtrando o editando esa categoría, ya no tiene sentido.
        _state.update { it.copy(filtro = it.filtro.copy(personalizadas = it.filtro.personalizadas - id)) }
        if (_manageCategoriasState.value.editCategoria?.id == id) cancelEditCategoria()
    }

    fun showEditCategoriaDialog(categoria: CategoriaPersonalizada) {
        _manageCategoriasState.update {
            it.copy(
                editCategoria = categoria,
                editNombre = categoria.nombre,
                editColor = categoria.color
            )
        }
    }

    fun updateEditNombre(nombre: String) {
        _manageCategoriasState.update { it.copy(editNombre = nombre) }
    }

    fun updateEditColor(color: Long) {
        _manageCategoriasState.update { it.copy(editColor = color) }
    }

    fun saveEditCategoria() {
        val state = _manageCategoriasState.value
        val categoria = state.editCategoria ?: return
        val updated = categoria.copy(
            nombre = state.editNombre,
            color = state.editColor
        )
        viewModelScope.launch {
            updateCategoriaPersonalizadaUseCase(updated)
            _categoriasPersonalizadas.value = _categoriasPersonalizadas.value.map {
                if (it.id == updated.id) updated else it
            }
            _manageCategoriasState.update {
                it.copy(editCategoria = null, editNombre = "", editColor = Categoria.PALETA_COLORES_PERSONALIZADOS.first())
            }
        }
    }

    fun cancelEditCategoria() {
        _manageCategoriasState.update {
            it.copy(editCategoria = null, editNombre = "", editColor = Categoria.PALETA_COLORES_PERSONALIZADOS.first())
        }
    }

    fun updateAddGastoConcepto(concepto: String) {
        _addGastoState.update { it.copy(concepto = concepto) }
    }

    fun updateAddGastoPrecio(precio: String) {
        _addGastoState.update { it.copy(precio = precio) }
    }

    fun updateAddGastoCategoria(categoria: Categoria?) {
        _addGastoState.update { it.copy(categoriaSeleccionada = categoria, categoriaPersonalizadaSeleccionada = null) }
    }

    fun updateAddGastoCategoriaPersonalizada(id: String?) {
        _addGastoState.update { current: AddGastoState ->
            current.copy(categoriaPersonalizadaSeleccionada = id, categoriaSeleccionada = null)
        }
    }

    fun updateAddGastoNota(nota: String) {
        _addGastoState.update { it.copy(nota = nota) }
    }

    fun updateAddGastoMetodoPago(metodo: MetodoPago?) {
        _addGastoState.update { it.copy(metodoPago = metodo) }
    }

    fun updateAddGastoFoto(ruta: String?) {
        _addGastoState.update { it.copy(fotoTicket = ruta) }
    }

    fun updateAddGastoCuenta(cuentaId: String?) {
        _addGastoState.update { it.copy(cuentaId = cuentaId) }
    }

    fun updateAddGastoRepetir(repetir: Boolean) {
        _addGastoState.update { it.copy(repetirMensual = repetir) }
    }

    fun updateAddGastoFecha(fecha: LocalDate) {
        _addGastoState.update { it.copy(fecha = fecha) }
    }

    fun resetAddGastoState() {
        _addGastoState.value = AddGastoState()
    }

    fun updateNuevaCategoria(nombre: String) {
        _manageCategoriasState.update { current: ManageCategoriasState ->
            current.copy(nuevaCategoria = nombre)
        }
    }

    fun updateColorSeleccionado(color: Long) {
        _manageCategoriasState.update { current: ManageCategoriasState ->
            current.copy(colorSeleccionado = color)
        }
    }

    fun resetManageCategoriasState() {
        _manageCategoriasState.value = ManageCategoriasState()
    }

    fun updateSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    fun alternarFiltroCategoria(categoria: Categoria) = aplicarFiltro(_state.value.filtro.alternarCategoria(categoria))

    fun alternarFiltroPersonalizada(id: String) = aplicarFiltro(_state.value.filtro.alternarPersonalizada(id))

    fun showFiltrosDialog() = _state.update { it.copy(showFiltrosDialog = true) }

    fun hideFiltrosDialog() = _state.update { it.copy(showFiltrosDialog = false) }

    /** Sustituye todos los filtros. Si trae rango de fechas, la lista pasa a ser de ese rango en vez del mes. */
    fun aplicarFiltro(filtro: FiltroGastos) {
        _state.update { it.copy(filtro = filtro, showFiltrosDialog = false) }
        rangoPropio.value = filtro.rangoFechas
    }

    /** Aplica una petición de otra pantalla (p. ej. pulsar una categoría en el gráfico). */
    fun aplicarPeticion(peticion: PeticionFiltro) {
        peticion.mes?.let { seleccionarMes(it) }
        aplicarFiltro(peticion.filtro)
    }

    fun clearFilters() {
        aplicarFiltro(FiltroGastos())
        _state.update { it.copy(searchQuery = "") }
    }

    fun updateSortOrder(order: SortOrder) {
        _state.update { it.copy(sortOrder = order) }
    }

    fun dismissAlert(clave: String) {
        _state.update {
            it.copy(
                dismissedAlertKeys = it.dismissedAlertKeys + clave,
                alerts = it.alerts.filter { a -> a.clave != clave }
            )
        }
    }

    fun showMonthPicker() {
        _state.update { it.copy(showMonthPicker = true) }
    }

    fun hideMonthPicker() {
        _state.update { it.copy(showMonthPicker = false) }
    }

    fun seleccionarMes(mes: Mes) {
        mesSeleccionado.value = mes
        // Elegir un mes quita el rango de fechas del filtro (si lo había).
        rangoPropio.value = null
        _state.update { it.copy(mes = mes, showMonthPicker = false, filtro = it.filtro.copy(desde = null, hasta = null)) }
    }

    fun mesAnterior() = seleccionarMes(mesSeleccionado.value.anterior())

    fun mesSiguiente() = seleccionarMes(mesSeleccionado.value.siguiente())

    private fun calculateAlerts(
        mes: Mes,
        totalesMesActual: List<TotalPorCategoria>,
        totalesMesAnterior: List<TotalPorCategoria>,
        categoriasPersonalizadas: List<CategoriaPersonalizada>
    ): List<CategoryAlert> {
        val anteriorPorCategoria = totalesMesAnterior
            .associate { (it.categoria to it.categoriaPersonalizadaId) to it.total }

        return totalesMesActual
            .mapNotNull { actual ->
                val totalAnterior = anteriorPorCategoria[actual.categoria to actual.categoriaPersonalizadaId] ?: 0.0
                if (totalAnterior <= 0 || actual.total <= totalAnterior) return@mapNotNull null

                CategoryAlert(
                    // Con el mes en la clave, descartar una alerta no oculta la de otros meses.
                    clave = "$mes:" + (actual.categoria?.name ?: actual.categoriaPersonalizadaId ?: CLAVE_SIN_CATEGORIA),
                    categoria = actual.categoria,
                    categoriaPersonalizada = actual.categoriaPersonalizadaId?.let { id ->
                        categoriasPersonalizadas.find { it.id == id }
                    },
                    gastoActual = actual.total,
                    gastoAnterior = totalAnterior
                )
            }
    }

    private companion object {
        const val CLAVE_SIN_CATEGORIA = "sin_categoria"
    }
}
