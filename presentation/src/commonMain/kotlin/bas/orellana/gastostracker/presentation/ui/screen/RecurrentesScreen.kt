package bas.orellana.gastostracker.presentation.ui.screen

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import bas.orellana.gastostracker.domain.model.Categoria
import bas.orellana.gastostracker.domain.model.Recurrente
import bas.orellana.gastostracker.domain.model.TipoMovimiento
import bas.orellana.gastostracker.presentation.resources.Res
import bas.orellana.gastostracker.presentation.resources.accion_cancelar
import bas.orellana.gastostracker.presentation.resources.accion_eliminar
import bas.orellana.gastostracker.presentation.resources.accion_guardar
import bas.orellana.gastostracker.presentation.resources.activo
import bas.orellana.gastostracker.presentation.resources.campo_categoria
import bas.orellana.gastostracker.presentation.resources.campo_concepto
import bas.orellana.gastostracker.presentation.resources.campo_dia_del_mes
import bas.orellana.gastostracker.presentation.resources.campo_monto
import bas.orellana.gastostracker.presentation.resources.dia_del_mes_n
import bas.orellana.gastostracker.presentation.resources.dia_invalido
import bas.orellana.gastostracker.presentation.resources.editar_fijo
import bas.orellana.gastostracker.presentation.resources.fijos_explicacion
import bas.orellana.gastostracker.presentation.resources.gastos_fijos
import bas.orellana.gastostracker.presentation.resources.importe_invalido
import bas.orellana.gastostracker.presentation.resources.ingresos_fijos
import bas.orellana.gastostracker.presentation.resources.nuevo_gasto_fijo
import bas.orellana.gastostracker.presentation.resources.nuevo_ingreso_fijo
import bas.orellana.gastostracker.presentation.resources.pausado
import bas.orellana.gastostracker.presentation.resources.sin_fijos
import bas.orellana.gastostracker.presentation.resources.sobrante_fijo
import bas.orellana.gastostracker.presentation.resources.titulo_recurrentes
import bas.orellana.gastostracker.presentation.resources.total_al_mes
import bas.orellana.gastostracker.presentation.ui.components.GradientTopAppBar
import bas.orellana.gastostracker.presentation.ui.components.SelectorCategoria
import bas.orellana.gastostracker.presentation.ui.components.SelectorCategoriaIngreso
import bas.orellana.gastostracker.presentation.ui.components.etiquetaCategoria
import bas.orellana.gastostracker.presentation.util.esEntradaImporteValida
import bas.orellana.gastostracker.presentation.util.formatMoney
import bas.orellana.gastostracker.presentation.util.simboloMoneda
import bas.orellana.gastostracker.presentation.viewmodel.EditorRecurrente
import bas.orellana.gastostracker.presentation.viewmodel.RecurrentesState
import bas.orellana.gastostracker.presentation.viewmodel.RecurrentesViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RecurrentesScreen(
    navController: NavController,
    viewModel: RecurrentesViewModel = koinViewModel()
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
                    emoji = "🔁",
                    title = stringResource(Res.string.titulo_recurrentes),
                    onBack = { navController.popBackStack() }
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { ResumenFijos(state) }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = { viewModel.nuevo(TipoMovimiento.GASTO) }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(Res.string.nuevo_gasto_fijo), fontSize = 13.sp)
                        }
                        OutlinedButton(onClick = { viewModel.nuevo(TipoMovimiento.INGRESO) }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(Res.string.nuevo_ingreso_fijo), fontSize = 13.sp)
                        }
                    }
                }
                if (state.recurrentes.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(Res.string.sin_fijos),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 24.dp)
                        )
                    }
                }
                if (state.gastosFijos.isNotEmpty()) {
                    item { TituloSeccion(stringResource(Res.string.gastos_fijos)) }
                    items(state.gastosFijos, key = { it.id }) { recurrente ->
                        FilaRecurrente(recurrente, state, onClick = { viewModel.editar(recurrente) }) {
                            viewModel.cambiarActivo(recurrente, it)
                        }
                    }
                }
                if (state.ingresosFijos.isNotEmpty()) {
                    item { TituloSeccion(stringResource(Res.string.ingresos_fijos)) }
                    items(state.ingresosFijos, key = { it.id }) { recurrente ->
                        FilaRecurrente(recurrente, state, onClick = { viewModel.editar(recurrente) }) {
                            viewModel.cambiarActivo(recurrente, it)
                        }
                    }
                }
                item {
                    Text(
                        text = stringResource(Res.string.fijos_explicacion),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            }
        }

        state.editor?.let { editor ->
            EditorRecurrenteDialog(
                editor = editor,
                state = state,
                onCambio = { viewModel.actualizarEditor(it) },
                onGuardar = { viewModel.guardar() },
                onEliminar = { viewModel.eliminarEditado() },
                onCancelar = { viewModel.cancelarEdicion() }
            )
        }
    }
}

