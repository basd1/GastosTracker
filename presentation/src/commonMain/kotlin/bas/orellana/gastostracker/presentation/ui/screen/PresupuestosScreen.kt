package bas.orellana.gastostracker.presentation.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import bas.orellana.gastostracker.domain.model.Mes
import bas.orellana.gastostracker.domain.model.Presupuesto
import bas.orellana.gastostracker.presentation.resources.Res
import bas.orellana.gastostracker.presentation.resources.accion_cancelar
import bas.orellana.gastostracker.presentation.resources.accion_eliminar
import bas.orellana.gastostracker.presentation.resources.accion_guardar
import bas.orellana.gastostracker.presentation.resources.anadir_presupuesto
import bas.orellana.gastostracker.presentation.resources.campo_categoria
import bas.orellana.gastostracker.presentation.resources.campo_limite_mensual
import bas.orellana.gastostracker.presentation.resources.categorias_personalizadas
import bas.orellana.gastostracker.presentation.resources.editar_presupuesto
import bas.orellana.gastostracker.presentation.resources.importe_invalido
import bas.orellana.gastostracker.presentation.resources.nuevo_presupuesto
import bas.orellana.gastostracker.presentation.resources.presupuesto_global
import bas.orellana.gastostracker.presentation.resources.sin_presupuestos_mensaje
import bas.orellana.gastostracker.presentation.resources.sin_presupuestos_titulo
import bas.orellana.gastostracker.presentation.resources.titulo_presupuestos
import bas.orellana.gastostracker.presentation.resources.todas_categorias_con_presupuesto
import bas.orellana.gastostracker.presentation.ui.components.FilaPresupuesto
import bas.orellana.gastostracker.presentation.ui.components.GradientTopAppBar
import bas.orellana.gastostracker.presentation.ui.components.SelectorMes
import bas.orellana.gastostracker.presentation.ui.components.nombre
import bas.orellana.gastostracker.presentation.ui.components.nombrePresupuesto
import bas.orellana.gastostracker.presentation.ui.dialogs.MonthYearPickerDialog
import bas.orellana.gastostracker.presentation.util.esEntradaImporteValida
import bas.orellana.gastostracker.presentation.util.simboloMoneda
import bas.orellana.gastostracker.presentation.util.subtituloPeriodo
import bas.orellana.gastostracker.presentation.viewmodel.EditorPresupuesto
import bas.orellana.gastostracker.presentation.viewmodel.PresupuestosState
import bas.orellana.gastostracker.presentation.viewmodel.PresupuestosViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PresupuestosScreen(
    navController: NavController,
    viewModel: PresupuestosViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                GradientTopAppBar(
                    emoji = "🎯",
                    title = stringResource(Res.string.titulo_presupuestos),
                    onBack = { navController.popBackStack() }
                )
            },
            floatingActionButton = {
                if (state.hayCategoriasDisponibles) {
                    ExtendedFloatingActionButton(
                        onClick = { viewModel.nuevoPresupuesto() },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(Res.string.anadir_presupuesto))
                    }
                }
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    SelectorMes(
                        mes = state.mes,
                        onAnterior = { viewModel.mesAnterior() },
                        onSiguiente = { viewModel.mesSiguiente() },
                        onElegir = { viewModel.showMonthPicker() },
                        subtitulo = subtituloPeriodo(state.mes, state.diaInicioMes),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                if (state.estados.isEmpty()) {
                    item { SinPresupuestos() }
                }
                items(state.estados, key = { it.presupuesto.clave }) { estado ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.editar(estado.presupuesto) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = MaterialTheme.shapes.small
                    ) {
                        FilaPresupuesto(
                            estado = estado,
                            categoriasPersonalizadas = state.categoriasPersonalizadas,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                if (!state.hayCategoriasDisponibles && state.estados.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(Res.string.todas_categorias_con_presupuesto),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                item { Spacer(modifier = Modifier.height(88.dp)) }
            }
        }

        state.editor?.let { editor ->
            EditorPresupuestoDialog(
                editor = editor,
                state = state,
                onCategoria = { categoria, personalizada -> viewModel.elegirCategoria(categoria, personalizada) },
                onMonto = { viewModel.cambiarMonto(it) },
                onGuardar = { viewModel.guardar() },
                onEliminar = { viewModel.eliminarEditado() },
                onCancelar = { viewModel.cancelarEdicion() }
            )
        }

        if (state.showMonthPicker) {
            MonthYearPickerDialog(
                currentMonth = state.mes.numero,
                currentYear = state.mes.anio,
                onConfirm = { month, year -> viewModel.seleccionarMes(Mes(year, month)) },
                onDismiss = { viewModel.hideMonthPicker() }
            )
        }
    }
}

