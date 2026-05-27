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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.navigation.NavController
import bas.orellana.gastostracker.domain.model.Categoria
import bas.orellana.gastostracker.domain.model.CategoriaPersonalizada
import bas.orellana.gastostracker.domain.model.GastoModel
import bas.orellana.gastostracker.presentation.state.CategoryAlert
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDarkTheme) Color(0xFF121212) else Color.White)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
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
                    containerColor = Color(0xFF2E7D32),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Text("Añadir gasto", fontSize = 18.sp)
                }
            }
        }
    ) { paddingValues ->
        GastosList(
            gastos = state.gastos,
            isLoading = state.isLoading,
            categoriasPersonalizadas = categoriasPersonalizadas,
            isDarkTheme = isDarkTheme,
            alerts = state.alerts,
            searchQuery = state.searchQuery,
            onSearchQueryChange = { viewModel.updateSearchQuery(it) },
            onDelete = { viewModel.deleteGasto(it) },
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
    isDarkTheme: Boolean,
    alerts: List<CategoryAlert> = emptyList(),
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    onDelete: (String) -> Unit,
    onEdit: (GastoModel) -> Unit,
    onDismissAlert: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val mutedTextColor = if (isDarkTheme) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.6f)
    val cardBg = if (isDarkTheme) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.06f)

    val filteredGastos = if (searchQuery.isBlank()) {
        gastos
    } else {
        gastos.filter { it.nombre.contains(searchQuery, ignoreCase = true) }
    }

    if (isLoading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
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
                    tint = mutedTextColor
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No hay gastos todavía",
                    style = MaterialTheme.typography.titleMedium,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Pulsa + para añadir tu primer gasto",
                    style = MaterialTheme.typography.bodyMedium,
                    color = mutedTextColor
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
                    cantidad = gastosDelMes.size,
                    textColor = textColor,
                    mutedTextColor = mutedTextColor,
                    cardBg = cardBg
                )
            }
            if (alerts.isNotEmpty()) {
                item {
                    AlertsSection(
                        alerts = alerts,
                        isDarkTheme = isDarkTheme,
                        onDismissAlert = onDismissAlert
                    )
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
                            tint = mutedTextColor
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = mutedTextColor.copy(alpha = 0.3f)
                    )
                )
            }
            if (filteredGastos.isEmpty() && searchQuery.isNotBlank()) {
                item {
                    Text(
                        text = "No se encontraron gastos con ese nombre",
                        style = MaterialTheme.typography.bodyMedium,
                        color = mutedTextColor,
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
                        isDarkTheme = isDarkTheme,
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
    isDarkTheme: Boolean,
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
                    containerColor = Color(0xFFFFF3E0)
                ),
                shape = RoundedCornerShape(10.dp)
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
                            color = Color(0xFFE65100)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Este mes: \u20AC${String.format("%.2f", alert.gastoActual)}  |  Mes pasado: \u20AC${String.format("%.2f", alert.gastoAnterior)}",
                            fontSize = 12.sp,
                            color = Color(0xFFBF360C)
                        )
                    }
                    IconButton(onClick = { onDismissAlert(alert.categoriaNombre) }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Descartar",
                            tint = Color(0xFFBF360C),
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
    cantidad: Int,
    textColor: Color,
    mutedTextColor: Color,
    cardBg: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(12.dp)
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
                    color = mutedTextColor
                )
                Text(
                    text = "\u20AC${String.format("%.2f", totalMes)}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "$cantidad ${if (cantidad == 1) "gasto" else "gastos"}",
                    fontSize = 13.sp,
                    color = mutedTextColor
                )
                val media = if (cantidad > 0) totalMes / cantidad else 0.0
                Text(
                    text = "media \u20AC${String.format("%.2f", media)}",
                    fontSize = 13.sp,
                    color = mutedTextColor
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun GastoItem(
    gasto: GastoModel,
    categoriasPersonalizadas: List<CategoriaPersonalizada>,
    isDarkTheme: Boolean,
    onDelete: (String) -> Unit,
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
                onDelete(gasto.id)
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
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = Color(0xFFE53935),
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
                containerColor = if (isDarkTheme) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.06f)
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
                        .background(categoriaColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
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
                            color = if (isDarkTheme) Color.White.copy(alpha = 0.65f) else Color.Black.copy(alpha = 0.55f)
                        )
                        Spacer(modifier = Modifier.height(1.dp))
                    }
                    Text(
                        text = gasto.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (isDarkTheme) Color.White else Color.Black
                    )
                    Text(
                        text = gasto.fecha.format(dateFormatter),
                        fontSize = 12.sp,
                        color = if (isDarkTheme) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.5f)
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

