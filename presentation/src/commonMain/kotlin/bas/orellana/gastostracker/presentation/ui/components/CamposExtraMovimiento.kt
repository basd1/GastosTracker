package bas.orellana.gastostracker.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import bas.orellana.gastostracker.domain.model.MetodoPago
import bas.orellana.gastostracker.presentation.resources.Res
import bas.orellana.gastostracker.presentation.resources.campo_metodo_pago
import bas.orellana.gastostracker.presentation.resources.campo_nota_opcional
import bas.orellana.gastostracker.presentation.resources.metodo_bizum
import bas.orellana.gastostracker.presentation.resources.metodo_efectivo
import bas.orellana.gastostracker.presentation.resources.metodo_otro
import bas.orellana.gastostracker.presentation.resources.metodo_tarjeta
import bas.orellana.gastostracker.presentation.resources.metodo_transferencia
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

val MetodoPago.nombreRes: StringResource
    get() = when (this) {
        MetodoPago.EFECTIVO -> Res.string.metodo_efectivo
        MetodoPago.TARJETA -> Res.string.metodo_tarjeta
        MetodoPago.TRANSFERENCIA -> Res.string.metodo_transferencia
        MetodoPago.BIZUM -> Res.string.metodo_bizum
        MetodoPago.OTRO -> Res.string.metodo_otro
    }

val MetodoPago.icono: ImageVector
    get() = when (this) {
        MetodoPago.EFECTIVO -> Icons.Default.Payments
        MetodoPago.TARJETA -> Icons.Default.CreditCard
        MetodoPago.TRANSFERENCIA -> Icons.Default.AccountBalance
        MetodoPago.BIZUM -> Icons.Default.PhoneIphone
        MetodoPago.OTRO -> Icons.Default.MoreHoriz
    }

@Composable
fun MetodoPago.nombre(): String = stringResource(nombreRes)

/** Campo de nota y, si se pasa [onMetodoPago], chips de método de pago (pulsar el elegido lo quita). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CamposExtraMovimiento(
    nota: String,
    onNota: (String) -> Unit,
    metodoPago: MetodoPago? = null,
    onMetodoPago: ((MetodoPago?) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = nota,
            onValueChange = onNota,
            label = { Text(stringResource(Res.string.campo_nota_opcional)) },
            maxLines = 3,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier.fillMaxWidth()
        )
        if (onMetodoPago != null) {
            Text(
                text = stringResource(Res.string.campo_metodo_pago),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                MetodoPago.entries.forEach { metodo ->
                    FilterChip(
                        selected = metodoPago == metodo,
                        onClick = { onMetodoPago(if (metodoPago == metodo) null else metodo) },
                        label = { Text(metodo.nombre()) },
                        leadingIcon = { Icon(metodo.icono, contentDescription = null) }
                    )
                }
            }
        }
    }
}
