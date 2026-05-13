package bas.orellana.gastostracker.presentation.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import bas.orellana.gastostracker.domain.model.CategoriaPersonalizada
import bas.orellana.gastostracker.domain.model.GastoModel
import bas.orellana.gastostracker.presentation.ui.components.BottomNavBar
import bas.orellana.gastostracker.presentation.viewmodel.HomeViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun GraphScreen(
    navController: NavController,
    viewModel: HomeViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val categoriasPersonalizadas by viewModel.categoriasPersonalizadas.collectAsState()

    val animatedColors = rememberInfiniteTransition(label = "gradient")
    val progress by animatedColors.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradientFloat"
    )

    val backgroundColor1 = interpolateColor(
        colorFrom = Color(0xFF81C784),
        colorTo = Color(0xFF64B5F6),
        fraction = progress
    )
    val backgroundColor2 = interpolateColor(
        colorFrom = Color(0xFF4DB6AC),
        colorTo = Color(0xFFAED581),
        fraction = progress
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        backgroundColor1.copy(alpha = 0.4f),
                        backgroundColor2.copy(alpha = 0.3f)
                    )
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                GraphTopAppBar(onSettingsClick = {})
            },
            bottomBar = { BottomNavBar(navController = navController) }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Distribución de Gastos",
                    style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (state.gastos.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay gastos",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 16.sp
                        )
                    }
                } else {
                    val totalGastos = state.gastos.sumOf { it.monto }
                    val categoriaData = calcularPorcentajesPorCategoria(
                        state.gastos,
                        categoriasPersonalizadas
                    )

                    Box(
                        modifier = Modifier.size(280.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(
                            modifier = Modifier.size(260.dp)
                        ) {
                            var startAngle = -90f
                            categoriaData.forEach { (_, data) ->
                                val sweepAngle = (data.porcentaje / 100f) * 360f
                                drawArc(
                                    color = data.color,
                                    startAngle = startAngle,
                                    sweepAngle = sweepAngle,
                                    useCenter = true
                                )
                                startAngle += sweepAngle
                            }
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Total",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "€${String.format("%.2f", totalGastos)}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    LazyVerticalGrid(
                        columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(2),
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categoriaData.values.sortedByDescending { it.porcentaje }) { data ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White.copy(alpha = 0.5f))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(data.color)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = data.nombre,
                                    color = Color(0xFF1B5E20),
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "${String.format("%.1f", data.porcentaje)}%",
                                    color = Color(0xFF1B5E20),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
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

private fun interpolateColor(colorFrom: Color, colorTo: Color, fraction: Float): Color {
    return Color(
        red = colorFrom.red + (colorTo.red - colorFrom.red) * fraction,
        green = colorFrom.green + (colorTo.green - colorFrom.green) * fraction,
        blue = colorFrom.blue + (colorTo.blue - colorFrom.blue) * fraction,
        alpha = colorFrom.alpha + (colorTo.alpha - colorFrom.alpha) * fraction
    )
}

@Composable
private fun GraphTopAppBar(
    onSettingsClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF1B5E20),
                        Color(0xFF2E7D32),
                        Color(0xFF4CAF50)
                    )
                )
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.15f),
                            Color.Transparent
                        ),
                        center = Offset(0f, 0f),
                        radius = 200f
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, start = 20.dp, end = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📊",
                        style = TextStyle(fontSize = 28.sp)
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                    Text(
                        text = "Gráfico",
                        style = TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Settings,
                        contentDescription = "Configuración",
                        tint = Color.White
                    )
                }
            }
        }
    }
}