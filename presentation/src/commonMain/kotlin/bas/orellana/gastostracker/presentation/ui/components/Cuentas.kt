package bas.orellana.gastostracker.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import bas.orellana.gastostracker.domain.model.Cuenta
import bas.orellana.gastostracker.domain.model.TipoCuenta
import bas.orellana.gastostracker.presentation.resources.Res
import bas.orellana.gastostracker.presentation.resources.campo_cuenta
import bas.orellana.gastostracker.presentation.resources.sin_cuenta
import bas.orellana.gastostracker.presentation.resources.tipo_cuenta_banco
import bas.orellana.gastostracker.presentation.resources.tipo_cuenta_efectivo
import bas.orellana.gastostracker.presentation.resources.tipo_cuenta_otra
import bas.orellana.gastostracker.presentation.resources.tipo_cuenta_tarjeta_credito
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

val TipoCuenta.nombreRes: StringResource
    get() = when (this) {
        TipoCuenta.BANCO -> Res.string.tipo_cuenta_banco
        TipoCuenta.EFECTIVO -> Res.string.tipo_cuenta_efectivo
        TipoCuenta.TARJETA_CREDITO -> Res.string.tipo_cuenta_tarjeta_credito
        TipoCuenta.OTRA -> Res.string.tipo_cuenta_otra
    }

val TipoCuenta.icono: ImageVector
    get() = when (this) {
        TipoCuenta.BANCO -> Icons.Default.AccountBalance
        TipoCuenta.EFECTIVO -> Icons.Default.Payments
        TipoCuenta.TARJETA_CREDITO -> Icons.Default.CreditCard
        TipoCuenta.OTRA -> Icons.Default.AccountBalanceWallet
    }

@Composable
fun TipoCuenta.nombre(): String = stringResource(nombreRes)

/** Chips para elegir la cuenta de un movimiento. No se muestra nada si no hay cuentas. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SelectorCuenta(
    cuentas: List<Cuenta>,
    cuentaId: String?,
    onElegir: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    if (cuentas.isEmpty()) return
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = stringResource(Res.string.campo_cuenta),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            FilterChip(
                selected = cuentaId == null,
                onClick = { onElegir(null) },
                label = { Text(stringResource(Res.string.sin_cuenta)) }
            )
            cuentas.forEach { cuenta ->
                FilterChip(
                    selected = cuentaId == cuenta.id,
                    onClick = { onElegir(cuenta.id) },
                    label = { Text(cuenta.nombre) },
                    leadingIcon = {
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(cuenta.color)))
                    }
                )
            }
        }
    }
}
