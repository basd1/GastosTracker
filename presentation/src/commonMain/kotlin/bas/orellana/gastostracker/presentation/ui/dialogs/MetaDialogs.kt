package bas.orellana.gastostracker.presentation.ui.dialogs

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import bas.orellana.gastostracker.domain.model.Categoria
import bas.orellana.gastostracker.presentation.resources.Res
import bas.orellana.gastostracker.presentation.resources.accion_cancelar
import bas.orellana.gastostracker.presentation.resources.accion_guardar
import bas.orellana.gastostracker.presentation.resources.aportar_a
import bas.orellana.gastostracker.presentation.resources.campo_fecha
import bas.orellana.gastostracker.presentation.resources.campo_fecha_objetivo
import bas.orellana.gastostracker.presentation.resources.campo_monto
import bas.orellana.gastostracker.presentation.resources.campo_nombre
import bas.orellana.gastostracker.presentation.resources.campo_nota_opcional
import bas.orellana.gastostracker.presentation.resources.campo_objetivo_opcional
import bas.orellana.gastostracker.presentation.resources.editar_meta
import bas.orellana.gastostracker.presentation.resources.importe_invalido
import bas.orellana.gastostracker.presentation.resources.nueva_meta
import bas.orellana.gastostracker.presentation.resources.retirar_de
import bas.orellana.gastostracker.presentation.resources.selecciona_color
import bas.orellana.gastostracker.presentation.ui.components.CampoFecha
import bas.orellana.gastostracker.presentation.util.esEntradaImporteValida
import bas.orellana.gastostracker.presentation.util.simboloMoneda
import bas.orellana.gastostracker.presentation.viewmodel.EditorAportacion
import bas.orellana.gastostracker.presentation.viewmodel.EditorMeta
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditorMetaDialog(
    editor: EditorMeta,
    onCambio: ((EditorMeta) -> EditorMeta) -> Unit,
    onGuardar: () -> Unit,
    onCancelar: () -> Unit
) {
    BasicAlertDialog(onDismissRequest = onCancelar) {
        Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(if (editor.original == null) Res.string.nueva_meta else Res.string.editar_meta),
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
                OutlinedTextField(
                    value = editor.objetivo,
                    onValueChange = { nuevo -> if (esEntradaImporteValida(nuevo)) onCambio { it.copy(objetivo = nuevo) } },
                    label = { Text(stringResource(Res.string.campo_objetivo_opcional)) },
                    isError = editor.objetivoInvalido,
                    supportingText = if (editor.objetivoInvalido) {
                        { Text(stringResource(Res.string.importe_invalido)) }
                    } else null,
                    prefix = { Text(simboloMoneda()) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                CampoFecha(
                    etiqueta = stringResource(Res.string.campo_fecha_objetivo),
                    fecha = editor.fechaObjetivo,
                    onCambio = { fecha -> onCambio { it.copy(fechaObjetivo = fecha) } },
                    onQuitar = { onCambio { it.copy(fechaObjetivo = null) } }
                )
                Text(
                    text = stringResource(Res.string.selecciona_color),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Categoria.PALETA_COLORES_PERSONALIZADOS.forEach { color ->
                        val elegido = color == editor.color
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(color))
                                .then(
                                    if (elegido) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                    else Modifier
                                )
                                .clickable { onCambio { it.copy(color = color) } }
                        )
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onCancelar) { Text(stringResource(Res.string.accion_cancelar)) }
                    Spacer(modifier = Modifier.size(8.dp))
                    Button(onClick = onGuardar, enabled = editor.esValido) { Text(stringResource(Res.string.accion_guardar)) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorAportacionDialog(
    editor: EditorAportacion,
    onCambio: ((EditorAportacion) -> EditorAportacion) -> Unit,
    onGuardar: () -> Unit,
    onCancelar: () -> Unit
) {
    val importeInvalido = editor.monto.isNotEmpty() && editor.importe == null
    BasicAlertDialog(onDismissRequest = onCancelar) {
        Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(
                        if (editor.esRetirada) Res.string.retirar_de else Res.string.aportar_a,
                        editor.meta.nombre
                    ),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
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
                CampoFecha(
                    etiqueta = stringResource(Res.string.campo_fecha),
                    fecha = editor.fecha,
                    onCambio = { fecha -> onCambio { it.copy(fecha = fecha) } }
                )
                OutlinedTextField(
                    value = editor.nota,
                    onValueChange = { nueva -> onCambio { it.copy(nota = nueva) } },
                    label = { Text(stringResource(Res.string.campo_nota_opcional)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onCancelar) { Text(stringResource(Res.string.accion_cancelar)) }
                    Spacer(modifier = Modifier.size(8.dp))
                    Button(onClick = onGuardar, enabled = editor.importe != null) { Text(stringResource(Res.string.accion_guardar)) }
                }
            }
        }
    }
}
