package bas.orellana.gastostracker.presentation.ui.screen

import bas.orellana.gastostracker.presentation.util.formatDecimal
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import bas.orellana.gastostracker.domain.model.Categoria
import bas.orellana.gastostracker.domain.model.CategoriaPersonalizada
import bas.orellana.gastostracker.domain.model.GastoModel
import bas.orellana.gastostracker.domain.model.Aportacion
import bas.orellana.gastostracker.domain.model.Mes
import bas.orellana.gastostracker.presentation.navigation.NavRoutes
import bas.orellana.gastostracker.presentation.navigation.PeticionFiltro
import bas.orellana.gastostracker.presentation.navigation.PeticionesFiltro
import bas.orellana.gastostracker.presentation.state.FiltroGastos
import bas.orellana.gastostracker.presentation.resources.toca_categoria_para_ver
import org.koin.compose.koinInject
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.PI
import kotlin.math.atan2
import bas.orellana.gastostracker.presentation.ui.components.BottomNavBar
import bas.orellana.gastostracker.presentation.ui.components.GradientTopAppBar
import bas.orellana.gastostracker.presentation.ui.dialogs.AddGastoDialog
import bas.orellana.gastostracker.presentation.ui.dialogs.MonthYearPickerDialog
import bas.orellana.gastostracker.presentation.viewmodel.HomeViewModel
import bas.orellana.gastostracker.presentation.viewmodel.GraphViewModel
import bas.orellana.gastostracker.domain.util.todayLocalDate
import org.koin.compose.viewmodel.koinViewModel
import bas.orellana.gastostracker.presentation.viewmodel.CuentasViewModel
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import bas.orellana.gastostracker.presentation.resources.Res
import bas.orellana.gastostracker.presentation.resources.ahorro_acumulado
import bas.orellana.gastostracker.presentation.resources.anadir_gasto
import bas.orellana.gastostracker.presentation.resources.periodo_3_meses
import bas.orellana.gastostracker.presentation.resources.periodo_este_mes
import bas.orellana.gastostracker.presentation.resources.periodo_total
import bas.orellana.gastostracker.presentation.resources.resumen_media_dia
import bas.orellana.gastostracker.presentation.resources.resumen_top_categoria
import bas.orellana.gastostracker.presentation.resources.resumen_total
import bas.orellana.gastostracker.presentation.resources.seleccionar_mes
import bas.orellana.gastostracker.presentation.resources.sin_datos_ahorro
import bas.orellana.gastostracker.presentation.resources.sin_gastos_periodo
import bas.orellana.gastostracker.presentation.resources.sin_gastos_registrados
import bas.orellana.gastostracker.presentation.resources.titulo_grafico
import bas.orellana.gastostracker.presentation.resources.toca_un_punto
import bas.orellana.gastostracker.presentation.ui.components.mesYAnio
import bas.orellana.gastostracker.presentation.ui.components.nombre
import bas.orellana.gastostracker.presentation.ui.components.nombreMesCorto
import bas.orellana.gastostracker.presentation.util.formatMoney
import org.jetbrains.compose.resources.stringResource

private enum class PeriodFilter {
    THIS_MONTH, LAST_3_MONTHS, ALL, SPECIFIC_MONTH
}

