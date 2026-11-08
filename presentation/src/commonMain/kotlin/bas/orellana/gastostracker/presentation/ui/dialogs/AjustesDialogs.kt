package bas.orellana.gastostracker.presentation.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import bas.orellana.gastostracker.domain.model.Ajustes
import bas.orellana.gastostracker.presentation.resources.Res
import bas.orellana.gastostracker.presentation.resources.accion_cancelar
import bas.orellana.gastostracker.presentation.resources.ajustes_dia_inicio
import bas.orellana.gastostracker.presentation.resources.ajustes_moneda
import bas.orellana.gastostracker.presentation.resources.dia_inicio_explicacion
import bas.orellana.gastostracker.presentation.util.formatMoney
import bas.orellana.gastostracker.presentation.util.simboloMoneda
import org.jetbrains.compose.resources.stringResource

@Composable
fun MonedaDialog(actual: String, onElegir: (String) -> Unit, onCancelar: () -> Unit) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(stringResource(Res.string.ajustes_moneda)) },
        text = {
            LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                items(Ajustes.MONEDAS) { codigo ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onElegir(codigo) }.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = codigo == actual, onClick = { onElegir(codigo) })
                        Text("$codigo (${simboloMoneda(codigo)})", modifier = Modifier.weight(1f))
                        Text(formatMoney(1234.5, codigo), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onCancelar) { Text(stringResource(Res.string.accion_cancelar)) } }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DiaInicioMesDialog(actual: Int, onElegir: (Int) -> Unit, onCancelar: () -> Unit) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(stringResource(Res.string.ajustes_dia_inicio)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    stringResource(Res.string.dia_inicio_explicacion),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Ajustes.DIAS_INICIO_VALIDOS.forEach { dia ->
                        val elegido = dia == actual
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (elegido) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { onElegir(dia) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dia.toString(),
                                fontWeight = if (elegido) FontWeight.Bold else FontWeight.Normal,
                                color = if (elegido) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onCancelar) { Text(stringResource(Res.string.accion_cancelar)) } }
    )
}
