package bas.orellana.gastostracker.presentation.ui.screen

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween

import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.navigation.NavController
import bas.orellana.gastostracker.domain.model.GastoModel
import bas.orellana.gastostracker.presentation.state.HomeState
import bas.orellana.gastostracker.presentation.ui.components.BottomNavBar
import bas.orellana.gastostracker.presentation.ui.dialogs.AddGastoDialog
import bas.orellana.gastostracker.presentation.ui.dialogs.ManageCategoriasDialog
import bas.orellana.gastostracker.presentation.ui.dialogs.SettingsDialog
import bas.orellana.gastostracker.presentation.viewmodel.HomeViewModel
import bas.orellana.gastostracker.presentation.viewmodel.SettingsViewModel
import bas.orellana.gastostracker.domain.model.CategoriaPersonalizada
import org.koin.androidx.compose.koinViewModel
import java.time.format.DateTimeFormatter
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState

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
                    Text("Añadir gasto", style = TextStyle(fontSize = 18.sp))
                }
            }
        }
    ) { paddingValues ->
        GastosList(
            gastos = state.gastos,
            isLoading = state.isLoading,
            categoriasPersonalizadas = categoriasPersonalizadas,
            isDarkTheme = isDarkTheme,
            onDelete = { viewModel.deleteGasto(it) },
            modifier = Modifier.padding(paddingValues)
        )

        if (state.showAddGastoDialog) {
            AddGastoDialog(
                concepto = addGastoState.concepto,
                precio = addGastoState.precio,
                categoriaSeleccionada = addGastoState.categoriaSeleccionada,
                categoriaPersonalizadaSeleccionada = addGastoState.categoriaPersonalizadaSeleccionada,
                categoriasPersonalizadas = categoriasPersonalizadas,
                onConceptoChange = { viewModel.updateAddGastoConcepto(it) },
                onPrecioChange = { viewModel.updateAddGastoPrecio(it) },
                onCategoriaChange = { viewModel.updateAddGastoCategoria(it) },
                onCategoriaPersonalizadaChange = { viewModel.updateAddGastoCategoriaPersonalizada(it) },
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
}

@Composable
private fun GastosList(
    gastos: List<GastoModel>,
    isLoading: Boolean,
    categoriasPersonalizadas: List<CategoriaPersonalizada>,
    isDarkTheme: Boolean,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
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
            Text(
                text = "No hay gastos todavía",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            items(gastos, key = { it.id }) { gasto ->
                GastoItem(
                    gasto = gasto,
                    categoriasPersonalizadas = categoriasPersonalizadas,
                    isDarkTheme = isDarkTheme,
                    onDelete = onDelete
                )
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GastoItem(
    gasto: GastoModel,
    categoriasPersonalizadas: List<CategoriaPersonalizada>,
    isDarkTheme: Boolean,
    onDelete: (String) -> Unit
) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

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

    val colorFondo = when {
        gasto.categoria != null -> Color(gasto.categoria!!.color)
        gasto.categoriaPersonalizadaId != null -> categoriasPersonalizadas.find { it.id == gasto.categoriaPersonalizadaId }?.let { Color(it.color) } ?: Color(0xFF607D8B)
        else -> Color(0xFF607D8B)
    }

    val animatedColors = rememberInfiniteTransition(label = "itemGradient")
    val progress by animatedColors.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "itemGradientFloat"
    )

    val lighterColor = Color(
        red = (colorFondo.red + 0.3f).coerceAtMost(1f),
        green = (colorFondo.green + 0.3f).coerceAtMost(1f),
        blue = (colorFondo.blue + 0.3f).coerceAtMost(1f),
        alpha = colorFondo.alpha
    )
    val darkerColor = Color(
        red = (colorFondo.red - 0.2f).coerceAtLeast(0f),
        green = (colorFondo.green - 0.2f).coerceAtLeast(0f),
        blue = (colorFondo.blue - 0.2f).coerceAtLeast(0f),
        alpha = colorFondo.alpha
    )
    val gradientColor1 = interpolateColor(colorFrom = lighterColor, colorTo = colorFondo, fraction = progress)
    val gradientColor2 = interpolateColor(colorFrom = colorFondo, colorTo = darkerColor, fraction = progress)

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
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(gradientColor1, gradientColor2)
                        )
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = gasto.nombre,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkTheme) Color.White else Color.Black
                        )
                        Text(
                            text = gasto.fecha.format(dateFormatter),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isDarkTheme) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.7f)
                        )

                        val nombreCategoria = when {
                            gasto.categoria != null -> gasto.categoria?.displayName
                            gasto.categoriaPersonalizadaId != null -> categoriasPersonalizadas.find { it.id == gasto.categoriaPersonalizadaId }?.nombre
                            else -> null
                        }

                        if (nombreCategoria != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            AssistChip(
                                onClick = { },
                                label = {
                                    Text(
                                        text = nombreCategoria,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                },
                                modifier = Modifier.height(28.dp),
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (isDarkTheme) Color.White.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.1f),
                                    labelColor = if (isDarkTheme) Color.White else Color.Black
                                ),
                                border = AssistChipDefaults.assistChipBorder(
                                    borderColor = if (isDarkTheme) Color.White.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.3f),
                                    enabled = true
                                )
                            )
                        }
                    }
                    Text(
                        text = "€${String.format("%.2f", gasto.monto)}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkTheme) Color.White else Color.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun GradientTopAppBar(
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
                        text = "💰",
                        style = TextStyle(fontSize = 28.sp)
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                    Text(
                        text = "Gastos",
                        style = TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Configuración",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

private fun interpolateColor(colorFrom: Color, colorTo: Color, fraction: Float): Color {
    return Color(
        red = colorFrom.red + (colorTo.red - colorFrom.red) * fraction,
        green = colorFrom.green + (colorTo.green - colorFrom.green) * fraction,
        blue = colorFrom.blue + (colorTo.blue - colorFrom.blue) * fraction,
        alpha = colorFrom.alpha + (colorTo.alpha - colorFrom.alpha) * fraction
    )
}