@Composable
private fun SinPresupuestos() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Savings,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(Res.string.sin_presupuestos_titulo),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(Res.string.sin_presupuestos_mensaje),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun EditorPresupuestoDialog(
    editor: EditorPresupuesto,
    state: PresupuestosState,
    onCategoria: (bas.orellana.gastostracker.domain.model.Categoria?, String?) -> Unit,
    onMonto: (String) -> Unit,
    onGuardar: () -> Unit,
    onEliminar: () -> Unit,
    onCancelar: () -> Unit
) {
    val importeInvalido = editor.monto.isNotEmpty() && editor.importe == null

    BasicAlertDialog(onDismissRequest = onCancelar) {
        Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                Text(
                    text = stringResource(if (editor.esNuevo) Res.string.nuevo_presupuesto else Res.string.editar_presupuesto),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (editor.esNuevo) {
                    Text(
                        text = stringResource(Res.string.campo_categoria),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (state.globalDisponible) {
                            FilterChip(
                                selected = editor.clave == Presupuesto.CLAVE_GLOBAL,
                                onClick = { onCategoria(null, null) },
                                label = { Text(stringResource(Res.string.presupuesto_global)) }
                            )
                        }
                        state.categoriasDisponibles.forEach { categoria ->
                            FilterChip(
                                selected = editor.categoria == categoria,
                                onClick = { onCategoria(categoria, null) },
                                label = { Text(categoria.nombre()) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(categoria.color).copy(alpha = 0.4f),
                                    containerColor = Color(categoria.color).copy(alpha = 0.15f)
                                )
                            )
                        }
                    }
                    if (state.personalizadasDisponibles.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(Res.string.categorias_personalizadas),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            state.personalizadasDisponibles.forEach { categoria ->
                                FilterChip(
                                    selected = editor.categoriaPersonalizadaId == categoria.id,
                                    onClick = { onCategoria(null, categoria.id) },
                                    label = { Text(categoria.nombre) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(categoria.color).copy(alpha = 0.4f),
                                        containerColor = Color(categoria.color).copy(alpha = 0.15f)
                                    )
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                } else {
                    Text(
                        text = nombrePresupuesto(
                            Presupuesto(editor.categoria, editor.categoriaPersonalizadaId, 1.0),
                            state.categoriasPersonalizadas
                        ),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                OutlinedTextField(
                    value = editor.monto,
                    onValueChange = { if (esEntradaImporteValida(it)) onMonto(it) },
                    label = { Text(stringResource(Res.string.campo_limite_mensual)) },
                    isError = importeInvalido,
                    supportingText = if (importeInvalido) {
                        { Text(stringResource(Res.string.importe_invalido)) }
                    } else null,
                    prefix = { Text(simboloMoneda()) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!editor.esNuevo) {
                        TextButton(onClick = onEliminar) {
                            Text(stringResource(Res.string.accion_eliminar), color = MaterialTheme.colorScheme.error)
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = onCancelar) {
                        Text(stringResource(Res.string.accion_cancelar))
                    }
                    Button(onClick = onGuardar, enabled = editor.importe != null) {
                        Text(stringResource(Res.string.accion_guardar))
                    }
                }
            }
        }
    }
}
