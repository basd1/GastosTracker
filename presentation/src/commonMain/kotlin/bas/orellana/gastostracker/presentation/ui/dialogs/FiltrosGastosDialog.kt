package bas.orellana.gastostracker.presentation.ui.dialogs

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bas.orellana.gastostracker.domain.model.Categoria
import bas.orellana.gastostracker.domain.model.CategoriaPersonalizada
import bas.orellana.gastostracker.presentation.resources.Res
import bas.orellana.gastostracker.presentation.resources.accion_cancelar
import bas.orellana.gastostracker.presentation.resources.aplicar
import bas.orellana.gastostracker.presentation.resources.campo_categoria
import bas.orellana.gastostracker.presentation.resources.filtro_desde
import bas.orellana.gastostracker.presentation.resources.filtro_hasta
import bas.orellana.gastostracker.presentation.resources.filtro_importe
import bas.orellana.gastostracker.presentation.resources.filtro_importe_max
import bas.orellana.gastostracker.presentation.resources.filtro_importe_min
import bas.orellana.gastostracker.presentation.resources.filtro_rango_fechas
import bas.orellana.gastostracker.presentation.resources.filtro_rango_fechas_detalle
import bas.orellana.gastostracker.presentation.resources.filtros
import bas.orellana.gastostracker.presentation.resources.limpiar_filtros
import bas.orellana.gastostracker.presentation.state.FiltroGastos
import bas.orellana.gastostracker.presentation.ui.components.CampoFecha
import bas.orellana.gastostracker.presentation.ui.components.nombre
import bas.orellana.gastostracker.presentation.util.esEntradaImporteValida
import bas.orellana.gastostracker.presentation.util.simboloMoneda
import bas.orellana.gastostracker.presentation.util.toPlainStringTrimmed
import org.jetbrains.compose.resources.stringResource

/** Chips de categorías en una fila desplazable, para filtrar con un toque (varias a la vez). */
@Composable
fun FiltrosRapidos(
    filtro: FiltroGastos,
    categoriasPersonalizadas: List<CategoriaPersonalizada>,
    onAlternarCategoria: (Categoria) -> Unit,
    onAlternarPersonalizada: (String) -> Unit,
    onLimpiar: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
        ) {
            Categoria.entries.forEach { categoria ->
                FilterChip(
                    selected = categoria in filtro.categorias,
                    onClick = { onAlternarCategoria(categoria) },
                    label = { Text(categoria.nombre(), fontSize = 12.sp, maxLines = 1) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(categoria.color).copy(alpha = 0.4f),
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
            categoriasPersonalizadas.forEach { categoria ->
                FilterChip(
                    selected = categoria.id in filtro.personalizadas,
                    onClick = { onAlternarPersonalizada(categoria.id) },
                    label = { Text(categoria.nombre, fontSize = 12.sp, maxLines = 1) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(categoria.color).copy(alpha = 0.4f),
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
        }
        if (filtro.activo) {
            TextButton(onClick = onLimpiar) {
                Text(stringResource(Res.string.limpiar_filtros), fontSize = 12.sp)
            }
        }
    }
}

/** Todos los filtros: categorías (varias), importe mínimo y máximo, y un rango de fechas propio. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FiltrosGastosDialog(
    filtroActual: FiltroGastos,
    categoriasPersonalizadas: List<CategoriaPersonalizada>,
    onAplicar: (FiltroGastos) -> Unit,
    onCancelar: () -> Unit
) {
    var filtro by remember { mutableStateOf(filtroActual) }
    var minimo by remember { mutableStateOf(filtroActual.importeMin?.toPlainStringTrimmed()?.replace('.', ',').orEmpty()) }
    var maximo by remember { mutableStateOf(filtroActual.importeMax?.toPlainStringTrimmed()?.replace('.', ',').orEmpty()) }

    BasicAlertDialog(onDismissRequest = onCancelar) {
        Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(stringResource(Res.string.filtros), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

                Text(stringResource(Res.string.campo_categoria), style = MaterialTheme.typography.labelMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Categoria.entries.forEach { categoria ->
                        FilterChip(
                            selected = categoria in filtro.categorias,
                            onClick = { filtro = filtro.alternarCategoria(categoria) },
                            label = { Text(categoria.nombre()) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(categoria.color).copy(alpha = 0.4f)
                            )
                        )
                    }
                    categoriasPersonalizadas.forEach { categoria ->
                        FilterChip(
                            selected = categoria.id in filtro.personalizadas,
                            onClick = { filtro = filtro.alternarPersonalizada(categoria.id) },
                            label = { Text(categoria.nombre) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(categoria.color).copy(alpha = 0.4f)
                            )
                        )
                    }
                }

                Text(stringResource(Res.string.filtro_importe), style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = minimo,
                        onValueChange = { if (esEntradaImporteValida(it)) minimo = it },
                        label = { Text(stringResource(Res.string.filtro_importe_min)) },
                        prefix = { Text(simboloMoneda()) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = maximo,
                        onValueChange = { if (esEntradaImporteValida(it)) maximo = it },
                        label = { Text(stringResource(Res.string.filtro_importe_max)) },
                        prefix = { Text(simboloMoneda()) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(stringResource(Res.string.filtro_rango_fechas), style = MaterialTheme.typography.labelMedium)
                Text(
                    stringResource(Res.string.filtro_rango_fechas_detalle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                CampoFecha(
                    etiqueta = stringResource(Res.string.filtro_desde),
                    fecha = filtro.desde,
                    onCambio = { filtro = filtro.copy(desde = it) },
                    onQuitar = { filtro = filtro.copy(desde = null) }
                )
                CampoFecha(
                    etiqueta = stringResource(Res.string.filtro_hasta),
                    fecha = filtro.hasta,
                    onCambio = { filtro = filtro.copy(hasta = it) },
                    onQuitar = { filtro = filtro.copy(hasta = null) }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { onAplicar(FiltroGastos()) }) { Text(stringResource(Res.string.limpiar_filtros)) }
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = onCancelar) { Text(stringResource(Res.string.accion_cancelar)) }
                    Spacer(modifier = Modifier.size(4.dp))
                    Button(onClick = {
                        onAplicar(filtro.copy(importeMin = leerImporte(minimo), importeMax = leerImporte(maximo)))
                    }) { Text(stringResource(Res.string.aplicar)) }
                }
            }
        }
    }
}

/** Importe opcional del filtro: vacío → sin límite; admite 0 y coma decimal. */
private fun leerImporte(texto: String): Double? = texto.trim().replace(',', '.').toDoubleOrNull()?.takeIf { it >= 0 }
