package bas.orellana.gastostracker.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bas.orellana.gastostracker.domain.model.CategoriaPersonalizada
import bas.orellana.gastostracker.domain.model.EstadoPresupuesto
import bas.orellana.gastostracker.domain.model.NivelPresupuesto
import bas.orellana.gastostracker.domain.model.Presupuesto
import bas.orellana.gastostracker.presentation.resources.Res
import bas.orellana.gastostracker.presentation.resources.presupuesto_gastado_de
import bas.orellana.gastostracker.presentation.resources.presupuesto_global
import bas.orellana.gastostracker.presentation.resources.presupuesto_restante
import bas.orellana.gastostracker.presentation.resources.presupuesto_superado
import bas.orellana.gastostracker.presentation.resources.sin_categoria
import bas.orellana.gastostracker.presentation.util.formatMoney
import org.jetbrains.compose.resources.stringResource
import kotlin.math.abs

private val VERDE = Color(0xFF43A047)
private val AMBAR = Color(0xFFFFA000)

@Composable
fun colorNivel(nivel: NivelPresupuesto): Color = when (nivel) {
    NivelPresupuesto.BIEN -> VERDE
    NivelPresupuesto.AVISO -> AMBAR
    NivelPresupuesto.SUPERADO -> MaterialTheme.colorScheme.error
}

@Composable
fun nombrePresupuesto(presupuesto: Presupuesto, categoriasPersonalizadas: List<CategoriaPersonalizada>): String {
    val categoria = presupuesto.categoria
    return when {
        presupuesto.esGlobal -> stringResource(Res.string.presupuesto_global)
        categoria != null -> categoria.nombre()
        else -> categoriasPersonalizadas.find { it.id == presupuesto.categoriaPersonalizadaId }?.nombre
            ?: stringResource(Res.string.sin_categoria)
    }
}

fun colorPresupuesto(presupuesto: Presupuesto, categoriasPersonalizadas: List<CategoriaPersonalizada>): Color? {
    val categoria = presupuesto.categoria
    return when {
        categoria != null -> Color(categoria.color)
        presupuesto.categoriaPersonalizadaId != null ->
            categoriasPersonalizadas.find { it.id == presupuesto.categoriaPersonalizadaId }?.let { Color(it.color) }
        else -> null
    }
}

/** Nombre, "gastado de límite", barra de progreso de color y lo que queda (o lo que se ha pasado). */
@Composable
fun FilaPresupuesto(
    estado: EstadoPresupuesto,
    categoriasPersonalizadas: List<CategoriaPersonalizada>,
    modifier: Modifier = Modifier,
    compacta: Boolean = false
) {
    val color = colorNivel(estado.nivel)
    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val colorCategoria = colorPresupuesto(estado.presupuesto, categoriasPersonalizadas)
            if (colorCategoria != null) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(colorCategoria)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = nombrePresupuesto(estado.presupuesto, categoriasPersonalizadas),
                style = if (compacta) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = stringResource(
                    Res.string.presupuesto_gastado_de,
                    formatMoney(estado.gastado),
                    formatMoney(estado.presupuesto.monto)
                ),
                fontSize = if (compacta) 12.sp else 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { estado.progreso.toFloat().coerceIn(0f, 1f) },
            color = color,
            trackColor = color.copy(alpha = 0.18f),
            strokeCap = StrokeCap.Round,
            modifier = Modifier
                .fillMaxWidth()
                .height(if (compacta) 6.dp else 8.dp)
        )
        if (!compacta) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (estado.nivel == NivelPresupuesto.SUPERADO) {
                    stringResource(Res.string.presupuesto_superado, formatMoney(abs(estado.restante)))
                } else {
                    stringResource(Res.string.presupuesto_restante, formatMoney(estado.restante))
                },
                fontSize = 12.sp,
                color = color,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