@Composable
fun GraphScreen(
    navController: NavController,
    viewModel: HomeViewModel = koinViewModel(),
    graphViewModel: GraphViewModel = koinViewModel(),
    cuentasViewModel: CuentasViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val addGastoState by viewModel.addGastoState.collectAsState()
    val cuentasState by cuentasViewModel.state.collectAsState()
    val categoriasPersonalizadas by graphViewModel.categoriasPersonalizadas.collectAsState()
    val todosLosGastos by graphViewModel.gastos.collectAsState()
    val diaInicio by graphViewModel.diaInicioMes.collectAsState()

    var selectedPeriod by remember { mutableStateOf(PeriodFilter.THIS_MONTH) }
    var selectedMonth by remember { mutableStateOf(todayLocalDate().monthNumber) }
    var selectedYear by remember { mutableStateOf(todayLocalDate().year) }
    var showMonthPicker by remember { mutableStateOf(false) }

    val filteredGastos = remember(todosLosGastos, selectedPeriod, selectedMonth, selectedYear, diaInicio) {
        filterGastosByPeriod(todosLosGastos, selectedPeriod, selectedMonth, selectedYear, diaInicio)
    }

    val gastosDelPeriodo = filteredGastos
    val ingresos by graphViewModel.ingresos.collectAsState()
    val peticionesFiltro = koinInject<PeticionesFiltro>()
    // Mes de referencia para la comparativa y la evolución: el elegido o, si no, el actual.
    val mesReferencia = if (selectedPeriod == PeriodFilter.SPECIFIC_MONTH) Mes(selectedYear, selectedMonth) else Mes.actual(diaInicio)
    val abrirGastosDeCategoria: (CategoriaData) -> Unit = { dato ->
        peticionesFiltro.pedir(peticionParaCategoria(dato, selectedPeriod, selectedMonth, selectedYear, todosLosGastos, diaInicio))
        navController.navigate(NavRoutes.HOME) {
            popUpTo(NavRoutes.HOME) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }


    val aportaciones by graphViewModel.aportaciones.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                GradientTopAppBar(
                    icon = null,
                    emoji = "\uD83D\uDCCA",
                    title = stringResource(Res.string.titulo_grafico),
                    onSettingsClick = { navController.navigate(NavRoutes.AJUSTES) }
                )
            },
            bottomBar = { BottomNavBar(navController = navController) }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                PeriodFilterChips(
                    selectedPeriod = selectedPeriod,
                    selectedMonth = selectedMonth,
                    selectedYear = selectedYear,
                    onPeriodChange = { period ->
                        selectedPeriod = period
                        if (period != PeriodFilter.SPECIFIC_MONTH) {
                            selectedMonth = todayLocalDate().monthNumber
                            selectedYear = todayLocalDate().year
                        }
                    },
                    onMonthClick = { showMonthPicker = true }
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (gastosDelPeriodo.isEmpty() && aportaciones.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(
                                if (todosLosGastos.isEmpty()) Res.string.sin_gastos_registrados
                                else Res.string.sin_gastos_periodo
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 16.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            SummaryCards(
                                gastos = gastosDelPeriodo,
                                selectedPeriod = selectedPeriod,
                                selectedMonth = selectedMonth,
                                selectedYear = selectedYear,
                                diaInicio = diaInicio,
                                categoriasPersonalizadas = categoriasPersonalizadas
                            )
                        }

                        item {
                            DonutChart(
                                gastos = gastosDelPeriodo,
                                categoriasPersonalizadas = categoriasPersonalizadas,
                                onCategoria = abrirGastosDeCategoria
                            )
                        }

                        item {
                            LegendSection(
                                gastos = gastosDelPeriodo,
                                categoriasPersonalizadas = categoriasPersonalizadas,
                                onCategoria = abrirGastosDeCategoria
                            )
                        }

                        item {
                            IngresosVsGastosCard(gastos = todosLosGastos, ingresos = ingresos, hasta = mesReferencia, diaInicio = diaInicio)
                        }

                        item {
                            EvolucionCategoriaCard(
                                gastos = todosLosGastos,
                                categoriasPersonalizadas = categoriasPersonalizadas,
                                hasta = mesReferencia,
                                categoriaInicial = null,
                                diaInicio = diaInicio
                            )
                        }

                        item {
                            ComparativaAnualCard(gastos = todosLosGastos, categoriasPersonalizadas = categoriasPersonalizadas, mes = mesReferencia, diaInicio = diaInicio)
                        }

                        item {
                            MonthlySavingsLineChart(
                                aportaciones = aportaciones
                            )
                        }

                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Button(
                                    onClick = { viewModel.showAddGastoDialog() },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    shape = MaterialTheme.shapes.small,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(stringResource(Res.string.anadir_gasto), fontSize = 15.sp)
                                }
                            }
                        }

                        item { Spacer(modifier = Modifier.height(8.dp)) }
                    }
                }
            }
        }

        if (showMonthPicker) {
            MonthYearPickerDialog(
                currentMonth = selectedMonth,
                currentYear = selectedYear,
                onConfirm = { month, year ->
                    selectedMonth = month
                    selectedYear = year
                    selectedPeriod = PeriodFilter.SPECIFIC_MONTH
                    showMonthPicker = false
                },
                onDismiss = { showMonthPicker = false }
            )
        }

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
                onRepetirMensualChange = { viewModel.updateAddGastoRepetir(it) },
                nota = addGastoState.nota,
                onNotaChange = { viewModel.updateAddGastoNota(it) },
                metodoPago = addGastoState.metodoPago,
                onMetodoPagoChange = { viewModel.updateAddGastoMetodoPago(it) },
                cuentas = cuentasState.activas,
                cuentaId = addGastoState.cuentaId,
                onCuentaChange = { viewModel.updateAddGastoCuenta(it) }
            )
        }
    }
}

