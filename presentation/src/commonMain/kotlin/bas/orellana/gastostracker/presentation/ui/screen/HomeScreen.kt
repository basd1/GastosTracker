package bas.orellana.gastostracker.presentation.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import bas.orellana.gastostracker.domain.model.Categoria
import bas.orellana.gastostracker.domain.model.CategoriaPersonalizada
import bas.orellana.gastostracker.domain.model.GastoModel
import bas.orellana.gastostracker.domain.model.Mes
import bas.orellana.gastostracker.domain.model.EstadoPresupuesto
import bas.orellana.gastostracker.presentation.ui.components.FilaPresupuesto
import bas.orellana.gastostracker.presentation.resources.presupuestos_del_mes
import bas.orellana.gastostracker.presentation.resources.ver_todos
import bas.orellana.gastostracker.presentation.ui.components.SelectorMes
import bas.orellana.gastostracker.presentation.ui.dialogs.MonthYearPickerDialog
import bas.orellana.gastostracker.presentation.resources.sin_gastos_mes
import bas.orellana.gastostracker.presentation.state.CategoryAlert
import bas.orellana.gastostracker.presentation.state.SortOrder
import bas.orellana.gastostracker.presentation.ui.components.BottomNavBar
import bas.orellana.gastostracker.presentation.navigation.NavRoutes
import bas.orellana.gastostracker.presentation.ui.components.GradientTopAppBar
import bas.orellana.gastostracker.presentation.ui.dialogs.AddGastoDialog
import bas.orellana.gastostracker.presentation.viewmodel.HomeViewModel
import bas.orellana.gastostracker.presentation.util.formatDDMMYYYY
import org.koin.compose.viewmodel.koinViewModel
import bas.orellana.gastostracker.presentation.resources.Res
import bas.orellana.gastostracker.presentation.resources.accion_descartar
import bas.orellana.gastostracker.presentation.resources.accion_deshacer
import bas.orellana.gastostracker.presentation.resources.accion_eliminar
import bas.orellana.gastostracker.presentation.resources.alerta_comparativa
import bas.orellana.gastostracker.presentation.resources.alerta_mas_gasto
import bas.orellana.gastostracker.presentation.resources.anadir_gasto
import bas.orellana.gastostracker.presentation.resources.buscar
import bas.orellana.gastostracker.presentation.resources.buscar_gastos
import bas.orellana.gastostracker.presentation.resources.gasto_eliminado
import bas.orellana.gastostracker.presentation.resources.gastos_del_mes
import bas.orellana.gastostracker.presentation.resources.limpiar_filtros
import bas.orellana.gastostracker.presentation.resources.media_importe
import bas.orellana.gastostracker.presentation.resources.numero_gastos
import bas.orellana.gastostracker.presentation.resources.sin_categoria
import bas.orellana.gastostracker.presentation.resources.sin_gastos_mensaje
import bas.orellana.gastostracker.presentation.resources.sin_gastos_titulo
import bas.orellana.gastostracker.presentation.resources.sin_resultados
import bas.orellana.gastostracker.presentation.resources.titulo_gastos
import bas.orellana.gastostracker.presentation.ui.components.etiquetaCategoria
import bas.orellana.gastostracker.presentation.ui.components.nombre
import bas.orellana.gastostracker.presentation.util.formatMoney
import bas.orellana.gastostracker.presentation.util.simboloMoneda
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val addGastoState by viewModel.addGastoState.collectAsState()
    val categoriasPersonalizadas by viewModel.categoriasPersonalizadas.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var stashedGasto by remember { mutableStateOf<GastoModel?>(null) }
    val textoGastoEliminado = stringResource(Res.string.gasto_eliminado)
    val textoDeshacer = stringResource(Res.string.accion_deshacer)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            GradientTopAppBar(
                title = stringResource(Res.string.titulo_gastos),
                onSettingsClick = { navController.navigate(NavRoutes.AJUSTES) }
            )
        },
        bottomBar = {
            BottomNavBar(navController = navController)
        },
        floatingActionButton = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 8.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.showAddGastoDialog() },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(Res.string.anadir_gasto), fontSize = 18.sp)
                }
            }
        }
    ) { paddingValues ->
        GastosList(
            gastos = state.gastos,
            gastosVisibles = state.gastosVisibles,
            isLoading = state.isLoading,
            categoriasPersonalizadas = categoriasPersonalizadas,
            alerts = state.alerts,
            searchQuery = state.searchQuery,
            filterCategoria = state.filterCategoria,
            filterCategoriaPersonalizadaId = state.filterCategoriaPersonalizadaId,
            hayBusquedaOFiltro = state.hayBusquedaOFiltro,
            sortOrder = state.sortOrder,
            onSearchQueryChange = { viewModel.updateSearchQuery(it) },
            onFilterCategoria = { viewModel.updateFilterCategoria(it) },
            onFilterCategoriaPersonalizada = { viewModel.updateFilterCategoriaPersonalizada(it) },
            onClearFilters = { viewModel.clearFilters() },
            onSortOrderChange = { viewModel.updateSortOrder(it) },
            onDelete = { gasto ->
                stashedGasto = gasto
                viewModel.deleteGasto(gasto.id)
                scope.launch {
                    val result = snackbarHostState.showSnackbar(
                        message = textoGastoEliminado,
                        actionLabel = textoDeshacer
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        stashedGasto?.let { viewModel.restoreGasto(it) }
                    }
                    stashedGasto = null
                }
            },
            onEdit = { viewModel.showEditGastoDialog(it) },
            onDismissAlert = { viewModel.dismissAlert(it) },
            mes = state.mes,
            onMesAnterior = { viewModel.mesAnterior() },
            onMesSiguiente = { viewModel.mesSiguiente() },
            onElegirMes = { viewModel.showMonthPicker() },
            presupuestos = state.presupuestos,
            onVerPresupuestos = { navController.navigate(NavRoutes.PRESUPUESTOS) },
            modifier = Modifier.padding(paddingValues)
        )

        if (state.showAddGastoDialog) {
            AddGastoDialog(
                concepto = addGastoState.concepto,
                precio = addGastoState.precio,
                categoriaSeleccionada = addGastoState.categoriaSeleccionada,
                categoriaPersonalizadaSeleccionada = addGastoState.categoriaPersonalizadaSeleccionada,
                categoriasPersonalizadas = categoriasPersonalizadas,
                gastoToEdit = addGastoState.gastoToEdit,
                fecha = addGastoState.fecha,
                onConceptoChange = { viewModel.updateAddGastoConcepto(it) },
                onPrecioChange = { viewModel.updateAddGastoPrecio(it) },
                onCategoriaChange = { viewModel.updateAddGastoCategoria(it) },
                onCategoriaPersonalizadaChange = { viewModel.updateAddGastoCategoriaPersonalizada(it) },
                onFechaChange = { viewModel.updateAddGastoFecha(it) },
                onSave = {
                    viewModel.saveGasto(
                        nombre = addGastoState.concepto,
                        precio = addGastoState.precio,
                        categoria = addGastoState.categoriaSeleccionada,
                        categoriaPersonalizadaId = addGastoState.categoriaPersonalizadaSeleccionada
                    )
                    viewModel.resetAddGastoState()
                },
                onDismiss = { viewModel.hideAddGastoDialog() },
                repetirMensual = addGastoState.repetirMensual,
                onRepetirMensualChange = { viewModel.updateAddGastoRepetir(it) }
            )
        }

        if (state.showMonthPicker) {
            MonthYearPickerDialog(
                currentMonth = state.mes.numero,
                currentYear = state.mes.anio,
                onConfirm = { month, year -> viewModel.seleccionarMes(Mes(year, month)) },
                onDismiss = { viewModel.hideMonthPicker() }
            )
        }
    }
    }
}