@Composable
private fun TituloSeccion(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
private fun ResumenFijos(state: RecurrentesState) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = MaterialTheme.shapes.small
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(Res.string.total_al_mes, formatMoney(state.totalGastosFijos)),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(stringResource(Res.string.gastos_fijos), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(Res.string.total_al_mes, formatMoney(state.totalIngresosFijos)),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(stringResource(Res.string.ingresos_fijos), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (state.totalIngresosFijos > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(Res.string.sobrante_fijo, formatMoney(state.totalIngresosFijos - state.totalGastosFijos)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
private fun FilaRecurrente(
    recurrente: Recurrente,
    state: RecurrentesState,
    onClick: () -> Unit,
    onActivo: (Boolean) -> Unit
) {
    val etiqueta = if (recurrente.tipo == TipoMovimiento.GASTO) {
        etiquetaCategoria(recurrente.categoria, recurrente.categoriaPersonalizadaId, state.categoriasPersonalizadas)
    } else {
        etiquetaCategoria(recurrente.categoriaIngreso, recurrente.categoriaPersonalizadaId, state.categoriasPersonalizadas)
    }
    val color = etiqueta?.color ?: MaterialTheme.colorScheme.primary
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(recurrente.diaDelMes.toString(), fontWeight = FontWeight.Bold, color = color)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = recurrente.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (recurrente.activo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = listOfNotNull(
                        stringResource(Res.string.dia_del_mes_n, recurrente.diaDelMes),
                        etiqueta?.nombre,
                        if (!recurrente.activo) stringResource(Res.string.pausado) else null
                    ).joinToString(" · "),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatMoney(recurrente.monto),
                    fontWeight = FontWeight.Bold,
                    color = if (recurrente.tipo == TipoMovimiento.GASTO) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
                Switch(checked = recurrente.activo, onCheckedChange = onActivo)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditorRecurrenteDialog(
    editor: EditorRecurrente,
    state: RecurrentesState,
    onCambio: ((EditorRecurrente) -> EditorRecurrente) -> Unit,
    onGuardar: () -> Unit,
    onEliminar: () -> Unit,
    onCancelar: () -> Unit
) {
    val importeInvalido = editor.monto.isNotEmpty() && editor.importe == null
    val diaInvalido = editor.dia.isNotEmpty() && editor.diaValido == null
    val titulo = when {
        editor.original != null -> Res.string.editar_fijo
        editor.tipo == TipoMovimiento.GASTO -> Res.string.nuevo_gasto_fijo
        else -> Res.string.nuevo_ingreso_fijo
    }

    BasicAlertDialog(onDismissRequest = onCancelar) {
        Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(stringResource(titulo), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = editor.nombre,
                    onValueChange = { nuevo -> onCambio { it.copy(nombre = nuevo) } },
                    label = { Text(stringResource(Res.string.campo_concepto)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = editor.monto,
                    onValueChange = { nuevo -> if (esEntradaImporteValida(nuevo)) onCambio { it.copy(monto = nuevo) } },
                    label = { Text(stringResource(Res.string.campo_monto)) },
                    isError = importeInvalido,
                    supportingText = if (importeInvalido) {
                        { Text(stringResource(Res.string.importe_invalido)) }
                    } else null,
                    prefix = { Text(simboloMoneda()) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = editor.dia,
                    onValueChange = { nuevo -> if (nuevo.length <= 2 && nuevo.all { it.isDigit() }) onCambio { it.copy(dia = nuevo) } },
                    label = { Text(stringResource(Res.string.campo_dia_del_mes)) },
                    isError = diaInvalido,
                    supportingText = if (diaInvalido) {
                        { Text(stringResource(Res.string.dia_invalido)) }
                    } else null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = stringResource(Res.string.campo_categoria),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (editor.tipo == TipoMovimiento.GASTO) {
                    SelectorCategoria(
                        predefinidas = Categoria.entries,
                        categoriasPersonalizadas = state.categoriasPersonalizadas,
                        categoria = editor.categoria,
                        categoriaPersonalizadaId = editor.categoriaPersonalizadaId,
                        onElegir = { categoria, personalizada ->
                            onCambio { it.copy(categoria = categoria, categoriaPersonalizadaId = personalizada) }
                        }
                    )
                } else {
                    SelectorCategoriaIngreso(
                        categoriasPersonalizadas = state.categoriasPersonalizadas,
                        categoria = editor.categoriaIngreso,
                        categoriaPersonalizadaId = editor.categoriaPersonalizadaId,
                        onElegir = { categoria, personalizada ->
                            onCambio { it.copy(categoriaIngreso = categoria, categoriaPersonalizadaId = personalizada) }
                        }
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(Res.string.activo), modifier = Modifier.weight(1f))
                    Switch(checked = editor.activo, onCheckedChange = { nuevo -> onCambio { it.copy(activo = nuevo) } })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (editor.original != null) {
                        TextButton(onClick = onEliminar) {
                            Text(stringResource(Res.string.accion_eliminar), color = MaterialTheme.colorScheme.error)
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = onCancelar) { Text(stringResource(Res.string.accion_cancelar)) }
                    Button(onClick = onGuardar, enabled = editor.esValido) { Text(stringResource(Res.string.accion_guardar)) }
                }
            }
        }
    }
}
