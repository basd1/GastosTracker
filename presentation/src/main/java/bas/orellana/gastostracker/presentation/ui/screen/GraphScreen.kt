package bas.orellana.gastostracker.presentation.ui.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import bas.orellana.gastostracker.domain.model.Categoria
import bas.orellana.gastostracker.domain.model.CategoriaPersonalizada
import bas.orellana.gastostracker.domain.model.GastoModel
import bas.orellana.gastostracker.presentation.ui.components.BottomNavBar
import bas.orellana.gastostracker.presentation.ui.components.GradientTopAppBar
import bas.orellana.gastostracker.presentation.ui.dialogs.ManageCategoriasDialog
import bas.orellana.gastostracker.presentation.ui.dialogs.MonthYearPickerDialog
import bas.orellana.gastostracker.presentation.ui.dialogs.SettingsDialog
import bas.orellana.gastostracker.presentation.viewmodel.HomeViewModel
import bas.orellana.gastostracker.presentation.viewmodel.SettingsViewModel
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.temporal.ChronoUnit

private enum class PeriodFilter {
    THIS_MONTH, LAST_3_MONTHS, ALL, SPECIFIC_MONTH
}

@Composable
fun GraphScreen(
    navController: NavController,
    viewModel: HomeViewModel = koinViewModel(),
    settingsViewModel: SettingsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val categoriasPersonalizadas by viewModel.categoriasPersonalizadas.collectAsState()
    val manageCategoriasState by viewModel.manageCategoriasState.collectAsState()
    val isDarkTheme by settingsViewModel.isDarkTheme.collectAsState()

    var selectedPeriod by remember { mutableStateOf(PeriodFilter.THIS_MONTH) }
    var selectedMonth by remember { mutableStateOf(LocalDate.now().monthValue) }
    var selectedYear by remember { mutableStateOf(LocalDate.now().year) }
    var showMonthPicker by remember { mutableStateOf(false) }

    val filteredGastos = remember(state.gastos, selectedPeriod, selectedMonth, selectedYear) {
        filterGastosByPeriod(state.gastos, selectedPeriod, selectedMonth, selectedYear)
    }

    val gastosSinAhorro = remember(filteredGastos) {
        filteredGastos.filter { it.categoria != Categoria.AHORRO }
    }

    val gastosAhorro = remember(filteredGastos) {
        filteredGastos.filter { it.categoria == Categoria.AHORRO }
    }

    val textColor = if (isDarkTheme) Color.White else Color.Black
    val mutedTextColor = if (isDarkTheme) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.6f)
    val cardBg = if (isDarkTheme) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.06f)
    val surfaceBg = if (isDarkTheme) Color(0xFF121212) else Color.White

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(surfaceBg)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                GradientTopAppBar(
                    icon = null,
                    emoji = "\uD83D\uDCCA",
                    title = "Gr\u00E1fico",
                    onSettingsClick = { viewModel.showSettingsDialog() }
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
                            selectedMonth = LocalDate.now().monthValue
                            selectedYear = LocalDate.now().year
                        }
                    },
                    onMonthClick = { showMonthPicker = true },
                    textColor = textColor,
                    cardBg = cardBg
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (gastosSinAhorro.isEmpty() && gastosAhorro.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (state.gastos.isEmpty()) "No hay gastos registrados"
                                   else "No hay gastos en este per\u00EDodo",
                            color = mutedTextColor,
                            fontSize = 16.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            SummaryCards(
                                gastos = gastosSinAhorro,
                                selectedPeriod = selectedPeriod,
                                selectedMonth = selectedMonth,
                                selectedYear = selectedYear,
                                categoriasPersonalizadas = categoriasPersonalizadas,
                                textColor = textColor,
                                mutedTextColor = mutedTextColor,
                                cardBg = cardBg
                            )
                        }

                        item {
                            DonutChart(
                                gastos = gastosSinAhorro,
                                categoriasPersonalizadas = categoriasPersonalizadas,
                                textColor = textColor,
                                mutedTextColor = mutedTextColor
                            )
                        }

                        item {
                            LegendSection(
                                gastos = gastosSinAhorro,
                                categoriasPersonalizadas = categoriasPersonalizadas,
                                textColor = textColor,
                                mutedTextColor = mutedTextColor,
                                cardBg = cardBg
                            )
                        }

                        item {
                            MonthlySavingsLineChart(
                                gastos = gastosAhorro,
                                textColor = textColor,
                                mutedTextColor = mutedTextColor,
                                cardBg = cardBg
                            )
                        }

                        item { Spacer(modifier = Modifier.height(8.dp)) }
                    }
                }

                Button(
                    onClick = { viewModel.seedTestAhorroData() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFD700).copy(alpha = 0.3f)
                    )
                ) {
                    Text(
                        text = "\uD83D\uDCB0 Generar datos de prueba AHORRO",
                        fontSize = 14.sp,
                        color = textColor
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
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
                onDismiss = { viewModel.hideManageCategoriasDialog() }
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
    onMonthClick: () -> Unit,
    textColor: Color,
    cardBg: Color
) {
    val monthNames = listOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        PeriodFilter.entries.filter { it != PeriodFilter.SPECIFIC_MONTH }.forEach { period ->
            val label = when (period) {
                PeriodFilter.THIS_MONTH -> "Este mes"
                PeriodFilter.LAST_3_MONTHS -> "3 meses"
                PeriodFilter.ALL -> "Total"
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
                    containerColor = cardBg,
                    selectedContainerColor = Color(0xFF2E7D32).copy(alpha = 0.3f),
                    labelColor = textColor,
                    selectedLabelColor = Color.White
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = textColor.copy(alpha = 0.2f),
                    selectedBorderColor = Color(0xFF2E7D32),
                    enabled = true,
                    selected = selectedPeriod == period
                )
            )
        }

        Card(
            modifier = Modifier.clickable(onClick = onMonthClick),
            colors = CardDefaults.cardColors(
                containerColor = if (selectedPeriod == PeriodFilter.SPECIFIC_MONTH)
                    Color(0xFF2E7D32).copy(alpha = 0.3f) else cardBg
            ),
            shape = RoundedCornerShape(8.dp),
            border = if (selectedPeriod == PeriodFilter.SPECIFIC_MONTH)
                androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E7D32))
            else null
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${monthNames[selectedMonth - 1]} $selectedYear",
                    fontSize = 13.sp,
                    fontWeight = if (selectedPeriod == PeriodFilter.SPECIFIC_MONTH) FontWeight.Bold else FontWeight.Normal,
                    color = textColor
                )
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Seleccionar mes",
                    tint = textColor.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun SummaryCards(
    gastos: List<GastoModel>,
    selectedPeriod: PeriodFilter,
    selectedMonth: Int = LocalDate.now().monthValue,
    selectedYear: Int = LocalDate.now().year,
    categoriasPersonalizadas: List<CategoriaPersonalizada>,
    textColor: Color,
    mutedTextColor: Color,
    cardBg: Color
) {
    val total = gastos.sumOf { it.monto }

    val topCategoriaData = calcularPorcentajesPorCategoria(gastos, categoriasPersonalizadas)
        .values.maxByOrNull { it.porcentaje }

    val daysInPeriod = when (selectedPeriod) {
        PeriodFilter.THIS_MONTH -> {
            val now = LocalDate.now()
            val startOfMonth = now.withDayOfMonth(1)
            ChronoUnit.DAYS.between(startOfMonth, now).toInt() + 1
        }
        PeriodFilter.LAST_3_MONTHS -> {
            val now = LocalDate.now()
            val start = now.minusMonths(3)
            ChronoUnit.DAYS.between(start, now).toInt() + 1
        }
        PeriodFilter.SPECIFIC_MONTH -> {
            val daysInMonth = java.time.YearMonth.of(selectedYear, selectedMonth).lengthOfMonth()
            daysInMonth
        }
        PeriodFilter.ALL -> {
            if (gastos.isEmpty()) 1
            else ChronoUnit.DAYS.between(gastos.minOf { it.fecha }, LocalDate.now()).toInt() + 1
        }
    }
    val dailyAvg = if (daysInPeriod > 0) total / daysInPeriod else 0.0

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SummaryCard(
            title = "Total",
            value = "\u20AC${String.format("%.2f", total)}",
            modifier = Modifier.weight(1f),
            textColor = textColor,
            mutedTextColor = mutedTextColor,
            cardBg = cardBg
        )
        SummaryCard(
            title = "Top categor\u00EDa",
            value = topCategoriaData?.let { "${it.nombre} ${String.format("%.0f", it.porcentaje)}%" } ?: "-",
            modifier = Modifier.weight(1f),
            textColor = textColor,
            mutedTextColor = mutedTextColor,
            cardBg = cardBg
        )
        SummaryCard(
            title = "Media/d\u00EDa",
            value = "\u20AC${String.format("%.2f", dailyAvg)}",
            modifier = Modifier.weight(1f),
            textColor = textColor,
            mutedTextColor = mutedTextColor,
            cardBg = cardBg
        )
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    modifier: Modifier,
    textColor: Color,
    mutedTextColor: Color,
    cardBg: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                color = mutedTextColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

@Composable
private fun DonutChart(
    gastos: List<GastoModel>,
    categoriasPersonalizadas: List<CategoriaPersonalizada>,
    textColor: Color,
    mutedTextColor: Color
) {
    val total = gastos.sumOf { it.monto }
    val categoriaData = calcularPorcentajesPorCategoria(gastos, categoriasPersonalizadas)
    val sortedData = categoriaData.values.sortedByDescending { it.porcentaje }

    val animationProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 800),
        label = "donut"
    )

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(260.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(260.dp)) {
                var startAngle = -90f
                sortedData.forEach { data ->
                    val sweepAngle = (data.porcentaje / 100f) * 360f * animationProgress
                    drawArc(
                        color = data.color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true
                    )
                    startAngle += (data.porcentaje / 100f) * 360f
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Total",
                    fontSize = 12.sp,
                    color = mutedTextColor
                )
                Text(
                    text = "\u20AC${String.format("%.2f", total)}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
        }
    }
}

