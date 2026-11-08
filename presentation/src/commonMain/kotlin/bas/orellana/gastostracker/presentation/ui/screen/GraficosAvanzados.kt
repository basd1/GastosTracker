package bas.orellana.gastostracker.presentation.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bas.orellana.gastostracker.domain.model.Categoria
import bas.orellana.gastostracker.domain.model.CategoriaPersonalizada
import bas.orellana.gastostracker.domain.model.ComparativaAnual
import bas.orellana.gastostracker.domain.model.GastoModel
import bas.orellana.gastostracker.domain.model.IngresoModel
import bas.orellana.gastostracker.domain.model.Mes
import bas.orellana.gastostracker.domain.model.comparativaConAnioAnterior
import bas.orellana.gastostracker.domain.model.gastoMensualDeCategoria
import bas.orellana.gastostracker.domain.model.totalesMensuales
import bas.orellana.gastostracker.domain.model.ultimosMeses
import bas.orellana.gastostracker.presentation.resources.Res
import bas.orellana.gastostracker.presentation.resources.comparativa_anual
import bas.orellana.gastostracker.presentation.resources.comparativa_sin_datos
import bas.orellana.gastostracker.presentation.resources.comparativa_total
import bas.orellana.gastostracker.presentation.resources.evolucion_categoria
import bas.orellana.gastostracker.presentation.resources.ingresos_vs_gastos
import bas.orellana.gastostracker.presentation.resources.resumen_gastos
import bas.orellana.gastostracker.presentation.resources.resumen_ingresos
import bas.orellana.gastostracker.presentation.resources.sin_categoria
import bas.orellana.gastostracker.presentation.resources.toca_una_barra
import bas.orellana.gastostracker.presentation.ui.components.mesYAnio
import bas.orellana.gastostracker.presentation.ui.components.nombre
import bas.orellana.gastostracker.presentation.ui.components.nombreMesCorto
import bas.orellana.gastostracker.presentation.util.formatDecimal
import bas.orellana.gastostracker.presentation.util.formatMoney
import org.jetbrains.compose.resources.stringResource
import kotlin.math.abs

private val COLOR_INGRESOS = Color(0xFF43A047)
private val COLOR_GASTOS = Color(0xFFE53935)

/** Una serie de barras: un valor por grupo (mes). */
data class SerieBarras(val nombre: String, val color: Color, val valores: List<Double>)

/**
 * Barras agrupadas: un grupo por etiqueta y una barra por serie. Tocar un grupo lo selecciona y
 * enseña sus valores debajo.
 */
