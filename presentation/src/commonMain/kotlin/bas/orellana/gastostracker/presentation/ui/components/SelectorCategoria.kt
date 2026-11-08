package bas.orellana.gastostracker.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import bas.orellana.gastostracker.domain.model.Categoria
import bas.orellana.gastostracker.domain.model.CategoriaIngreso
import bas.orellana.gastostracker.domain.model.CategoriaPersonalizada
import bas.orellana.gastostracker.presentation.resources.Res
import bas.orellana.gastostracker.presentation.resources.categorias_personalizadas
import bas.orellana.gastostracker.presentation.resources.sin_categoria
import org.jetbrains.compose.resources.stringResource

/** Chips de categorías de gasto. */
@Composable
fun SelectorCategoria(
    predefinidas: List<Categoria>,
    categoriasPersonalizadas: List<CategoriaPersonalizada>,
    categoria: Categoria?,
    categoriaPersonalizadaId: String?,
    onElegir: (categoria: Categoria?, categoriaPersonalizadaId: String?) -> Unit,
    modifier: Modifier = Modifier,
    permitirSinCategoria: Boolean = true
) = SelectorCategoriaGenerico(
    predefinidas = predefinidas,
    nombre = { it.nombre() },
    color = { it.color },
    categoriasPersonalizadas = categoriasPersonalizadas,
    seleccionada = categoria,
    categoriaPersonalizadaId = categoriaPersonalizadaId,
    onElegir = onElegir,
    modifier = modifier,
    permitirSinCategoria = permitirSinCategoria
)

/** Chips de categorías de ingreso. */
@Composable
fun SelectorCategoriaIngreso(
    categoriasPersonalizadas: List<CategoriaPersonalizada>,
    categoria: CategoriaIngreso?,
    categoriaPersonalizadaId: String?,
    onElegir: (categoria: CategoriaIngreso?, categoriaPersonalizadaId: String?) -> Unit,
    modifier: Modifier = Modifier
) = SelectorCategoriaGenerico(
    predefinidas = CategoriaIngreso.entries,
    nombre = { it.nombre() },
    color = { it.color },
    categoriasPersonalizadas = categoriasPersonalizadas,
    seleccionada = categoria,
    categoriaPersonalizadaId = categoriaPersonalizadaId,
    onElegir = onElegir,
    modifier = modifier,
    permitirSinCategoria = true
)

/**
 * Chips para elegir la categoría de un movimiento: "Sin categoría", las predefinidas y las
 * personalizadas. Solo una a la vez.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> SelectorCategoriaGenerico(
    predefinidas: List<T>,
    nombre: @Composable (T) -> String,
    color: (T) -> Long,
    categoriasPersonalizadas: List<CategoriaPersonalizada>,
    seleccionada: T?,
    categoriaPersonalizadaId: String?,
    onElegir: (categoria: T?, categoriaPersonalizadaId: String?) -> Unit,
    modifier: Modifier,
    permitirSinCategoria: Boolean
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (permitirSinCategoria) {
                FilterChip(
                    selected = seleccionada == null && categoriaPersonalizadaId == null,
                    onClick = { onElegir(null, null) },
                    label = { Text(stringResource(Res.string.sin_categoria)) }
                )
            }
            predefinidas.forEach { opcion ->
                FilterChip(
                    selected = seleccionada == opcion,
                    onClick = { onElegir(opcion, null) },
                    label = { Text(nombre(opcion)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(color(opcion)).copy(alpha = 0.4f),
                        containerColor = Color(color(opcion)).copy(alpha = 0.15f)
                    )
                )
            }
        }
        if (categoriasPersonalizadas.isNotEmpty()) {
            Text(
                text = stringResource(Res.string.categorias_personalizadas),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                categoriasPersonalizadas.forEach { opcion ->
                    FilterChip(
                        selected = categoriaPersonalizadaId == opcion.id,
                        onClick = { onElegir(null, opcion.id) },
                        label = { Text(opcion.nombre) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(opcion.color).copy(alpha = 0.4f),
                            containerColor = Color(opcion.color).copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    }
}
