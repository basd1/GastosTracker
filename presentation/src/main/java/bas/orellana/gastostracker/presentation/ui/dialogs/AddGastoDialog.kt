package bas.orellana.gastostracker.presentation.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import bas.orellana.gastostracker.domain.model.Categoria
import bas.orellana.gastostracker.domain.model.CategoriaPersonalizada
import bas.orellana.gastostracker.domain.model.GastoModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGastoDialog(
    concepto: String,
    precio: String,
    categoriaSeleccionada: Categoria?,
    categoriaPersonalizadaSeleccionada: String?,
    categoriasPersonalizadas: List<CategoriaPersonalizada>,
    gastoToEdit: GastoModel?,
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
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = if (gastoToEdit != null) "Editar gasto" else "Añadir gasto",
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
                    value = precio,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*[,.]?\\d*$"))) {
                            onPrecioChange(newValue)
                        }
                    },
                    label = { Text("Precio") },
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
                            label = { Text("Sin categoría") },
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
                                    label = { Text(categoria.displayName) },
                                    modifier = Modifier.clickable { onCategoriaChange(categoria) },
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
                                    modifier = Modifier.clickable {
                                        onCategoriaChange(null)
                                        onCategoriaPersonalizadaChange(categoriaPersonalizada.id)
                                    },
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
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Cancelar",
                            color = Color(0xFFE53935)
                        )
                    }
                    Button(
                        onClick = onSave,
                        enabled = concepto.isNotBlank() && precio.isNotBlank(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (gastoToEdit != null) "Actualizar" else "Guardar")
                    }
                }
            }
        }
    }
}