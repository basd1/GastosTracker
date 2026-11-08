package bas.orellana.gastostracker.presentation.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import bas.orellana.gastostracker.presentation.resources.Res
import bas.orellana.gastostracker.presentation.resources.repetir_cada_mes
import bas.orellana.gastostracker.presentation.resources.repetir_cada_mes_detalle
import org.jetbrains.compose.resources.stringResource

/** Interruptor "Repetir cada mes" de los diálogos de gasto e ingreso (solo al crear). */
@Composable
fun OpcionRepetirMensual(
    activado: Boolean,
    dia: Int,
    onCambio: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraSmall)
            .clickable { onCambio(!activado) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(stringResource(Res.string.repetir_cada_mes), style = MaterialTheme.typography.bodyLarge)
            if (activado) {
                Text(
                    text = stringResource(Res.string.repetir_cada_mes_detalle, dia),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(checked = activado, onCheckedChange = onCambio)
    }
}
