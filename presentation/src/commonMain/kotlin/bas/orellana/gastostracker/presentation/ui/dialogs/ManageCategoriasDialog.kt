package bas.orellana.gastostracker.presentation.ui.dialogs

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import bas.orellana.gastostracker.domain.model.Categoria
import bas.orellana.gastostracker.domain.model.CategoriaPersonalizada
import androidx.compose.material.icons.filled.Add
import bas.orellana.gastostracker.presentation.resources.Res
import bas.orellana.gastostracker.presentation.resources.accion_cerrar
import bas.orellana.gastostracker.presentation.resources.accion_eliminar
import bas.orellana.gastostracker.presentation.resources.accion_guardar
import bas.orellana.gastostracker.presentation.resources.administrar_categorias
import bas.orellana.gastostracker.presentation.resources.anadir_categoria
import bas.orellana.gastostracker.presentation.resources.campo_nombre
import bas.orellana.gastostracker.presentation.resources.cancelar_edicion
import bas.orellana.gastostracker.presentation.resources.categorias_existentes
import bas.orellana.gastostracker.presentation.resources.editar_categoria
import bas.orellana.gastostracker.presentation.resources.nueva_categoria
import bas.orellana.gastostracker.presentation.resources.selecciona_color
import org.jetbrains.compose.resources.stringResource
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.sp
import bas.orellana.gastostracker.domain.model.CategoriaDestino
import bas.orellana.gastostracker.presentation.resources.accion_cancelar
import bas.orellana.gastostracker.presentation.resources.eliminar_categoria_mensaje
import bas.orellana.gastostracker.presentation.resources.eliminar_categoria_titulo
import bas.orellana.gastostracker.presentation.ui.components.nombre
import bas.orellana.gastostracker.presentation.viewmodel.HomeViewModel
import org.jetbrains.compose.resources.pluralStringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ManageCategoriasDialog(
    categoriasPersonalizadas: List<CategoriaPersonalizada>,
    colorSeleccionado: Long,
    nuevaCategoria: String,
    editCategoria: CategoriaPersonalizada?,
    editNombre: String,
    editColor: Long,
    onColorSeleccionado: (Long) -> Unit,
    onNuevaCategoriaChange: (String) -> Unit,
    onAddCategoria: () -> Unit,
    onDeleteCategoria: (CategoriaPersonalizada) -> Unit,
    onEditCategoria: (CategoriaPersonalizada) -> Unit,
    onEditNombreChange: (String) -> Unit,
    onEditColorChange: (Long) -> Unit,
    onSaveEdit: () -> Unit,
    onCancelEdit: () -> Unit,
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
                    text = stringResource(Res.string.administrar_categorias),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (categoriasPersonalizadas.isNotEmpty()) {
                    Text(
                        text = stringResource(Res.string.categorias_existentes),
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
                                    .combinedClickable(
                                        onClick = {},
                                        onLongClick = { onEditCategoria(categoria) }
                                    )
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
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { onDeleteCategoria(categoria) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = stringResource(Res.string.accion_eliminar),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (editCategoria != null) {
                    Text(
                        text = stringResource(Res.string.editar_categoria),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(Res.string.selecciona_color),
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
                                                width = if (color == editColor) 3.dp else 0.dp,
                                                color = if (color == editColor) MaterialTheme.colorScheme.primary else Color.Transparent,
                                                shape = CircleShape
                                            )
                                            .clickable { onEditColorChange(color) }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = editNombre,
                            onValueChange = onEditNombreChange,
                            label = { Text(stringResource(Res.string.campo_nombre)) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                        )
                        Button(
                            onClick = onSaveEdit,
                            enabled = editNombre.isNotBlank()
                        ) {
                            Text(stringResource(Res.string.accion_guardar))
                        }
                    }

                    TextButton(
                        onClick = onCancelEdit
                    ) {
                        Text(stringResource(Res.string.cancelar_edicion), color = MaterialTheme.colorScheme.error)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                Text(
                    text = stringResource(Res.string.nueva_categoria),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(Res.string.selecciona_color),
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
                        label = { Text(stringResource(Res.string.campo_nombre)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                    )
                    Button(
                        onClick = onAddCategoria,
                        enabled = nuevaCategoria.isNotBlank()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(Res.string.anadir_categoria))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(stringResource(Res.string.accion_cerrar))
                }
            }
        }
    }
}

/** [ManageCategoriasDialog] conectado al [HomeViewModel], incluida la pregunta al borrar una categoría con movimientos. */
@Composable
fun ManageCategoriasDialog(viewModel: HomeViewModel) {
    val state by viewModel.manageCategoriasState.collectAsState()
    val categoriasPersonalizadas by viewModel.categoriasPersonalizadas.collectAsState()

    ManageCategoriasDialog(
        categoriasPersonalizadas = categoriasPersonalizadas,
        colorSeleccionado = state.colorSeleccionado,
        nuevaCategoria = state.nuevaCategoria,
        editCategoria = state.editCategoria,
        editNombre = state.editNombre,
        editColor = state.editColor,
        onColorSeleccionado = viewModel::updateColorSeleccionado,
        onNuevaCategoriaChange = viewModel::updateNuevaCategoria,
        onAddCategoria = {
            viewModel.addCategoriaPersonalizada(state.nuevaCategoria, state.colorSeleccionado)
            viewModel.resetManageCategoriasState()
        },
        onDeleteCategoria = viewModel::solicitarEliminarCategoria,
        onEditCategoria = viewModel::showEditCategoriaDialog,
        onEditNombreChange = viewModel::updateEditNombre,
        onEditColorChange = viewModel::updateEditColor,
        onSaveEdit = viewModel::saveEditCategoria,
        onCancelEdit = viewModel::cancelEditCategoria,
        onDismiss = viewModel::hideManageCategoriasDialog
    )

    state.categoriaAEliminar?.let { categoria ->
        ReasignarCategoriaDialog(
            categoria = categoria,
            movimientos = state.movimientosCategoriaAEliminar,
            destino = state.destinoReasignacion,
            otrasCategorias = categoriasPersonalizadas.filter { it.id != categoria.id },
            onDestinoChange = viewModel::updateDestinoReasignacion,
            onConfirmar = viewModel::confirmarEliminarCategoria,
            onCancelar = viewModel::cancelarEliminarCategoria
        )
    }
}

/**
 * Al borrar una categoría personalizada que tiene gastos o ingresos, pregunta a qué categoría pasarlos
 * (por defecto "Otros"). La de ahorro no se ofrece: convertiría gastos e ingresos en ahorro.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReasignarCategoriaDialog(
    categoria: CategoriaPersonalizada,
    movimientos: Int,
    destino: CategoriaDestino,
    otrasCategorias: List<CategoriaPersonalizada>,
    onDestinoChange: (CategoriaDestino) -> Unit,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(stringResource(Res.string.eliminar_categoria_titulo, categoria.nombre)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(pluralStringResource(Res.plurals.eliminar_categoria_mensaje, movimientos, movimientos))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Categoria.entries.forEach { predefinida ->
                        OpcionDestino(
                            texto = predefinida.nombre(),
                            color = predefinida.color,
                            seleccionada = destino == CategoriaDestino.Predefinida(predefinida),
                            onClick = { onDestinoChange(CategoriaDestino.Predefinida(predefinida)) }
                        )
                    }
                    otrasCategorias.forEach { personalizada ->
                        OpcionDestino(
                            texto = personalizada.nombre,
                            color = personalizada.color,
                            seleccionada = destino == CategoriaDestino.Personalizada(personalizada.id),
                            onClick = { onDestinoChange(CategoriaDestino.Personalizada(personalizada.id)) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirmar) {
                Text(stringResource(Res.string.accion_eliminar), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text(stringResource(Res.string.accion_cancelar))
            }
        }
    )
}

@Composable
private fun OpcionDestino(texto: String, color: Long, seleccionada: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = seleccionada,
        onClick = onClick,
        label = { Text(texto, fontSize = 12.sp) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Color(color).copy(alpha = 0.4f),
            containerColor = Color(color).copy(alpha = 0.15f)
        )
    )
}
