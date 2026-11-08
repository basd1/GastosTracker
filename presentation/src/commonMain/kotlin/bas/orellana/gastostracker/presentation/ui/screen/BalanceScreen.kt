package bas.orellana.gastostracker.presentation.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import bas.orellana.gastostracker.domain.model.CategoriaPersonalizada
import bas.orellana.gastostracker.domain.model.IngresoModel
import bas.orellana.gastostracker.domain.model.Mes
import bas.orellana.gastostracker.presentation.ui.components.SelectorMes
import bas.orellana.gastostracker.presentation.resources.balance_disponible
import bas.orellana.gastostracker.presentation.ui.components.BottomNavBar
import bas.orellana.gastostracker.presentation.ui.components.GradientTopAppBar
import bas.orellana.gastostracker.presentation.ui.dialogs.AddIngresoDialog
import bas.orellana.gastostracker.presentation.ui.dialogs.ManageCategoriasDialog
import bas.orellana.gastostracker.presentation.ui.dialogs.MonthYearPickerDialog
import bas.orellana.gastostracker.presentation.ui.dialogs.SettingsDialog
import bas.orellana.gastostracker.presentation.viewmodel.BalanceViewModel
import bas.orellana.gastostracker.presentation.viewmodel.HomeViewModel
import bas.orellana.gastostracker.presentation.viewmodel.SettingsViewModel
import bas.orellana.gastostracker.presentation.util.formatDDMMYYYY
import org.koin.compose.viewmodel.koinViewModel
import bas.orellana.gastostracker.presentation.resources.Res
import bas.orellana.gastostracker.presentation.resources.accion_deshacer
import bas.orellana.gastostracker.presentation.resources.accion_eliminar
import bas.orellana.gastostracker.presentation.resources.anadir_ingreso
import bas.orellana.gastostracker.presentation.resources.boton_ingreso
import bas.orellana.gastostracker.presentation.resources.ingreso_eliminado
import bas.orellana.gastostracker.presentation.resources.ingresos_con_numero
import bas.orellana.gastostracker.presentation.resources.resumen_ahorro
import bas.orellana.gastostracker.presentation.resources.resumen_gastos
import bas.orellana.gastostracker.presentation.resources.resumen_ingresos
import bas.orellana.gastostracker.presentation.resources.titulo_balance
import bas.orellana.gastostracker.presentation.ui.components.etiquetaCategoria
import bas.orellana.gastostracker.presentation.util.formatMoney
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BalanceScreen(
    navController: NavController,
    viewModel: BalanceViewModel = koinViewModel(),
    homeViewModel: HomeViewModel = koinViewModel(),
    settingsViewModel: SettingsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val addIngresoState by viewModel.addIngresoState.collectAsState()
    val isDarkTheme by settingsViewModel.isDarkTheme.collectAsState()
    val categoriasPersonalizadas by homeViewModel.categoriasPersonalizadas.collectAsState()
    val homeState by homeViewModel.state.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var stashedIngreso by remember { mutableStateOf<IngresoModel?>(null) }
    val textoIngresoEliminado = stringResource(Res.string.ingreso_eliminado)
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
                    icon = null,
                    emoji = "\uD83D\uDCB0",
                    title = stringResource(Res.string.titulo_balance),
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
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(Res.string.anadir_ingreso),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(Res.string.boton_ingreso),
                            color = MaterialTheme.colorScheme.onPrimary,
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
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
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
                            mes = state.mes,
                            balance = state.balanceMes,
                            onMesAnterior = { viewModel.mesAnterior() },
                            onMesSiguiente = { viewModel.mesSiguiente() },
                            showMonthPicker = { viewModel.showMonthPicker() }
                        )
                    }

                    item {
                        Text(
                            text = stringResource(Res.string.ingresos_con_numero, state.ingresos.size),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    items(state.ingresos, key = { it.id }) { ingreso ->
                        IngresoItem(
                            ingreso = ingreso,
                            categoriasPersonalizadas = categoriasPersonalizadas,
                            onDelete = { ingresoEliminado ->
                                stashedIngreso = ingresoEliminado
                                viewModel.deleteIngreso(ingresoEliminado.id)
                                scope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = textoIngresoEliminado,
                                        actionLabel = textoDeshacer
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        stashedIngreso?.let { viewModel.restoreIngreso(it) }
                                    }
                                    stashedIngreso = null
                                }
                            },
                            onEdit = { viewModel.showEditIngresoDialog(it) }
                        )
                    }
                }
            }
        }

        if (state.showMonthPicker) {
            MonthYearPickerDialog(
                currentMonth = state.mes.numero,
                currentYear = state.mes.anio,
                onConfirm = { month, year ->
                    viewModel.seleccionarMes(Mes(year, month))
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
                fecha = addIngresoState.fecha,
                onConceptoChange = { viewModel.updateConcepto(it) },
                onMontoChange = { viewModel.updateMonto(it) },
                onCategoriaChange = { viewModel.updateCategoria(it) },
                onCategoriaPersonalizadaChange = { viewModel.updateCategoriaPersonalizada(it) },
                onFechaChange = { viewModel.updateFecha(it) },
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
            ManageCategoriasDialog(homeViewModel)
        }
    }
}

@Composable
private fun BalanceHeader(
    totalIngresos: Double,
    totalGastos: Double,
    totalAhorro: Double,
    mes: Mes,
    balance: Double,
    onMesAnterior: () -> Unit,
    onMesSiguiente: () -> Unit,
    showMonthPicker: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp, top = 8.dp),
        shape = MaterialTheme.shapes.small
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            SelectorMes(
                mes = mes,
                onAnterior = onMesAnterior,
                onSiguiente = onMesSiguiente,
                onElegir = showMonthPicker
            )

            Text(
                text = stringResource(Res.string.balance_disponible, formatMoney(balance)),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (balance >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = formatMoney(totalIngresos),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(Res.string.resumen_ingresos),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = formatMoney(totalGastos),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = stringResource(Res.string.resumen_gastos),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = formatMoney(totalAhorro),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = stringResource(Res.string.resumen_ahorro),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun IngresoItem(
    ingreso: IngresoModel,
    categoriasPersonalizadas: List<CategoriaPersonalizada>,
    onDelete: (IngresoModel) -> Unit,
    onEdit: (IngresoModel) -> Unit
) {
    val etiqueta = etiquetaCategoria(ingreso.categoria, ingreso.categoriaPersonalizadaId, categoriasPersonalizadas)
    val categoryColor = etiqueta?.color ?: MaterialTheme.colorScheme.primary

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                onDelete(ingreso)
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
                    onLongClick = { onEdit(ingreso) }
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
                        .background(categoryColor.copy(alpha = 0.2f), MaterialTheme.shapes.extraSmall),
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
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = ingreso.fecha.formatDDMMYYYY(),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (etiqueta != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(categoryColor, MaterialTheme.shapes.extraSmall)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = etiqueta.nombre,
                                fontSize = 11.sp,
                                color = categoryColor
                            )
                        }
                    }
                }
                Text(
                    text = formatMoney(ingreso.monto),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
