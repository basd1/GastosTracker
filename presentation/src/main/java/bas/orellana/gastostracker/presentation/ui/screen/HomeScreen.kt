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
import bas.orellana.gastostracker.presentation.state.CategoryAlert
import bas.orellana.gastostracker.presentation.state.SortOrder
import bas.orellana.gastostracker.presentation.ui.components.BottomNavBar
import bas.orellana.gastostracker.presentation.ui.components.GradientTopAppBar
import bas.orellana.gastostracker.presentation.ui.dialogs.AddGastoDialog
import bas.orellana.gastostracker.presentation.ui.dialogs.ManageCategoriasDialog
import bas.orellana.gastostracker.presentation.ui.dialogs.SettingsDialog
import bas.orellana.gastostracker.presentation.viewmodel.HomeViewModel
import bas.orellana.gastostracker.presentation.viewmodel.SettingsViewModel
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = koinViewModel(),
    settingsViewModel: SettingsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val addGastoState by viewModel.addGastoState.collectAsState()
    val manageCategoriasState by viewModel.manageCategoriasState.collectAsState()
    val isDarkTheme by settingsViewModel.isDarkTheme.collectAsState()
    val categoriasPersonalizadas by viewModel.categoriasPersonalizadas.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var stashedGasto by remember { mutableStateOf<GastoModel?>(null) }

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
                title = "Gastos",
                onSettingsClick = { viewModel.showSettingsDialog() }
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
                    Text("Añadir gasto", fontSize = 18.sp)
                }
            }
        }
    ) { paddingValues ->
        GastosList(
            gastos = state.gastos,
            isLoading = state.isLoading,
            categoriasPersonalizadas = categoriasPersonalizadas,
            alerts = state.alerts,
            searchQuery = state.searchQuery,
            filterCategoria = state.filterCategoria,
            filterCategoriaPersonalizadaId = state.filterCategoriaPersonalizadaId,
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
                        message = "Gasto eliminado",
                        actionLabel = "Deshacer"
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        stashedGasto?.let { viewModel.restoreGasto(it) }
                    }
                    stashedGasto = null
                }
            },
            onEdit = { viewModel.showEditGastoDialog(it) },
            onDismissAlert = { viewModel.dismissAlert(it) },
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
                onDismiss = { viewModel.hideAddGastoDialog() }
            )
        }

        if (state.showSettingsDialog) {
            SettingsDialog(
                isDarkTheme = isDarkTheme,
                onToggleDarkTheme = { settingsViewModel.toggleDarkTheme() },
                onManageCategorias = { viewModel.showManageCategoriasDialog() },
                onDismiss = { viewModel.hideSettingsDialog() }
            )
        }

        if (state.showManageCategoriasDialog) {
            ManageCategoriasDialog(
                categoriasPersonalizadas = categoriasPersonalizadas,
                colorSeleccionado = manageCategoriasState.colorSeleccionado,
                nuevaCategoria = manageCategoriasState.nuevaCategoria,
                editCategoria = manageCategoriasState.editCategoria,
                editNombre = manageCategoriasState.editNombre,
                editColor = manageCategoriasState.editColor,
                onColorSeleccionado = { viewModel.updateColorSeleccionado(it) },
                onNuevaCategoriaChange = { viewModel.updateNuevaCategoria(it) },
                onAddCategoria = {
                    viewModel.addCategoriaPersonalizada(
                        manageCategoriasState.nuevaCategoria,
                        manageCategoriasState.colorSeleccionado
                    )
                    viewModel.resetManageCategoriasState()
                },
                onDeleteCategoria = { viewModel.deleteCategoriaPersonalizada(it) },
                onEditCategoria = { viewModel.showEditCategoriaDialog(it) },
                onEditNombreChange = { viewModel.updateEditNombre(it) },
                onEditColorChange = { viewModel.updateEditColor(it) },
                onSaveEdit = { viewModel.saveEditCategoria() },
                onCancelEdit = { viewModel.cancelEditCategoria() },
                onDismiss = { viewModel.hideManageCategoriasDialog() }
            )
        }
    }
    }
}

