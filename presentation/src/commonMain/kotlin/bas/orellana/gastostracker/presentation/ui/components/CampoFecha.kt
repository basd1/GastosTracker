package bas.orellana.gastostracker.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bas.orellana.gastostracker.domain.util.todayLocalDate
import bas.orellana.gastostracker.presentation.resources.Res
import bas.orellana.gastostracker.presentation.resources.accion_cancelar
import bas.orellana.gastostracker.presentation.resources.accion_seleccionar
import bas.orellana.gastostracker.presentation.resources.cambiar_fecha
import bas.orellana.gastostracker.presentation.resources.quitar_fecha
import bas.orellana.gastostracker.presentation.resources.sin_fecha
import bas.orellana.gastostracker.presentation.util.formatDDMMYYYY
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource

/**
 * Fila "Etiqueta ...... 08/11/2026 📅" que abre el selector de fecha del sistema.
 * Con [onQuitar] la fecha es opcional y se puede dejar vacía.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoFecha(
    etiqueta: String,
    fecha: LocalDate?,
    onCambio: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    onQuitar: (() -> Unit)? = null
) {
    var mostrarSelector by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .clickable { mostrarSelector = true }
            .padding(start = 16.dp, end = if (onQuitar != null && fecha != null) 4.dp else 16.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = etiqueta, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
            Text(
                text = fecha?.formatDDMMYYYY() ?: stringResource(Res.string.sin_fecha),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = stringResource(Res.string.cambiar_fecha),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (onQuitar != null && fecha != null) {
            IconButton(onClick = onQuitar) {
                Icon(Icons.Default.Close, contentDescription = stringResource(Res.string.quitar_fecha))
            }
        }
    }

    if (mostrarSelector) {
        // El DatePicker de Material trabaja en milisegundos UTC: con la zona del dispositivo, al oeste de
        // Greenwich la fecha elegida se iría al día anterior.
        val zona = TimeZone.UTC
        val estado = rememberDatePickerState(
            initialSelectedDateMillis = (fecha ?: todayLocalDate()).atStartOfDayIn(zona).toEpochMilliseconds()
        )
        DatePickerDialog(
            onDismissRequest = { mostrarSelector = false },
            confirmButton = {
                TextButton(onClick = {
                    estado.selectedDateMillis?.let { millis ->
                        onCambio(Instant.fromEpochMilliseconds(millis).toLocalDateTime(zona).date)
                    }
                    mostrarSelector = false
                }) { Text(stringResource(Res.string.accion_seleccionar)) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarSelector = false }) {
                    Text(stringResource(Res.string.accion_cancelar), color = MaterialTheme.colorScheme.error)
                }
            }
        ) {
            DatePicker(state = estado)
        }
    }
}
