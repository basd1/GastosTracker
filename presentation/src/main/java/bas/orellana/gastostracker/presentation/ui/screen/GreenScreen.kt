package bas.orellana.gastostracker.presentation.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 8.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    ExtendedFloatingActionButton(
                        onClick = { viewModel.showAddIngresoDialog() },
                        containerColor = Color(0xFF2E7D32),
                        contentColor = Color.White,
                        shape = RoundedCornerShape(28.dp),
                        modifier = Modifier.padding(horizontal = 24.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                        Text("Añadir ingreso", fontSize = 18.sp)
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                val filteredIngresos = state.ingresos.filter {
                    it.fecha.monthValue == state.selectedMonth && it.fecha.year == state.selectedYear
                }

                BalanceHeader(
                    totalIngresos = state.totalIngresosMes,
                    totalGastos = state.totalGastosMes,
                    totalAhorro = state.totalAhorroMes,
                    selectedMonth = state.selectedMonth,
                    selectedYear = state.selectedYear,
                    isDarkTheme = isDarkTheme,
                    onMonthClick = { viewModel.showMonthPicker() }
                )

                IngresosList(
                    ingresos = filteredIngresos,
                    isLoading = state.isLoading,
                    isDarkTheme = isDarkTheme,
                    onDelete = { viewModel.deleteIngreso(it) },
                    onEdit = { viewModel.showEditIngresoDialog(it) },
                    modifier = Modifier.weight(1f)
                )
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
                    ingresoToEdit = addIngresoState.ingresoToEdit,
                    onConceptoChange = { viewModel.updateConcepto(it) },
                    onMontoChange = { viewModel.updateMonto(it) },
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
                    onDismiss = { homeViewModel.hideManageCategoriasDialog() }
                )
            }
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
    onMonthClick: () -> Unit = {}
) {
    val balance = totalIngresos - totalGastos
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val mutedTextColor = if (isDarkTheme) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.6f)
    val cardBg = if (isDarkTheme) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.06f)
    val monthNames = listOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )
    val monthLabel = "${monthNames[selectedMonth - 1]} $selectedYear"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier.clickable(onClick = onMonthClick),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkTheme) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.05f)
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = monthLabel,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Cambiar mes",
                        tint = mutedTextColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BalanceCard(
                title = "Ingresos",
                amount = totalIngresos,
                color = Color(0xFF2E7D32),
                textColor = textColor,
                mutedTextColor = mutedTextColor,
                cardBg = cardBg,
                modifier = Modifier.weight(1f)
            )
            BalanceCard(
                title = "Gastos",
                amount = totalGastos,
                color = Color(0xFFC62828),
                textColor = textColor,
                mutedTextColor = mutedTextColor,
                cardBg = cardBg,
                modifier = Modifier.weight(1f)
            )
            BalanceCard(
                title = "Ahorro",
                amount = totalAhorro,
                color = Color(0xFFFFD700),
                textColor = textColor,
                mutedTextColor = mutedTextColor,
                cardBg = cardBg,
                modifier = Modifier.weight(1f)
            )
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (balance >= 0) Color(0xFF2E7D32).copy(alpha = 0.15f) else Color(0xFFC62828).copy(alpha = 0.15f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Balance neto",
                    fontSize = 16.sp,
                    color = mutedTextColor
                )
                Text(
                    text = "\u20AC${String.format("%.2f", balance)}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (balance >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
                )
            }
        }
    }
}

@Composable
private fun BalanceCard(
    title: String,
    amount: Double,
    color: Color,
    textColor: Color,
    mutedTextColor: Color,
    cardBg: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = title,
                fontSize = 13.sp,
                color = mutedTextColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "\u20AC${String.format("%.2f", amount)}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun IngresosList(
    ingresos: List<IngresoModel>,
    isLoading: Boolean,
    isDarkTheme: Boolean,
    onDelete: (String) -> Unit,
    onEdit: (IngresoModel) -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val mutedTextColor = if (isDarkTheme) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.6f)

    if (isLoading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (ingresos.isEmpty()) {
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
                    text = "No hay ingresos todavía",
                    style = MaterialTheme.typography.titleMedium,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Pulsa + para añadir tu primer ingreso",
                    style = MaterialTheme.typography.bodyMedium,
                    color = mutedTextColor
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    text = "Historial de ingresos",
                    style = MaterialTheme.typography.titleSmall,
                    color = mutedTextColor,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(ingresos, key = { it.id }) { ingreso ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(300)) +
                            slideInVertically(
                                animationSpec = tween(300),
                                initialOffsetY = { it / 2 }
                            ),
                    exit = fadeOut(animationSpec = tween(200))
                ) {
                    IngresoItem(
                        ingreso = ingreso,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IngresoItem(
    ingreso: IngresoModel,
    isDarkTheme: Boolean,
    onDelete: (String) -> Unit,
    onEdit: (IngresoModel) -> Unit
) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val greenColor = Color(0xFF4CAF50)

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
                .clickable { onEdit(ingreso) },
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
                        .background(greenColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = greenColor
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
                    Text(
                        text = ingreso.fecha.format(dateFormatter),
                        fontSize = 12.sp,
                        color = if (isDarkTheme) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.5f)
                    )
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
