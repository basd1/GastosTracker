package bas.orellana.gastostracker.presentation.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.navigation.NavController
import bas.orellana.gastostracker.domain.model.Categoria
import bas.orellana.gastostracker.domain.model.CategoriaPersonalizada
import bas.orellana.gastostracker.domain.model.IngresoModel
import bas.orellana.gastostracker.presentation.ui.components.BottomNavBar
import bas.orellana.gastostracker.presentation.ui.components.GradientTopAppBar
import bas.orellana.gastostracker.presentation.ui.dialogs.AddIngresoDialog
import bas.orellana.gastostracker.presentation.ui.dialogs.ManageCategoriasDialog
import bas.orellana.gastostracker.presentation.ui.dialogs.MonthYearPickerDialog
import bas.orellana.gastostracker.presentation.ui.dialogs.SettingsDialog
import bas.orellana.gastostracker.presentation.viewmodel.GreenViewModel
import bas.orellana.gastostracker.presentation.viewmodel.HomeViewModel
import bas.orellana.gastostracker.presentation.viewmodel.SettingsViewModel
import org.koin.androidx.compose.koinViewModel
import java.time.format.DateTimeFormatter

@Composable
fun GreenScreen(
    navController: NavController,
    viewModel: GreenViewModel = koinViewModel(),
    homeViewModel: HomeViewModel = koinViewModel(),
    settingsViewModel: SettingsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val addIngresoState by viewModel.addIngresoState.collectAsState()
    val isDarkTheme by settingsViewModel.isDarkTheme.collectAsState()
    val categoriasPersonalizadas by homeViewModel.categoriasPersonalizadas.collectAsState()
    val manageCategoriasState by homeViewModel.manageCategoriasState.collectAsState()
    val homeState by homeViewModel.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDarkTheme) Color(0xFF121212) else Color.White)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                GradientTopAppBar(
                    icon = null,
                    emoji = "\uD83D\uDCB0",
                    title = "Balance",
                    onSettingsClick = { viewModel.showSettingsDialog() }
                )
            },
            bottomBar = {
                BottomNavBar(navController = navController)
            },
            floatingActionButton = {
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
                    exit = fadeOut(animationSpec = tween(300))
                ) {
                    ExtendedFloatingActionButton(
                        onClick = { viewModel.showAddIngresoDialog() },
                        containerColor = Color(0xFF4CAF50)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Añadir ingreso",
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ingreso",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        ) { padding ->
            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp)
                ) {
                    item {
                        BalanceHeader(
                            totalIngresos = state.totalIngresosMes,
                            totalGastos = state.totalGastosMes,
                            totalAhorro = state.totalAhorroMes,
                            selectedMonth = state.selectedMonth,
                            selectedYear = state.selectedYear,
                            isDarkTheme = isDarkTheme,
                            showMonthPicker = { viewModel.showMonthPicker() }
                        )
                    }

                    item {
                        Text(
                            text = "Ingresos (${state.ingresos.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp),
                            color = if (isDarkTheme) Color.White else Color.Black
                        )
                    }

                    items(state.ingresos, key = { it.id }) { ingreso ->
                        IngresoItem(
                            ingreso = ingreso,
                            categoriasPersonalizadas = categoriasPersonalizadas,
                            isDarkTheme = isDarkTheme,
                            onDelete = { viewModel.deleteIngreso(it) },
                            onEdit = { viewModel.showEditIngresoDialog(it) }
                        )
                    }
                }
            }
        }

        if (state.showMonthPicker) {
            MonthYearPickerDialog(
                currentMonth = state.selectedMonth,
                currentYear = state.selectedYear,
                onConfirm = { month, year ->
                    viewModel.updateSelectedMonth(month, year)
                },
                onDismiss = { viewModel.hideMonthPicker() }
            )
        }

        if (state.showAddIngresoDialog) {
            AddIngresoDialog(
                concepto = addIngresoState.concepto,
                monto = addIngresoState.monto,
                categoriaSeleccionada = addIngresoState.categoriaSeleccionada,
                categoriaPersonalizadaId = addIngresoState.categoriaPersonalizadaId,
                categoriasPersonalizadas = categoriasPersonalizadas,
                ingresoToEdit = addIngresoState.ingresoToEdit,
                onConceptoChange = { viewModel.updateConcepto(it) },
                onMontoChange = { viewModel.updateMonto(it) },
                onCategoriaChange = { viewModel.updateCategoria(it) },
                onCategoriaPersonalizadaChange = { viewModel.updateCategoriaPersonalizada(it) },
                onSave = {
                    viewModel.saveIngreso(
                        nombre = addIngresoState.concepto,
                        monto = addIngresoState.monto
                    )
                    viewModel.resetAddIngresoState()
                },
                onDismiss = { viewModel.hideAddIngresoDialog() }
            )
        }

        if (state.showSettingsDialog) {
            SettingsDialog(
                isDarkTheme = isDarkTheme,
                onToggleDarkTheme = { settingsViewModel.toggleDarkTheme() },
                onManageCategorias = { homeViewModel.showManageCategoriasDialog() },
                onDismiss = { viewModel.hideSettingsDialog() }
            )
        }

        if (homeState.showManageCategoriasDialog) {
            ManageCategoriasDialog(
                categoriasPersonalizadas = categoriasPersonalizadas,
                colorSeleccionado = manageCategoriasState.colorSeleccionado,
                nuevaCategoria = manageCategoriasState.nuevaCategoria,
                editCategoria = manageCategoriasState.editCategoria,
                editNombre = manageCategoriasState.editNombre,
                editColor = manageCategoriasState.editColor,
                onColorSeleccionado = { homeViewModel.updateColorSeleccionado(it) },
                onNuevaCategoriaChange = { homeViewModel.updateNuevaCategoria(it) },
                onAddCategoria = {
                    homeViewModel.addCategoriaPersonalizada(
                        manageCategoriasState.nuevaCategoria,
                        manageCategoriasState.colorSeleccionado
                    )
                    homeViewModel.resetManageCategoriasState()
                },
                onDeleteCategoria = { homeViewModel.deleteCategoriaPersonalizada(it) },
                onEditCategoria = { homeViewModel.showEditCategoriaDialog(it) },
                onEditNombreChange = { homeViewModel.updateEditNombre(it) },
                onEditColorChange = { homeViewModel.updateEditColor(it) },
                onSaveEdit = { homeViewModel.saveEditCategoria() },
                onCancelEdit = { homeViewModel.cancelEditCategoria() },
                onDismiss = { homeViewModel.hideManageCategoriasDialog() }
            )
        }
    }
}

