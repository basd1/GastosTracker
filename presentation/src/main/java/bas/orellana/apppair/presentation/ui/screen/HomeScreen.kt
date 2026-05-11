package bas.orellana.gastostracker.presentation.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.AssistChip
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import bas.orellana.gastostracker.domain.model.Categoria
import bas.orellana.gastostracker.domain.model.CategoriaPersonalizada
import bas.orellana.gastostracker.domain.model.GastoModel
import bas.orellana.gastostracker.presentation.state.HomeState
import bas.orellana.gastostracker.presentation.ui.components.BottomNavBar
import bas.orellana.gastostracker.presentation.viewmodel.HomeViewModel
import bas.orellana.gastostracker.presentation.viewmodel.SettingsViewModel
import org.koin.androidx.compose.koinViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = koinViewModel(),
    settingsViewModel: SettingsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val isDarkTheme by settingsViewModel.isDarkTheme.collectAsState()
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showManageCategoriasDialog by remember { mutableStateOf(false) }
    var concepto by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var categoriaSeleccionada by remember { mutableStateOf<Categoria?>(null) }
    var categoriaPersonalizadaSeleccionada by remember { mutableStateOf<String?>(null) }
    var nuevaCategoria by remember { mutableStateOf("") }
    var colorSeleccionado by remember { mutableStateOf(Categoria.PALETA_COLORES_PERSONALIZADOS.first()) }
    val categoriasPersonalizadas by viewModel.categoriasPersonalizadas.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gastos") },
                actions = {
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Configuración"
                        )
                    }
                }
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
    ) { padding ->
        HomeContent(
            state = state,
            categoriasPersonalizadas = categoriasPersonalizadas,
            modifier = Modifier.padding(padding)
        )
    }

    if (showSettingsDialog) {
        SettingsDialog(
            isDarkTheme = isDarkTheme,
            onToggleDarkTheme = { settingsViewModel.toggleDarkTheme() },
            onManageCategorias = {
                showSettingsDialog = false
                showManageCategoriasDialog = true
            },
            onDismiss = { showSettingsDialog = false }
        )
    }

    if (showManageCategoriasDialog) {
        ManageCategoriasDialog(
            categoriasPersonalizadas = categoriasPersonalizadas,
            colorSeleccionado = colorSeleccionado,
            nuevaCategoria = nuevaCategoria,
            onColorSeleccionado = { colorSeleccionado = it },
            onNuevaCategoriaChange = { nuevaCategoria = it },
            onAddCategoria = {
                viewModel.addCategoriaPersonalizada(nuevaCategoria, colorSeleccionado)
                nuevaCategoria = ""
                colorSeleccionado = Categoria.PALETA_COLORES_PERSONALIZADOS.first()
            },
            onDeleteCategoria = { viewModel.deleteCategoriaPersonalizada(it) },
            onDismiss = {
                showManageCategoriasDialog = false
                nuevaCategoria = ""
                colorSeleccionado = Categoria.PALETA_COLORES_PERSONALIZADOS.first()
            }
        )
    }

    if (state.showAddGastoDialog) {
        AddGastoDialog(
            concepto = concepto,
            precio = precio,
            categoriaSeleccionada = categoriaSeleccionada,
            categoriaPersonalizadaSeleccionada = categoriaPersonalizadaSeleccionada,
            categoriasPersonalizadas = categoriasPersonalizadas,
            onConceptoChange = { concepto = it },
            onPrecioChange = { precio = it },
            onCategoriaChange = { categoriaSeleccionada = it },
            onCategoriaPersonalizadaChange = { categoriaPersonalizadaSeleccionada = it },
            onSave = {
                viewModel.saveGasto(concepto, precio, categoriaSeleccionada, categoriaPersonalizadaSeleccionada)
                concepto = ""
                precio = ""
                categoriaSeleccionada = null
                categoriaPersonalizadaSeleccionada = null
            },
            onDismiss = {
                viewModel.hideAddGastoDialog()
                concepto = ""
                precio = ""
                categoriaSeleccionada = null
                categoriaPersonalizadaSeleccionada = null
            }
        )
    }
}