@Composable
private fun LegendSection(
    gastos: List<GastoModel>,
    categoriasPersonalizadas: List<CategoriaPersonalizada>,
    textColor: Color,
    mutedTextColor: Color,
    cardBg: Color
) {
    val categoriaData = calcularPorcentajesPorCategoria(gastos, categoriasPersonalizadas)
    val sortedData = categoriaData.values.sortedByDescending { it.porcentaje }
    val total = gastos.sumOf { it.monto }

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        sortedData.forEach { data ->
            val monto = total * (data.porcentaje / 100f)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(10.dp)
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
                        text = data.nombre,
                        color = textColor,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "\u20AC${String.format("%.2f", monto)}",
                        color = textColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${String.format("%.1f", data.porcentaje)}%",
                        color = mutedTextColor,
                        fontSize = 13.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(cardBg)
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
    val label: String,
    val amount: Double
)

@Composable
private fun MonthlySavingsLineChart(
    gastos: List<GastoModel>,
    textColor: Color,
    mutedTextColor: Color,
    cardBg: Color
) {
    val monthNames = listOf(
        "Ene", "Feb", "Mar", "Abr", "May", "Jun",
        "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"
    )

    val monthlyData = remember(gastos) {
        gastos
            .groupBy { it.fecha.year * 12 + (it.fecha.monthValue - 1) }
            .map { (key, items) ->
                val year = key / 12
                val month = key % 12
                MonthlySaving(
                    year = year,
                    month = month + 1,
                    label = "${monthNames[month]} $year",
                    amount = items.sumOf { it.monto }
                )
            }
            .sortedBy { it.year * 12 + (it.month - 1) }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = "Ahorro mensual",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (monthlyData.isEmpty()) {
                Text(
                    text = "No hay datos de ahorro",
                    color = mutedTextColor,
                    fontSize = 12.sp
                )
            } else {
                val ahorroColor = Color(0xFFFFD700)
                val maxAmount = monthlyData.maxOf { it.amount }.coerceAtLeast(1.0)
                val chartHeight = 160.dp
                val bottomMargin = 32.dp

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(chartHeight + bottomMargin)
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(chartHeight)
                    ) {
                        if (monthlyData.size == 1) {
                            val x = size.width / 2f
                            val y = ((1f - (monthlyData[0].amount / maxAmount).toFloat()) * (size.height - 10f)) + 5f
                            drawCircle(
                                color = ahorroColor,
                                radius = 6.dp.toPx(),
                                center = Offset(x, y)
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 3.dp.toPx(),
                                center = Offset(x, y)
                            )
                        } else if (monthlyData.size >= 2) {
                            val stepX = size.width / (monthlyData.size - 1).coerceAtLeast(1)
                            val points = monthlyData.mapIndexed { index, data ->
                                val x = stepX * index
                                val y = ((1f - (data.amount / maxAmount).toFloat()) * (size.height - 10f)) + 5f
                                Offset(x, y)
                            }

                            for (i in 0 until points.size - 1) {
                                drawLine(
                                    color = ahorroColor,
                                    start = points[i],
                                    end = points[i + 1],
                                    strokeWidth = 3.dp.toPx()
                                )
                            }

                            points.forEach { point ->
                                drawCircle(
                                    color = ahorroColor,
                                    radius = 6.dp.toPx(),
                                    center = point
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = 3.dp.toPx(),
                                    center = point
                                )
                            }
                        }
                    }

                    if (monthlyData.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .padding(top = chartHeight),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val labelsToShow = if (monthlyData.size <= 6) monthlyData
                            else monthlyData.filterIndexed { index, _ ->
                                index == 0 || index == monthlyData.size - 1 || monthlyData[index].month == 1
                            }
                            labelsToShow.forEach { data ->
                                Text(
                                    text = data.label,
                                    fontSize = 9.sp,
                                    color = mutedTextColor,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                monthlyData.forEach { data ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = data.label,
                            fontSize = 11.sp,
                            color = mutedTextColor
                        )
                        Text(
                            text = "\u20AC${String.format("%.2f", data.amount)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ahorroColor
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                }
            }
        }
    }
}

private fun obtenerColorCategoria(gasto: GastoModel, categoriasPersonalizadas: List<CategoriaPersonalizada>): Color {
    return when {
        gasto.categoria != null -> Color(gasto.categoria!!.color)
        gasto.categoriaPersonalizadaId != null -> categoriasPersonalizadas
            .find { it.id == gasto.categoriaPersonalizadaId }?.let { Color(it.color) } ?: Color(0xFF90A4AE)
        else -> Color(0xFF90A4AE)
    }
}

private fun filterGastosByPeriod(
    gastos: List<GastoModel>,
    period: PeriodFilter,
    selectedMonth: Int = LocalDate.now().monthValue,
    selectedYear: Int = LocalDate.now().year
): List<GastoModel> {
    val now = LocalDate.now()
    return when (period) {
        PeriodFilter.THIS_MONTH -> {
            val startOfMonth = now.withDayOfMonth(1)
            gastos.filter { !it.fecha.isBefore(startOfMonth) && !it.fecha.isAfter(now) }
        }
        PeriodFilter.LAST_3_MONTHS -> {
            val start = now.minusMonths(3)
            gastos.filter { !it.fecha.isBefore(start) && !it.fecha.isAfter(now) }
        }
        PeriodFilter.ALL -> gastos
        PeriodFilter.SPECIFIC_MONTH -> {
            gastos.filter { it.fecha.monthValue == selectedMonth && it.fecha.year == selectedYear }
        }
    }
}

private data class CategoriaData(
    val nombre: String,
    val porcentaje: Float,
    val color: Color
)

private fun calcularPorcentajesPorCategoria(
    gastos: List<GastoModel>,
    categoriasPersonalizadas: List<CategoriaPersonalizada>
): Map<String, CategoriaData> {
    val total = gastos.sumOf { it.monto }
    if (total == 0.0) return emptyMap()

    val categoriaMontos = mutableMapOf<String, Double>()

    gastos.forEach { gasto ->
        val nombreCategoria = when {
            gasto.categoria != null -> gasto.categoria!!.displayName
            gasto.categoriaPersonalizadaId != null -> categoriasPersonalizadas
                .find { it.id == gasto.categoriaPersonalizadaId }?.nombre ?: "Otros"
            else -> "Otros"
        }
        categoriaMontos[nombreCategoria] = (categoriaMontos[nombreCategoria] ?: 0.0) + gasto.monto
    }

    val result = mutableMapOf<String, CategoriaData>()

    categoriaMontos.forEach { (nombre, monto) ->
        val porcentaje = ((monto / total) * 100).toFloat()

        val color = when {
            gastos.any { it.categoria?.displayName == nombre } -> {
                val cat = gastos.first { it.categoria?.displayName == nombre }.categoria
                Color(cat!!.color)
            }
            else -> {
                val catPersonalizada = categoriasPersonalizadas.find { it.nombre == nombre }
                if (catPersonalizada != null) Color(catPersonalizada.color)
                else Color(0xFF90A4AE)
            }
        }
        result[nombre] = CategoriaData(nombre, porcentaje, color)
    }

    return result
}
