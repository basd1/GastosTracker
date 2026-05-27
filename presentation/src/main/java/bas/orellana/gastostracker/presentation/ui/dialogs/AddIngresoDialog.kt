package bas.orellana.gastostracker.presentation.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bas.orellana.gastostracker.domain.model.Categoria
import bas.orellana.gastostracker.domain.model.CategoriaPersonalizada
import bas.orellana.gastostracker.domain.model.IngresoModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddIngresoDialog(
    concepto: String,
    monto: String,
    categoriaSeleccionada: Categoria?,
    categoriaPersonalizadaId: String?,
    categoriasPersonalizadas: List<CategoriaPersonalizada>,
    ingresoToEdit: IngresoModel?,
    onConceptoChange: (String) -> Unit,
    onMontoChange: (String) -> Unit,
    onCategoriaChange: (Categoria?) -> Unit,
    onCategoriaPersonalizadaChange: (String?) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
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
                    text = if (ingresoToEdit != null) "Editar ingreso" else "Añadir ingreso",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = concepto,
                    onValueChange = onConceptoChange,
                    label = { Text("Concepto") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = monto,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*[,.]?\\d*$"))) {
                            onMontoChange(newValue)
                        }
                    },
                    label = { Text("Monto") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        capitalization = KeyboardCapitalization.Sentences
                    ),
                    prefix = { Text("€") }
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Categoría",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                FilterChip(
                    selected = categoriaSeleccionada == null && categoriaPersonalizadaId == null,
                    onClick = {
                        onCategoriaChange(null)
                        onCategoriaPersonalizadaChange(null)
                    },
                    label = { Text("Sin categoría", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Categoria.entries.filter { it != Categoria.AHORRO }.forEach { cat ->
                        FilterChip(
                            selected = categoriaSeleccionada == cat && categoriaPersonalizadaId == null,
                            onClick = {
                                onCategoriaChange(cat)
                                onCategoriaPersonalizadaChange(null)
                            },
                            label = { Text(cat.displayName, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(cat.color).copy(alpha = 0.4f),
                                containerColor = Color(cat.color).copy(alpha = 0.15f)
                            )
                        )
                    }
                }
                if (categoriasPersonalizadas.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categoriasPersonalizadas.forEach { cat ->
                            FilterChip(
                                selected = categoriaPersonalizadaId == cat.id,
                                onClick = {
                                    onCategoriaPersonalizadaChange(cat.id)
                                    onCategoriaChange(null)
                                },
                                label = { Text(cat.nombre, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(cat.color).copy(alpha = 0.4f),
                                    containerColor = Color(cat.color).copy(alpha = 0.15f)
                                )
                            )
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
                            text = "Cancelar",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Button(
                        onClick = onSave,
                        enabled = concepto.isNotBlank() && monto.isNotBlank(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (ingresoToEdit != null) "Actualizar" else "Guardar")
                    }
                }
            }
        }
    }
}