@Composable
private fun HomeContent(
    state: HomeState,
    categoriasPersonalizadas: List<CategoriaPersonalizada>,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        when {
            state.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            state.error != null -> {
                Text(
                    text = state.error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            state.gastos.isEmpty() -> {
                Text(
                    text = "No hay gastos registrados",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            else -> {
                GastosList(gastos = state.gastos, categoriasPersonalizadas = categoriasPersonalizadas)
            }
        }
    }
}

@Composable
private fun GastosList(
    gastos: List<GastoModel>,
    categoriasPersonalizadas: List<CategoriaPersonalizada>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }
        items(gastos, key = { it.id }) { gasto ->
            GastoItem(
                gasto = gasto,
                categoriasPersonalizadas = categoriasPersonalizadas
            )
        }
        item { Spacer(modifier = Modifier.height(8.dp)) }
    }
}

@Composable
private fun GastoItem(
    gasto: GastoModel,
    categoriasPersonalizadas: List<CategoriaPersonalizada>
) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    val colorFondo = when {
        gasto.categoria != null -> Color(gasto.categoria!!.color).copy(alpha = 0.15f)
        gasto.categoriaPersonalizadaId != null -> categoriasPersonalizadas.find { it.id == gasto.categoriaPersonalizadaId }?.let { Color(it.color).copy(alpha = 0.15f) } ?: Color.Transparent
        else -> Color.Transparent
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colorFondo),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = gasto.fecha.format(dateFormatter),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                val nombreCategoria = when {
                    gasto.categoria != null -> gasto.categoria?.displayName
                    gasto.categoriaPersonalizadaId != null -> categoriasPersonalizadas.find { it.id == gasto.categoriaPersonalizadaId }?.nombre
                    else -> null
                }

                if (nombreCategoria != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    AssistChip(
                        onClick = { },
                        label = {
                            Text(
                                text = nombreCategoria,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        modifier = Modifier.height(24.dp)
                    )
                }
            }
            Text(
                text = "€${String.format("%.2f", gasto.monto)}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsDialog(
    isDarkTheme: Boolean,
    onToggleDarkTheme: () -> Unit,
    onManageCategorias: () -> Unit,
    onDismiss: () -> Unit
) {
    BasicAlertDialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Configuración",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Modo oscuro",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = { onToggleDarkTheme() }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onManageCategorias,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Administrar categorías")
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Cerrar")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddGastoDialog(
    concepto: String,
    precio: String,
    categoriaSeleccionada: Categoria?,
    categoriaPersonalizadaSeleccionada: String?,
    categoriasPersonalizadas: List<CategoriaPersonalizada>,
    onConceptoChange: (String) -> Unit,
    onPrecioChange: (String) -> Unit,
    onCategoriaChange: (Categoria?) -> Unit,
    onCategoriaPersonalizadaChange: (String?) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    BasicAlertDialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Añadir gasto",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = concepto,
                    onValueChange = onConceptoChange,
                    label = { Text("Concepto") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = precio,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                            onPrecioChange(newValue)
                        }
                    },
                    label = { Text("Precio") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                    prefix = { Text("€") }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Categoría",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = categoriaSeleccionada == null && categoriaPersonalizadaSeleccionada == null,
                            onClick = {
                                onCategoriaChange(null)
                                onCategoriaPersonalizadaChange(null)
                            },
                            label = { Text("Sin categoría") }
                        )
                    }

                    Categoria.entries.chunked(5).forEach { chunk ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            chunk.forEach { categoria ->
                                FilterChip(
                                    selected = categoriaSeleccionada == categoria,
                                    onClick = {
                                        onCategoriaChange(categoria)
                                        onCategoriaPersonalizadaChange(null)
                                    },
                                    label = { Text(categoria.displayName) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(categoria.color),
                                        containerColor = Color(categoria.color).copy(alpha = 0.3f)
                                    )
                                )
                            }
                        }
                    }

                    if (categoriasPersonalizadas.isNotEmpty()) {
                        Text(
                            text = "Personalizadas",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    categoriasPersonalizadas.chunked(5).forEach { chunk ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            chunk.forEach { categoriaPersonalizada ->
                                FilterChip(
                                    selected = categoriaPersonalizadaSeleccionada == categoriaPersonalizada.id,
                                    onClick = {
                                        onCategoriaChange(null)
                                        onCategoriaPersonalizadaChange(categoriaPersonalizada.id)
                                    },
                                    label = { Text(categoriaPersonalizada.nombre) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(categoriaPersonalizada.color),
                                        containerColor = Color(categoriaPersonalizada.color).copy(alpha = 0.3f)
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = onSave,
                        enabled = concepto.isNotBlank() && precio.isNotBlank(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ManageCategoriasDialog(
    categoriasPersonalizadas: List<CategoriaPersonalizada>,
    colorSeleccionado: Long,
    nuevaCategoria: String,
    onColorSeleccionado: (Long) -> Unit,
    onNuevaCategoriaChange: (String) -> Unit,
    onAddCategoria: () -> Unit,
    onDeleteCategoria: (String) -> Unit,
    onDismiss: () -> Unit
) {
    BasicAlertDialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Administrar categorías",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (categoriasPersonalizadas.isNotEmpty()) {
                    Text(
                        text = "Categorías existentes",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier.height(150.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categoriasPersonalizadas) { categoria ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Color(categoria.color).copy(alpha = 0.2f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = categoria.nombre,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                IconButton(
                                    onClick = { onDeleteCategoria(categoria.id) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Eliminar",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                Text(
                    text = "Nueva categoría",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Selecciona un color",
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Categoria.PALETA_COLORES_PERSONALIZADOS.chunked(9).forEach { chunk ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            chunk.forEach { color ->
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Color(color))
                                        .border(
                                            width = if (color == colorSeleccionado) 3.dp else 0.dp,
                                            color = if (color == colorSeleccionado) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { onColorSeleccionado(color) }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = nuevaCategoria,
                        onValueChange = onNuevaCategoriaChange,
                        label = { Text("Nombre") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Button(
                        onClick = onAddCategoria,
                        enabled = nuevaCategoria.isNotBlank()
                    ) {
                        Text("+")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Cerrar")
                }
            }
        }
    }
}