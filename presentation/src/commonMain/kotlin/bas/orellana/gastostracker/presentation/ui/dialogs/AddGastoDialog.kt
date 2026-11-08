package bas.orellana.gastostracker.presentation.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bas.orellana.gastostracker.domain.model.Categoria
import bas.orellana.gastostracker.domain.model.CategoriaPersonalizada
import bas.orellana.gastostracker.domain.model.GastoModel
import bas.orellana.gastostracker.presentation.util.esEntradaImporteValida
import bas.orellana.gastostracker.presentation.util.formatDDMMYYYY
import bas.orellana.gastostracker.presentation.util.parseImporte
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import bas.orellana.gastostracker.presentation.resources.Res
import bas.orellana.gastostracker.presentation.resources.accion_actualizar
import bas.orellana.gastostracker.presentation.resources.accion_cancelar
import bas.orellana.gastostracker.presentation.resources.accion_guardar
import bas.orellana.gastostracker.presentation.resources.accion_seleccionar
import bas.orellana.gastostracker.presentation.resources.anadir_gasto
import bas.orellana.gastostracker.presentation.resources.cambiar_fecha
import bas.orellana.gastostracker.presentation.resources.campo_categoria
import bas.orellana.gastostracker.presentation.resources.campo_concepto
import bas.orellana.gastostracker.presentation.resources.campo_fecha
import bas.orellana.gastostracker.presentation.resources.campo_precio
import bas.orellana.gastostracker.presentation.resources.categorias_personalizadas
import bas.orellana.gastostracker.presentation.resources.editar_gasto
import bas.orellana.gastostracker.presentation.resources.importe_invalido
import bas.orellana.gastostracker.presentation.resources.sin_categoria
import bas.orellana.gastostracker.presentation.ui.components.nombre
import bas.orellana.gastostracker.presentation.util.simboloMoneda
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGastoDialog(
    concepto: String,
    precio: String,
    categoriaSeleccionada: Categoria?,
    categoriaPersonalizadaSeleccionada: String?,
    categoriasPersonalizadas: List<CategoriaPersonalizada>,
    gastoToEdit: GastoModel?,
    fecha: LocalDate,
    onConceptoChange: (String) -> Unit,
    onPrecioChange: (String) -> Unit,
    onCategoriaChange: (Categoria?) -> Unit,
    onCategoriaPersonalizadaChange: (String?) -> Unit,
    onFechaChange: (LocalDate) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val importeValido = parseImporte(precio) != null
    val importeInvalido = precio.isNotEmpty() && !importeValido

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = stringResource(if (gastoToEdit != null) Res.string.editar_gasto else Res.string.anadir_gasto),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = concepto,
                    onValueChange = onConceptoChange,
                    label = { Text(stringResource(Res.string.campo_concepto)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = precio,
                    onValueChange = { newValue ->
                        if (esEntradaImporteValida(newValue)) {
                            onPrecioChange(newValue)
                        }
                    },
                    isError = importeInvalido,
                    supportingText = if (importeInvalido) {
                        { Text(stringResource(Res.string.importe_invalido)) }
                    } else null,
                    label = { Text(stringResource(Res.string.campo_precio)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        capitalization = KeyboardCapitalization.Sentences
                    ),
                    prefix = { Text(simboloMoneda()) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                DateDisplay(
                    fecha = fecha,
                    onClick = { showDatePicker = true }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(Res.string.campo_categoria),
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
                            label = { Text(stringResource(Res.string.sin_categoria)) },
                            modifier = Modifier.clickable {
                                onCategoriaChange(null)
                                onCategoriaPersonalizadaChange(null)
                            }
                        )
                    }

                    Categoria.entries.chunked(5).forEach { chunk ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            chunk.forEach { categoria ->
                                FilterChip(
                                    selected = categoriaSeleccionada == categoria,
                                    onClick = { onCategoriaChange(categoria) },
                                    label = { Text(categoria.nombre()) },
                                    modifier = Modifier.clickable { onCategoriaChange(categoria) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(categoria.color).copy(alpha = 0.4f),
                                        containerColor = Color(categoria.color).copy(alpha = 0.15f)
                                    )
                                )
                            }
                        }
                    }

                    if (categoriasPersonalizadas.isNotEmpty()) {
                        Text(
                            text = stringResource(Res.string.categorias_personalizadas),
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
                                    modifier = Modifier.clickable {
                                        onCategoriaChange(null)
                                        onCategoriaPersonalizadaChange(categoriaPersonalizada.id)
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(categoriaPersonalizada.color).copy(alpha = 0.4f),
                                        containerColor = Color(categoriaPersonalizada.color).copy(alpha = 0.15f)
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
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = stringResource(Res.string.accion_cancelar),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Button(
                        onClick = onSave,
                        enabled = concepto.isNotBlank() && importeValido,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(if (gastoToEdit != null) Res.string.accion_actualizar else Res.string.accion_guardar))
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = fecha.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        onFechaChange(
                            Instant.fromEpochMilliseconds(millis)
                                .toLocalDateTime(TimeZone.currentSystemDefault())
                                .date
                        )
                    }
                    showDatePicker = false
                }) {
                    Text(stringResource(Res.string.accion_seleccionar))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(Res.string.accion_cancelar), color = MaterialTheme.colorScheme.error)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun DateDisplay(
    fecha: LocalDate,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(Res.string.campo_fecha),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = fecha.formatDDMMYYYY(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = stringResource(Res.string.cambiar_fecha),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}