@Composable
private fun PeriodFilterChips(
    selectedPeriod: PeriodFilter,
    selectedMonth: Int,
    selectedYear: Int,
    onPeriodChange: (PeriodFilter) -> Unit,
    onMonthClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        PeriodFilter.entries.filter { it != PeriodFilter.SPECIFIC_MONTH }.forEach { period ->
            val label = when (period) {
                PeriodFilter.THIS_MONTH -> stringResource(Res.string.periodo_este_mes)
                PeriodFilter.LAST_3_MONTHS -> stringResource(Res.string.periodo_3_meses)
                PeriodFilter.ALL -> stringResource(Res.string.periodo_total)
                PeriodFilter.SPECIFIC_MONTH -> ""
            }
            FilterChip(
                selected = selectedPeriod == period,
                onClick = { onPeriodChange(period) },
                label = {
                    Text(
                        text = label,
                        fontSize = 13.sp,
                        fontWeight = if (selectedPeriod == period) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    selectedBorderColor = MaterialTheme.colorScheme.primary,
                    enabled = true,
                    selected = selectedPeriod == period
                )
            )
        }

        FilterChip(
            selected = selectedPeriod == PeriodFilter.SPECIFIC_MONTH,
            onClick = onMonthClick,
            label = {
                Text(
                    text = mesYAnio(selectedMonth, selectedYear),
                    fontSize = 13.sp,
                    fontWeight = if (selectedPeriod == PeriodFilter.SPECIFIC_MONTH) FontWeight.Bold else FontWeight.Normal
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = stringResource(Res.string.seleccionar_mes),
                    modifier = Modifier.size(18.dp)
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            border = FilterChipDefaults.filterChipBorder(
                borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                selectedBorderColor = MaterialTheme.colorScheme.primary,
                enabled = true,
                selected = selectedPeriod == PeriodFilter.SPECIFIC_MONTH
            )
        )
    }
}

@Composable
private fun SummaryCards(
    gastos: List<GastoModel>,
    selectedPeriod: PeriodFilter,
    selectedMonth: Int = todayLocalDate().monthNumber,
    selectedYear: Int = todayLocalDate().year,
    diaInicio: Int = 1,
    categoriasPersonalizadas: List<CategoriaPersonalizada>
) {
    val total = gastos.sumOf { it.monto }

    val topCategoria = calcularTotalesPorCategoria(gastos, categoriasPersonalizadas).firstOrNull()

    val daysInPeriod = when (selectedPeriod) {
        PeriodFilter.THIS_MONTH -> {
            val now = todayLocalDate()
            val startOfMonth = Mes.actual(diaInicio).periodo(diaInicio).start
            startOfMonth.daysUntil(now) + 1
        }
        PeriodFilter.LAST_3_MONTHS -> {
            val now = todayLocalDate()
            val start = now.minus(3, DateTimeUnit.MONTH)
            start.daysUntil(now) + 1
        }
        PeriodFilter.SPECIFIC_MONTH -> {
            Mes(selectedYear, selectedMonth).periodo(diaInicio).let { it.start.daysUntil(it.endInclusive) + 1 }
        }
        PeriodFilter.ALL -> {
            if (gastos.isEmpty()) 1
            else gastos.minOf { it.fecha }.daysUntil(todayLocalDate()) + 1
        }
    }
    val dailyAvg = if (daysInPeriod > 0) total / daysInPeriod else 0.0

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SummaryCard(
            title = stringResource(Res.string.resumen_total),
            value = formatMoney(total),
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            title = stringResource(Res.string.resumen_top_categoria),
            value = topCategoria?.let { "${it.nombre()} ${formatDecimal(it.porcentaje, 0)}%" } ?: "-",
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            title = stringResource(Res.string.resumen_media_dia),
            value = formatMoney(dailyAvg),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    modifier: Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = MaterialTheme.shapes.small
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/** Gráfico de anillo por categoría. Tocar una sección abre la lista de gastos de esa categoría. */
@Composable
private fun DonutChart(
    gastos: List<GastoModel>,
    categoriasPersonalizadas: List<CategoriaPersonalizada>,
    onCategoria: (CategoriaData) -> Unit = {}
) {
    val total = gastos.sumOf { it.monto }
    val sortedData = calcularTotalesPorCategoria(gastos, categoriasPersonalizadas)

    val animationProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 800),
        label = "donut"
    )

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(240.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .size(240.dp)
                    .pointerInput(sortedData) {
                        detectTapGestures { toque ->
                            val centro = Offset(size.width / 2f, size.height / 2f)
                            val distancia = (toque - centro).getDistance()
                            val radioExterior = size.width / 2f
                            val grosor = radioExterior * 0.38f
                            if (distancia > radioExterior || distancia < radioExterior - grosor * 1.4f) return@detectTapGestures
                            // Ángulo desde las 12 en punto, en el sentido de las agujas del reloj.
                            var angulo = (atan2(toque.y - centro.y, toque.x - centro.x) * 180f / PI.toFloat()) + 90f
                            if (angulo < 0f) angulo += 360f
                            var acumulado = 0f
                            sortedData.forEach { dato ->
                                acumulado += dato.porcentaje / 100f * 360f
                                if (angulo <= acumulado) {
                                    onCategoria(dato)
                                    return@detectTapGestures
                                }
                            }
                        }
                    }
            ) {
                val grosor = size.minDimension / 2f * 0.38f
                val inset = grosor / 2f
                var startAngle = -90f
                sortedData.forEach { data ->
                    val sweepAngle = (data.porcentaje / 100f) * 360f * animationProgress
                    drawArc(
                        color = data.color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = Offset(inset, inset),
                        size = Size(size.width - grosor, size.height - grosor),
                        style = Stroke(width = grosor)
                    )
                    startAngle += (data.porcentaje / 100f) * 360f
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(Res.string.resumen_total),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatMoney(total),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(Res.string.toca_categoria_para_ver),
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LegendSection(
    gastos: List<GastoModel>,
    categoriasPersonalizadas: List<CategoriaPersonalizada>,
    onCategoria: (CategoriaData) -> Unit = {}
) {
    val sortedData = calcularTotalesPorCategoria(gastos, categoriasPersonalizadas)

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        sortedData.forEach { data ->
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onCategoria(data) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = MaterialTheme.shapes.extraSmall
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(data.color)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = data.nombre(),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = formatMoney(data.monto),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${formatDecimal(data.porcentaje, 1)}%",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(data.porcentaje / 100f)
                            .height(3.dp)
                            .background(data.color)
                    )
                }
            }
        }
    }
}

private data class MonthlySaving(
    val year: Int,
    val month: Int,
    val amount: Double
)

@Composable
private fun MonthlySavingsLineChart(
    aportaciones: List<Aportacion>
) {
    val monthlyData = remember(aportaciones) {
        val sorted = aportaciones
            .groupBy { it.fecha.year * 12 + (it.fecha.monthNumber - 1) }
            .map { (key, items) ->
                val year = key / 12
                val month = key % 12
                MonthlySaving(
                    year = year,
                    month = month + 1,
                    amount = items.sumOf { it.monto }
                )
            }
            .sortedBy { it.year * 12 + (it.month - 1) }

        var runningTotal = 0.0
        sorted.map { data ->
            runningTotal += data.amount
            data.copy(amount = runningTotal)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = MaterialTheme.shapes.small
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = stringResource(Res.string.ahorro_acumulado),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (monthlyData.isEmpty()) {
                Text(
                    text = stringResource(Res.string.sin_datos_ahorro),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            } else {
                val ahorroColor = MaterialTheme.colorScheme.tertiary
                val maxAmount = monthlyData.maxOf { it.amount }.coerceAtLeast(1.0)
                val chartHeight = 160.dp
                val bottomMargin = 32.dp

                val horizontalPadding = 20.dp
                val minSpacing = 25.dp
                val density = LocalDensity.current

                var selectedIndex by remember { mutableStateOf(-1) }
                var viewportWidth by remember { mutableStateOf(0f) }
                val chartHeightPx = with(density) { chartHeight.toPx() }
                val horizontalPaddingPx = with(density) { horizontalPadding.toPx() }
                val minSpacingPx = with(density) { minSpacing.toPx() }

                val totalChartWidthPx = remember(monthlyData.size, viewportWidth, minSpacingPx, horizontalPaddingPx) {
                    if (monthlyData.size < 2) viewportWidth.coerceAtLeast(1f)
                    else {
                        val fillSpacingPx = (viewportWidth - 2 * horizontalPaddingPx) / (monthlyData.size - 1).coerceAtLeast(1)
                        val actualSpacingPx = maxOf(fillSpacingPx, minSpacingPx)
                        (monthlyData.size - 1) * actualSpacingPx + 2 * horizontalPaddingPx
                    }
                }
                val totalChartWidth = with(density) { totalChartWidthPx.toDp() }

                val points = remember(monthlyData, totalChartWidthPx, maxAmount, horizontalPaddingPx) {
                    if (totalChartWidthPx <= 0f || monthlyData.size < 2) emptyList()
                    else {
                        val stepX = (totalChartWidthPx - 2 * horizontalPaddingPx) / (monthlyData.size - 1)
                        monthlyData.mapIndexed { index, data ->
                            val x = horizontalPaddingPx + stepX * index
                            val y = ((1f - (data.amount / maxAmount).toFloat()) * (chartHeightPx - 10f)) + 5f
                            Offset(x, y)
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .onSizeChanged { viewportWidth = it.width.toFloat() }
                        .height(chartHeight + bottomMargin)
                ) {
                    Canvas(
                        modifier = Modifier
                            .width(totalChartWidth)
                            .height(chartHeight)
                            .pointerInput(points) {
                                detectTapGestures { tapOffset ->
                                    if (points.isNotEmpty()) {
                                        val threshold = 30.dp.toPx()
                                        val touched = points.indexOfFirst {
                                            (it - tapOffset).getDistance() <= threshold
                                        }
                                        selectedIndex = if (touched >= 0) touched else -1
                                    }
                                }
                            }
                    ) {
                        if (monthlyData.size == 1) {
                            val x = size.width / 2f
                            val y = ((1f - (monthlyData[0].amount / maxAmount).toFloat()) * (size.height - 10f)) + 5f
                            val radius = if (selectedIndex == 0) 10.dp.toPx() else 6.dp.toPx()
                            val innerRadius = if (selectedIndex == 0) 5.dp.toPx() else 3.dp.toPx()
                            drawCircle(color = ahorroColor, radius = radius, center = Offset(x, y))
                            drawCircle(color = Color.White, radius = innerRadius, center = Offset(x, y))
                        } else if (monthlyData.size >= 2 && points.isNotEmpty()) {
                            for (i in 0 until points.size - 1) {
                                drawLine(
                                    color = ahorroColor,
                                    start = points[i],
                                    end = points[i + 1],
                                    strokeWidth = 3.dp.toPx()
                                )
                            }

                            points.forEachIndexed { index, point ->
                                val isSelected = index == selectedIndex
                                val radius = if (isSelected) 10.dp.toPx() else 6.dp.toPx()
                                val innerRadius = if (isSelected) 5.dp.toPx() else 3.dp.toPx()
                                drawCircle(color = ahorroColor, radius = radius, center = point)
                                drawCircle(color = Color.White, radius = innerRadius, center = point)
                            }
                        }
                    }

                    if (monthlyData.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .padding(top = chartHeight)
                                .padding(horizontal = horizontalPadding),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val labelsToShow = if (monthlyData.size <= 6) monthlyData
                            else monthlyData.filterIndexed { index, _ ->
                                index == 0 || index == monthlyData.size - 1 || monthlyData[index].month == 1
                            }
                            labelsToShow.forEach { data ->
                                Text(
                                    text = "${nombreMesCorto(data.month)} ${data.year}",
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                val selectedData = if (selectedIndex in monthlyData.indices) monthlyData[selectedIndex] else null
                if (selectedData != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${nombreMesCorto(selectedData.month)} ${selectedData.year}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = formatMoney(selectedData.amount),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ahorroColor
                        )
                    }
                } else {
                    Text(
                        text = stringResource(Res.string.toca_un_punto),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}

private fun daysInMonth(year: Int, month: Int): Int {
    val first = LocalDate(year, month, 1)
    return first.daysUntil(first.plus(1, DateTimeUnit.MONTH))
}

private fun filterGastosByPeriod(
    gastos: List<GastoModel>,
    period: PeriodFilter,
    selectedMonth: Int = todayLocalDate().monthNumber,
    selectedYear: Int = todayLocalDate().year,
    diaInicio: Int = 1
): List<GastoModel> {
    val now = todayLocalDate()
    return when (period) {
        PeriodFilter.THIS_MONTH -> {
            val startOfMonth = Mes.actual(diaInicio).periodo(diaInicio).start
            gastos.filter { it.fecha >= startOfMonth && it.fecha <= now }
        }
        PeriodFilter.LAST_3_MONTHS -> {
            val start = now.minus(3, DateTimeUnit.MONTH)
            gastos.filter { it.fecha >= start && it.fecha <= now }
        }
        PeriodFilter.ALL -> gastos
        PeriodFilter.SPECIFIC_MONTH -> {
            val periodo = Mes(selectedYear, selectedMonth).periodo(diaInicio)
            gastos.filter { it.fecha in periodo }
        }
    }
}

/**
 * Total de una categoría en el periodo. Los gastos sin categoría, o con una personalizada que ya
 * no existe, cuentan como "Otros".
 */
private data class CategoriaData(
    val categoria: Categoria?,
    val nombrePersonalizada: String?,
    val colorArgb: Long,
    val monto: Double,
    val porcentaje: Float,
    val categoriaPersonalizadaId: String? = null
) {
    val color: Color get() = Color(colorArgb)
}

@Composable
private fun CategoriaData.nombre(): String = categoria?.nombre() ?: nombrePersonalizada.orEmpty()

/** Totales por categoría, de mayor a menor importe. */
private fun calcularTotalesPorCategoria(
    gastos: List<GastoModel>,
    categoriasPersonalizadas: List<CategoriaPersonalizada>
): List<CategoriaData> {
    val total = gastos.sumOf { it.monto }
    if (total == 0.0) return emptyList()

    return gastos
        .groupBy { gasto ->
            val personalizada = gasto.categoriaPersonalizadaId?.let { id -> categoriasPersonalizadas.find { it.id == id } }
            when {
                gasto.categoria != null -> gasto.categoria
                personalizada != null -> personalizada
                else -> Categoria.OTROS
            }
        }
        .map { (clave, grupo) ->
            val monto = grupo.sumOf { it.monto }
            val porcentaje = ((monto / total) * 100).toFloat()
            when (clave) {
                is Categoria -> CategoriaData(clave, null, clave.color, monto, porcentaje)
                is CategoriaPersonalizada -> CategoriaData(null, clave.nombre, clave.color, monto, porcentaje, clave.id)
                else -> error("Clave de categoría inesperada: $clave")
            }
        }
        .sortedByDescending { it.monto }
}

/**
 * Petición para abrir Gastos con la categoría tocada y el mismo periodo del gráfico: un mes si el
 * gráfico es de un mes, o un rango de fechas para "3 meses" y "Total".
 * Los gastos sin categoría salen en el gráfico como "Otros"; al tocar "Otros" solo se filtra esa categoría.
 */
private fun peticionParaCategoria(
    dato: CategoriaData,
    periodo: PeriodFilter,
    mes: Int,
    anio: Int,
    todos: List<GastoModel>,
    diaInicio: Int
): PeticionFiltro {
    val porCategoria = FiltroGastos(
        categorias = setOfNotNull(dato.categoria),
        personalizadas = setOfNotNull(dato.categoriaPersonalizadaId)
    )
    val hoy = todayLocalDate()
    return when (periodo) {
        PeriodFilter.THIS_MONTH -> PeticionFiltro(porCategoria, Mes.actual(diaInicio))
        PeriodFilter.SPECIFIC_MONTH -> PeticionFiltro(porCategoria, Mes(anio, mes))
        PeriodFilter.LAST_3_MONTHS -> PeticionFiltro(porCategoria.copy(desde = hoy.minus(3, DateTimeUnit.MONTH), hasta = hoy))
        PeriodFilter.ALL -> PeticionFiltro(porCategoria.copy(desde = todos.minOfOrNull { it.fecha } ?: hoy, hasta = hoy))
    }
}