@Composable
private fun GastosList(
    gastos: List<GastoModel>,
    isLoading: Boolean,
    categoriasPersonalizadas: List<CategoriaPersonalizada>,
    alerts: List<CategoryAlert> = emptyList(),
    searchQuery: String = "",
    filterCategoria: Categoria? = null,
    filterCategoriaPersonalizadaId: String? = null,
    sortOrder: SortOrder = SortOrder.DATE_DESC,
    onSearchQueryChange: (String) -> Unit = {},
    onFilterCategoria: (Categoria?) -> Unit = {},
    onFilterCategoriaPersonalizada: (String?) -> Unit = {},
    onClearFilters: () -> Unit = {},
    onSortOrderChange: (SortOrder) -> Unit = {},
    onDelete: (GastoModel) -> Unit,
    onEdit: (GastoModel) -> Unit,
    onDismissAlert: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val filteredGastos = gastos.filter { gasto ->
        val matchesSearch = searchQuery.isBlank() || gasto.nombre.contains(searchQuery, ignoreCase = true)
        val matchesCategoria = filterCategoria == null || gasto.categoria == filterCategoria
        val matchesPersonalizada = filterCategoriaPersonalizadaId == null || gasto.categoriaPersonalizadaId == filterCategoriaPersonalizadaId
        matchesSearch && matchesCategoria && matchesPersonalizada
    }.let { sorted ->
        when (sortOrder) {
            SortOrder.DATE_DESC -> sorted.sortedByDescending { it.fecha }
            SortOrder.DATE_ASC -> sorted.sortedBy { it.fecha }
            SortOrder.AMOUNT_DESC -> sorted.sortedByDescending { it.monto }
            SortOrder.AMOUNT_ASC -> sorted.sortedBy { it.monto }
            SortOrder.NAME_ASC -> sorted.sortedBy { it.nombre.lowercase() }
            SortOrder.NAME_DESC -> sorted.sortedByDescending { it.nombre.lowercase() }
        }
    }

    if (isLoading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    } else if (gastos.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
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
                    text = "No hay gastos todavía",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Pulsa + para añadir tu primer gasto",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        val now = LocalDate.now()
        val gastosDelMes = gastos.filter {
            it.fecha.year == now.year && it.fecha.month == now.month && it.categoria != Categoria.AHORRO
        }
        val totalMes = gastosDelMes.sumOf { it.monto }

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                SummaryHeader(
                    totalMes = totalMes,
                    cantidad = gastosDelMes.size
                )
            }
            if (alerts.isNotEmpty()) {
                item {
                    AlertsSection(
                        alerts = alerts,
                        onDismissAlert = onDismissAlert
                    )
                }
            }
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SortOrder.entries.take(4).forEach { order ->
                        FilterChip(
                            selected = sortOrder == order,
                            onClick = { onSortOrderChange(order) },
                            label = { Text(order.displayName, fontSize = 11.sp) },
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
                    placeholder = { Text("Buscar gastos...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar",
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
                                label = { Text(cat.displayName, fontSize = 11.sp) },
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
                            Text("Limpiar filtros", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
            if (filteredGastos.isEmpty() && (searchQuery.isNotBlank() || filterCategoria != null || filterCategoriaPersonalizadaId != null)) {
                item {
                    Text(
                        text = "No se encontraron gastos con esos criterios",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            }
            items(filteredGastos, key = { it.id }) { gasto ->
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
            item { Spacer(modifier = Modifier.height(80.dp)) }
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
                        Text(
                            text = "Has gastado m\u00E1s en ${alert.categoriaNombre} que el mes pasado",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Este mes: \u20AC${String.format("%.2f", alert.gastoActual)}  |  Mes pasado: \u20AC${String.format("%.2f", alert.gastoAnterior)}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                        )
                    }
                    IconButton(onClick = { onDismissAlert(alert.categoriaNombre) }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Descartar",
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
    totalMes: Double,
    cantidad: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Gastos del mes",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "\u20AC${String.format("%.2f", totalMes)}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "$cantidad ${if (cantidad == 1) "gasto" else "gastos"}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val media = if (cantidad > 0) totalMes / cantidad else 0.0
                Text(
                    text = "media \u20AC${String.format("%.2f", media)}",
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
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    val categoriaColor = when {
        gasto.categoria != null -> Color(gasto.categoria!!.color)
        gasto.categoriaPersonalizadaId != null -> categoriasPersonalizadas.find { it.id == gasto.categoriaPersonalizadaId }?.let { Color(it.color) } ?: Color(0xFF607D8B)
        else -> Color(0xFF607D8B)
    }

    val nombreCategoria = when {
        gasto.categoria != null -> gasto.categoria?.displayName
        gasto.categoriaPersonalizadaId != null -> categoriasPersonalizadas.find { it.id == gasto.categoriaPersonalizadaId }?.nombre
        else -> null
    }

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
                        contentDescription = "Eliminar",
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
                        text = "\u20AC",
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
                        text = gasto.fecha.format(dateFormatter),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
                Text(
                    text = "\u20AC${String.format("%.2f", gasto.monto)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = categoriaColor
                )
            }
        }
    }
}
