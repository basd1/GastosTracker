package bas.orellana.gastostracker.presentation.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import bas.orellana.gastostracker.domain.model.SaldoCuenta
import bas.orellana.gastostracker.domain.model.TipoCuenta
import bas.orellana.gastostracker.presentation.resources.Res
import bas.orellana.gastostracker.presentation.resources.accion_cancelar
import bas.orellana.gastostracker.presentation.resources.accion_eliminar
import bas.orellana.gastostracker.presentation.resources.accion_guardar
import bas.orellana.gastostracker.presentation.resources.archivada
import bas.orellana.gastostracker.presentation.resources.campo_nombre
import bas.orellana.gastostracker.presentation.resources.campo_saldo_inicial
import bas.orellana.gastostracker.presentation.resources.campo_tipo_cuenta
import bas.orellana.gastostracker.presentation.resources.cuentas_explicacion
import bas.orellana.gastostracker.presentation.resources.editar_cuenta
import bas.orellana.gastostracker.presentation.resources.eliminar_cuenta_mensaje
import bas.orellana.gastostracker.presentation.resources.eliminar_cuenta_titulo
import bas.orellana.gastostracker.presentation.resources.nueva_cuenta
import bas.orellana.gastostracker.presentation.resources.saldo_inicial_invalido
import bas.orellana.gastostracker.presentation.resources.saldo_total
import bas.orellana.gastostracker.presentation.resources.selecciona_color
import bas.orellana.gastostracker.presentation.resources.sin_cuentas
import bas.orellana.gastostracker.presentation.resources.titulo_cuentas
import bas.orellana.gastostracker.presentation.ui.components.GradientTopAppBar
import bas.orellana.gastostracker.presentation.ui.components.icono
import bas.orellana.gastostracker.presentation.ui.components.nombre
import bas.orellana.gastostracker.presentation.util.formatMoney
import bas.orellana.gastostracker.presentation.util.simboloMoneda
import bas.orellana.gastostracker.presentation.viewmodel.CuentasViewModel
import bas.orellana.gastostracker.presentation.viewmodel.EditorCuenta
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CuentasScreen(
    navController: NavController,
    viewModel: CuentasViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                GradientTopAppBar(
                    emoji = "🏦",
                    title = stringResource(Res.string.titulo_cuentas),
                    onBack = { navController.popBackStack() }
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.nueva() },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(Res.string.nueva_cuenta))
                }
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(stringResource(Res.string.saldo_total), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = formatMoney(state.saldoTotal),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (state.saldoTotal >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                if (state.saldos.isEmpty()) {
                    item {
                        Text(
                            stringResource(Res.string.sin_cuentas),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 24.dp)
                        )
                    }
                }
                items(state.saldos, key = { it.cuenta.id }) { saldo ->
                    FilaCuenta(saldo, onClick = { viewModel.editar(saldo.cuenta) })
                }
                item {
                    Text(
                        stringResource(Res.string.cuentas_explicacion),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
                item { Spacer(modifier = Modifier.height(72.dp)) }
            }
        }

        state.editor?.let { editor ->
            EditorCuentaDialog(
                editor = editor,
                onCambio = { viewModel.actualizarEditor(it) },
                onGuardar = { viewModel.guardar() },
                onEliminar = { viewModel.pedirEliminar() },
                onCancelar = { viewModel.cancelarEdicion() }
            )
        }
        state.cuentaAEliminar?.let { cuenta ->
            AlertDialog(
                onDismissRequest = { viewModel.cancelarEliminar() },
                title = { Text(stringResource(Res.string.eliminar_cuenta_titulo, cuenta.nombre)) },
                text = { Text(stringResource(Res.string.eliminar_cuenta_mensaje)) },
                confirmButton = {
                    TextButton(onClick = { viewModel.confirmarEliminar() }) {
                        Text(stringResource(Res.string.accion_eliminar), color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.cancelarEliminar() }) { Text(stringResource(Res.string.accion_cancelar)) }
                }
            )
        }
    }
}

@Composable
private fun FilaCuenta(saldo: SaldoCuenta, onClick: () -> Unit) {
    val cuenta = saldo.cuenta
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(cuenta.color).copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(cuenta.tipo.icono, contentDescription = null, tint = Color(cuenta.color))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(cuenta.nombre, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = listOfNotNull(cuenta.tipo.nombre(), if (cuenta.archivada) stringResource(Res.string.archivada) else null)
                        .joinToString(" · "),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = formatMoney(saldo.saldo),
                fontWeight = FontWeight.Bold,
                color = if (saldo.saldo >= 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun EditorCuentaDialog(
    editor: EditorCuenta,
    onCambio: ((EditorCuenta) -> EditorCuenta) -> Unit,
    onGuardar: () -> Unit,
    onEliminar: () -> Unit,
    onCancelar: () -> Unit
) {
    val saldoInvalido = editor.importeInicial == null
    BasicAlertDialog(onDismissRequest = onCancelar) {
        Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    stringResource(if (editor.original == null) Res.string.nueva_cuenta else Res.string.editar_cuenta),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                OutlinedTextField(
                    value = editor.nombre,
                    onValueChange = { nuevo -> onCambio { it.copy(nombre = nuevo) } },
                    label = { Text(stringResource(Res.string.campo_nombre)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth()
                )
                Text(stringResource(Res.string.campo_tipo_cuenta), style = MaterialTheme.typography.labelMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    TipoCuenta.entries.forEach { tipo ->
                        FilterChip(
                            selected = editor.tipo == tipo,
                            onClick = { onCambio { it.copy(tipo = tipo) } },
                            label = { Text(tipo.nombre()) },
                            leadingIcon = { Icon(tipo.icono, contentDescription = null) }
                        )
                    }
                }
                OutlinedTextField(
                    value = editor.saldoInicial,
                    onValueChange = { nuevo ->
                        if (nuevo.matches(Regex("^-?\\d*[,.]?\\d{0,2}$"))) onCambio { it.copy(saldoInicial = nuevo) }
                    },
                    label = { Text(stringResource(Res.string.campo_saldo_inicial)) },
                    isError = saldoInvalido,
                    supportingText = if (saldoInvalido) {
                        { Text(stringResource(Res.string.saldo_inicial_invalido)) }
                    } else null,
                    prefix = { Text(simboloMoneda()) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    modifier = Modifier.fillMaxWidth()
                )
                Text(stringResource(Res.string.selecciona_color), style = MaterialTheme.typography.labelMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Categoria.PALETA_COLORES_PERSONALIZADOS.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(color))
                                .then(if (color == editor.color) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape) else Modifier)
                                .clickable { onCambio { it.copy(color = color) } }
                        )
                    }
                }
                if (editor.original != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(Res.string.archivada), modifier = Modifier.weight(1f))
                        Switch(checked = editor.archivada, onCheckedChange = { nuevo -> onCambio { it.copy(archivada = nuevo) } })
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
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