@Composable
fun GraficoBarras(
    etiquetas: List<String>,
    series: List<SerieBarras>,
    modifier: Modifier = Modifier
) {
    var seleccionado by remember(etiquetas) { mutableStateOf(etiquetas.lastIndex) }
    val maximo = series.flatMap { it.valores }.maxOrNull()?.takeIf { it > 0 } ?: 1.0
    val colorEje = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    val colorSeleccion = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .pointerInput(etiquetas) {
                    detectTapGestures { toque ->
                        if (etiquetas.isNotEmpty()) {
                            val anchoGrupo = size.width.toFloat() / etiquetas.size
                            seleccionado = (toque.x / anchoGrupo).toInt().coerceIn(0, etiquetas.lastIndex)
                        }
                    }
                }
        ) {
            if (etiquetas.isEmpty()) return@Canvas
            val anchoGrupo = size.width / etiquetas.size
            val hueco = anchoGrupo * 0.2f
            val anchoBarra = (anchoGrupo - hueco) / series.size.coerceAtLeast(1)
            if (seleccionado in etiquetas.indices) {
                drawRect(colorSeleccion, topLeft = Offset(anchoGrupo * seleccionado, 0f), size = Size(anchoGrupo, size.height))
            }
            drawLine(colorEje, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 1.dp.toPx())
            etiquetas.indices.forEach { grupo ->
                series.forEachIndexed { indiceSerie, serie ->
                    val valor = serie.valores.getOrElse(grupo) { 0.0 }
                    val alto = (valor / maximo).toFloat() * (size.height - 4.dp.toPx())
                    if (alto > 0f) {
                        drawRoundRect(
                            color = serie.color,
                            topLeft = Offset(anchoGrupo * grupo + hueco / 2 + anchoBarra * indiceSerie, size.height - alto),
                            size = Size(anchoBarra * 0.9f, alto),
                            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                        )
                    }
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            etiquetas.forEachIndexed { indice, etiqueta ->
                Text(
                    text = etiqueta,
                    fontSize = 10.sp,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    fontWeight = if (indice == seleccionado) FontWeight.Bold else FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        if (seleccionado in etiquetas.indices) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.align(Alignment.CenterHorizontally)) {
                series.forEach { serie ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(serie.color))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${serie.nombre}: ${formatMoney(serie.valores.getOrElse(seleccionado) { 0.0 })}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        } else {
            Text(
                text = stringResource(Res.string.toca_una_barra),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun TarjetaGrafico(titulo: String, contenido: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = MaterialTheme.shapes.small
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Text(titulo, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(8.dp))
            contenido()
        }
    }
}

/** Ingresos frente a gastos en los 6 meses que terminan en [hasta]. */
@Composable
fun IngresosVsGastosCard(gastos: List<GastoModel>, ingresos: List<IngresoModel>, hasta: Mes) {
    val meses = remember(hasta) { ultimosMeses(hasta, 6) }
    val totales = remember(gastos, ingresos, meses) { totalesMensuales(gastos, ingresos, meses) }
    TarjetaGrafico(stringResource(Res.string.ingresos_vs_gastos)) {
        GraficoBarras(
            etiquetas = meses.map { nombreMesCorto(it.numero) },
            series = listOf(
                SerieBarras(stringResource(Res.string.resumen_ingresos), COLOR_INGRESOS, totales.map { it.ingresos }),
                SerieBarras(stringResource(Res.string.resumen_gastos), COLOR_GASTOS, totales.map { it.gastos })
            )
        )
    }
}

/** Lo gastado en una categoría (a elegir) en los 12 meses que terminan en [hasta]. */
@Composable
fun EvolucionCategoriaCard(
    gastos: List<GastoModel>,
    categoriasPersonalizadas: List<CategoriaPersonalizada>,
    hasta: Mes,
    categoriaInicial: Pair<Categoria?, String?>?
) {
    // Solo se ofrecen las categorías que tienen algún gasto.
    val conGastos = remember(gastos) {
        gastos.map { it.categoria to it.categoriaPersonalizadaId }
            .filter { it.first != null || it.second != null }
            .groupingBy { it }.eachCount().entries.sortedByDescending { it.value }.map { it.key }
    }
    if (conGastos.isEmpty()) return
    var elegida by remember(conGastos, categoriaInicial) {
        mutableStateOf(categoriaInicial?.takeIf { it in conGastos } ?: conGastos.first())
    }
    val meses = remember(hasta) { ultimosMeses(hasta, 12) }
    val serie = remember(gastos, elegida, meses) { gastoMensualDeCategoria(gastos, elegida.first, elegida.second, meses) }

    TarjetaGrafico(stringResource(Res.string.evolucion_categoria)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
        ) {
            conGastos.forEach { opcion ->
                val (nombre, color) = nombreYColor(opcion, categoriasPersonalizadas)
                FilterChip(
                    selected = opcion == elegida,
                    onClick = { elegida = opcion },
                    label = { Text(nombre, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = color.copy(alpha = 0.4f))
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        val (nombre, color) = nombreYColor(elegida, categoriasPersonalizadas)
        GraficoBarras(
            etiquetas = meses.map { nombreMesCorto(it.numero).take(1) },
            series = listOf(SerieBarras(nombre, color, serie.map { it.second }))
        )
    }
}

@Composable
private fun nombreYColor(clave: Pair<Categoria?, String?>, personalizadas: List<CategoriaPersonalizada>): Pair<String, Color> {
    val categoria = clave.first
    if (categoria != null) return categoria.nombre() to Color(categoria.color)
    val personalizada = personalizadas.find { it.id == clave.second }
    return (personalizada?.nombre ?: stringResource(Res.string.sin_categoria)) to Color(personalizada?.color ?: 0xFF90A4AE)
}

/** Lo gastado en [mes] frente al mismo mes del año anterior, en total y en las categorías que más cambian. */
@Composable
fun ComparativaAnualCard(gastos: List<GastoModel>, categoriasPersonalizadas: List<CategoriaPersonalizada>, mes: Mes) {
    val comparativa: ComparativaAnual = remember(gastos, mes) { comparativaConAnioAnterior(gastos, mes) }
    val anterior = mes.mas(-12)
    TarjetaGrafico(stringResource(Res.string.comparativa_anual, mesYAnio(mes.numero, mes.anio), mesYAnio(anterior.numero, anterior.anio))) {
        if (comparativa.total.actual == 0.0 && comparativa.total.anterior == 0.0) {
            Text(stringResource(Res.string.comparativa_sin_datos), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            return@TarjetaGrafico
        }
        FilaComparativa(
            nombre = stringResource(Res.string.comparativa_total),
            actual = comparativa.total.actual,
            anterior = comparativa.total.anterior,
            variacion = comparativa.total.variacion,
            negrita = true
        )
        comparativa.porCategoria.take(4).forEach { item ->
            val (nombre, _) = nombreYColor(item.categoria to item.categoriaPersonalizadaId, categoriasPersonalizadas)
            FilaComparativa(nombre, item.comparativa.actual, item.comparativa.anterior, item.comparativa.variacion, negrita = false)
        }
    }
}

@Composable
private fun FilaComparativa(nombre: String, actual: Double, anterior: Double, variacion: Double?, negrita: Boolean) {
    val diferencia = actual - anterior
    // Gastar más es "malo" (rojo); gastar menos, "bueno" (verde).
    val color = when {
        diferencia > 0 -> COLOR_GASTOS
        diferencia < 0 -> COLOR_INGRESOS
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = nombre,
            fontSize = 13.sp,
            fontWeight = if (negrita) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "${formatMoney(anterior)} → ${formatMoney(actual)}",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = variacion?.let { (if (it >= 0) "+" else "−") + formatDecimal(abs(it) * 100, 0) + "%" } ?: "—",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.width(52.dp),
            textAlign = TextAlign.End
        )
    }
}