@Composable
private fun BalanceHeader(
    totalIngresos: Double,
    totalGastos: Double,
    totalAhorro: Double,
    selectedMonth: Int,
    selectedYear: Int,
    isDarkTheme: Boolean,
    showMonthPicker: () -> Unit
) {

    val textColor = if (isDarkTheme) Color.White else Color.Black
    val cardBg = if (isDarkTheme) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.06f)

    Card(
        colors = CardDefaults.cardColors(containerColor = cardBg),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp, top = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showMonthPicker() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                val monthNames = listOf(
                    "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                    "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
                )
                Text(
                    text = "${monthNames[selectedMonth - 1]} $selectedYear",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Seleccionar mes",
                    tint = textColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "\u20AC${String.format("%.2f", totalIngresos)}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                    Text(
                        text = "Ingresos",
                        fontSize = 13.sp,
                        color = textColor.copy(alpha = 0.7f)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "\u20AC${String.format("%.2f", totalGastos)}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE53935)
                    )
                    Text(
                        text = "Gastos",
                        fontSize = 13.sp,
                        color = textColor.copy(alpha = 0.7f)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "\u20AC${String.format("%.2f", totalAhorro)}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF9800)
                    )
                    Text(
                        text = "Ahorro",
                        fontSize = 13.sp,
                        color = textColor.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun IngresoItem(
    ingreso: IngresoModel,
    categoriasPersonalizadas: List<CategoriaPersonalizada>,
    isDarkTheme: Boolean,
    onDelete: (String) -> Unit,
    onEdit: (IngresoModel) -> Unit
) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val greenColor = Color(0xFF4CAF50)
    val categoria = ingreso.categoria

    val categoryColor = when {
        ingreso.categoriaPersonalizadaId != null -> {
            categoriasPersonalizadas.find { it.id == ingreso.categoriaPersonalizadaId }?.let {
                Color(it.color)
            } ?: greenColor
        }
        categoria != null -> Color(categoria.color)
        else -> greenColor
    }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                onDelete(ingreso.id)
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
                    onLongClick = { onEdit(ingreso) }
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
                        .background(categoryColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = categoryColor
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = ingreso.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (isDarkTheme) Color.White else Color.Black
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = ingreso.fecha.format(dateFormatter),
                            fontSize = 12.sp,
                            color = if (isDarkTheme) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.5f)
                        )
                        if (categoria != null || ingreso.categoriaPersonalizadaId != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(categoryColor, RoundedCornerShape(4.dp))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = categoria?.displayName
                                    ?: categoriasPersonalizadas.find { it.id == ingreso.categoriaPersonalizadaId }?.nombre
                                    ?: "",
                                fontSize = 11.sp,
                                color = categoryColor
                            )
                        }
                    }
                }
                Text(
                    text = "\u20AC${String.format("%.2f", ingreso.monto)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = greenColor
                )
            }
        }
    }
}