@Composable
private fun GastosList(
    gastos: List<GastoModel>,
    gastosVisibles: List<GastoModel>,
    isLoading: Boolean,
    categoriasPersonalizadas: List<CategoriaPersonalizada>,
    alerts: List<CategoryAlert> = emptyList(),
    searchQuery: String = "",
    filterCategoria: Categoria? = null,
    filterCategoriaPersonalizadaId: String? = null,
    hayBusquedaOFiltro: Boolean = false,
    sortOrder: SortOrder = SortOrder.DATE_DESC,
    onSearchQueryChange: (String) -> Unit = {},
    onFilterCategoria: (Categoria?) -> Unit = {},
    onFilterCategoriaPersonalizada: (String?) -> Unit = {},
    onClearFilters: () -> Unit = {},
    onSortOrderChange: (SortOrder) -> Unit = {},
    onDelete: (GastoModel) -> Unit,
    onEdit: (GastoModel) -> Unit,
    onDismissAlert: (String) -> Unit = {},
    mes: Mes = Mes.actual(),
    onMesAnterior: () -> Unit = {},
    onMesSiguiente: () -> Unit = {},
    onElegirMes: () -> Unit = {},
    presupuestos: List<EstadoPresupuesto> = emptyList(),
    onVerPresupuestos: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (isLoading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    } else {
        // La lista ya viene filtrada por mes desde SQL.
        val gastosDelMes = gastos
        val totalMes = gastosDelMes.sumOf { it.monto }

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                SummaryHeader(
                    mes = mes,
                    totalMes = totalMes,
                    cantidad = gastosDelMes.size,
                    onMesAnterior = onMesAnterior,
                    onMesSiguiente = onMesSiguiente,
                    onElegirMes = onElegirMes
                )
            }
            if (presupuestos.isNotEmpty()) {
                item {
                    PresupuestosResumen(
                        presupuestos = presupuestos,
                        categoriasPersonalizadas = categoriasPersonalizadas,
                        onVerTodos = onVerPresupuestos
                    )
                }
            }
            if (alerts.isNotEmpty()) {
                item {
                    AlertsSection(
                        alerts = alerts,
                        onDismissAlert = onDismissAlert
                    )
                }
            }
            if (gastos.isEmpty()) {
                item { SinGastos(esMesActual = mes == Mes.actual()) }
            } else {
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SortOrder.entries.take(4).forEach { order ->
                        FilterChip(
                            selected = sortOrder == order,
                            onClick = { onSortOrderChange(order) },
                            label = { Text(stringResource(order.etiqueta), fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text(stringResource(Res.string.buscar_gastos)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = stringResource(Res.string.buscar),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                )
            }
            item {
                val hasActiveFilter = filterCategoria != null || filterCategoriaPersonalizadaId != null
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Categoria.entries.take(6).forEach { cat ->
                            FilterChip(
                                selected = filterCategoria == cat,
                                onClick = {
                                    if (filterCategoria == cat) onFilterCategoria(null)
                                    else onFilterCategoria(cat)
                                },
                                label = { Text(cat.nombre(), fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(cat.color).copy(alpha = 0.4f),
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }
                    }
                    if (categoriasPersonalizadas.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            categoriasPersonalizadas.take(4).forEach { cat ->
                                FilterChip(
                                    selected = filterCategoriaPersonalizadaId == cat.id,
                                    onClick = {
                                        if (filterCategoriaPersonalizadaId == cat.id) onFilterCategoriaPersonalizada(null)
                                        else onFilterCategoriaPersonalizada(cat.id)
                                    },
                                    label = { Text(cat.nombre, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(cat.color).copy(alpha = 0.4f),
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                )
                            }
                        }
                    }
                    if (hasActiveFilter) {
                        Spacer(modifier = Modifier.height(4.dp))
                        TextButton(
                            onClick = onClearFilters,
                            modifier = Modifier.padding(0.dp)
                        ) {
                            Text(stringResource(Res.string.limpiar_filtros), fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
            if (gastosVisibles.isEmpty() && hayBusquedaOFiltro) {
                item {
                    Text(
                        text = stringResource(Res.string.sin_resultados),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            }
            items(gastosVisibles, key = { it.id }) { gasto ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(300)) +
                            slideInVertically(
                                animationSpec = tween(300),
                                initialOffsetY = { it / 2 }
                            ),
                    exit = fadeOut(animationSpec = tween(200))
                ) {
                    GastoItem(
                        gasto = gasto,
                        categoriasPersonalizadas = categoriasPersonalizadas,
                        onDelete = onDelete,
                        onEdit = onEdit
                    )
                }
            }
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

/** Los presupuestos más apurados del mes (máximo 3) y un enlace a la pantalla de presupuestos. */
@Composable
private fun PresupuestosResumen(
    presupuestos: List<EstadoPresupuesto>,
    categoriasPersonalizadas: List<CategoriaPersonalizada>,
    onVerTodos: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = MaterialTheme.shapes.small
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 4.dp, bottom = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(Res.string.presupuestos_del_mes),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onVerTodos) {
                    Text(stringResource(Res.string.ver_todos), fontSize = 12.sp)
                }
            }
            Column(
                modifier = Modifier.padding(end = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                presupuestos.take(3).forEach { estado ->
                    FilaPresupuesto(
                        estado = estado,
                        categoriasPersonalizadas = categoriasPersonalizadas,
                        compacta = true
                    )
                }
            }
        }
    }
}

@Composable
private fun SinGastos(esMesActual: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(if (esMesActual) Res.string.sin_gastos_titulo else Res.string.sin_gastos_mes),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (esMesActual) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(Res.string.sin_gastos_mensaje),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AlertsSection(
    alerts: List<CategoryAlert>,
    onDismissAlert: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        alerts.forEach { alert ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                ),
                shape = MaterialTheme.shapes.small
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "\u26A0\uFE0F",
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        val nombreCategoria = alert.categoria?.nombre()
                            ?: alert.categoriaPersonalizada?.nombre
                            ?: stringResource(Res.string.sin_categoria)
                        Text(
                            text = stringResource(Res.string.alerta_mas_gasto, nombreCategoria),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(
                                Res.string.alerta_comparativa,
                                formatMoney(alert.gastoActual),
                                formatMoney(alert.gastoAnterior)
                            ),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                        )
                    }
                    IconButton(onClick = { onDismissAlert(alert.clave) }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(Res.string.accion_descartar),
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryHeader(
    mes: Mes,
    totalMes: Double,
    cantidad: Int,
    onMesAnterior: () -> Unit,
    onMesSiguiente: () -> Unit,
    onElegirMes: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = MaterialTheme.shapes.small
    ) {
        SelectorMes(
            mes = mes,
            onAnterior = onMesAnterior,
            onSiguiente = onMesSiguiente,
            onElegir = onElegirMes,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stringResource(Res.string.gastos_del_mes),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatMoney(totalMes),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = pluralStringResource(Res.plurals.numero_gastos, cantidad, cantidad),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val media = if (cantidad > 0) totalMes / cantidad else 0.0
                Text(
                    text = stringResource(Res.string.media_importe, formatMoney(media)),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun GastoItem(
    gasto: GastoModel,
    categoriasPersonalizadas: List<CategoriaPersonalizada>,
    onDelete: (GastoModel) -> Unit,
    onEdit: (GastoModel) -> Unit
) {
    val etiqueta = etiquetaCategoria(gasto.categoria, gasto.categoriaPersonalizadaId, categoriasPersonalizadas)
    val categoriaColor = etiqueta?.color ?: Color(0xFF607D8B)
    val nombreCategoria = etiqueta?.nombre

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                onDelete(gasto)
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val dismissDirection = dismissState.dismissDirection
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (dismissDirection == SwipeToDismissBoxValue.EndToStart) Color.Transparent
                        else Color.Transparent,
                        MaterialTheme.shapes.extraSmall
                    )
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(Res.string.accion_eliminar),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        },
        enableDismissFromStartToEnd = false
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = {},
                    onLongClick = { onEdit(gasto) }
                ),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(categoriaColor.copy(alpha = 0.2f), MaterialTheme.shapes.extraSmall),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = simboloMoneda(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = categoriaColor
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    if (nombreCategoria != null) {
                        Text(
                            text = nombreCategoria,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(1.dp))
                    }
                    Text(
                        text = gasto.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = gasto.fecha.formatDDMMYYYY(),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
                Text(
                    text = formatMoney(gasto.monto),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = categoriaColor
                )
            }
        }
    }
}
