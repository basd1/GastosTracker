package bas.orellana.gastostracker.presentation.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bas.orellana.gastostracker.domain.model.ProgresoMeta
import bas.orellana.gastostracker.domain.util.todayLocalDate
import bas.orellana.gastostracker.presentation.resources.Res
import bas.orellana.gastostracker.presentation.resources.meta_completada
import bas.orellana.gastostracker.presentation.resources.meta_cuota_mensual
import bas.orellana.gastostracker.presentation.resources.meta_faltan
import bas.orellana.gastostracker.presentation.resources.meta_progreso
import bas.orellana.gastostracker.presentation.resources.meta_sin_objetivo
import bas.orellana.gastostracker.presentation.resources.meta_vencida
import bas.orellana.gastostracker.presentation.util.formatDDMMYYYY
import bas.orellana.gastostracker.presentation.util.formatMoney
import org.jetbrains.compose.resources.stringResource

/** "1.200 € de 3.000 €", barra con el color de la meta y el consejo de cuánto apartar al mes. */
@Composable
fun ProgresoMetaInfo(progreso: ProgresoMeta, modifier: Modifier = Modifier) {
    val color = Color(progreso.meta.color)
    val hoy = todayLocalDate()
    Column(modifier = modifier.fillMaxWidth()) {
        val objetivo = progreso.meta.objetivo
        Text(
            text = if (objetivo != null) {
                stringResource(Res.string.meta_progreso, formatMoney(progreso.ahorrado), formatMoney(objetivo))
            } else {
                stringResource(Res.string.meta_sin_objetivo, formatMoney(progreso.ahorrado))
            },
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        val fraccion = progreso.progreso
        if (fraccion != null) {
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { fraccion.toFloat().coerceIn(0f, 1f) },
                color = color,
                trackColor = color.copy(alpha = 0.2f),
                strokeCap = StrokeCap.Round,
                modifier = Modifier.fillMaxWidth().height(8.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            val cuota = progreso.cuotaMensual(hoy)
            val fecha = progreso.meta.fechaObjetivo
            val restante = progreso.restante ?: 0.0
            Text(
                text = when {
                    progreso.completada -> stringResource(Res.string.meta_completada)
                    progreso.vencida(hoy) -> stringResource(Res.string.meta_vencida, formatMoney(restante))
                    cuota != null && fecha != null ->
                        stringResource(Res.string.meta_cuota_mensual, formatMoney(cuota), fecha.formatDDMMYYYY())
                    else -> stringResource(Res.string.meta_faltan, formatMoney(restante))
                },
                fontSize = 12.sp,
                color = if (progreso.vencida(hoy)) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
