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
import bas.orellana.gastostracker.domain.model.CategoriaPersonalizada
import bas.orellana.gastostracker.presentation.resources.Res
import bas.orellana.gastostracker.presentation.resources.categorias_personalizadas
import bas.orellana.gastostracker.presentation.resources.sin_categoria
import org.jetbrains.compose.resources.stringResource

/**
 * Chips para elegir la categoría de un movimiento: "Sin categoría", las predefinidas de [predefinidas]
 * y las personalizadas. Solo una a la vez.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SelectorCategoria(
    predefinidas: List<Categoria>,
    categoriasPersonalizadas: List<CategoriaPersonalizada>,
    categoria: Categoria?,
    categoriaPersonalizadaId: String?,
    onElegir: (categoria: Categoria?, categoriaPersonalizadaId: String?) -> Unit,
    modifier: Modifier = Modifier,
    permitirSinCategoria: Boolean = true
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (permitirSinCategoria) {
                FilterChip(
                    selected = categoria == null && categoriaPersonalizadaId == null,
                    onClick = { onElegir(null, null) },
                    label = { Text(stringResource(Res.string.sin_categoria)) }
                )
            }
            predefinidas.forEach { opcion ->
                FilterChip(
                    selected = categoria == opcion,
                    onClick = { onElegir(opcion, null) },
                    label = { Text(opcion.nombre()) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(opcion.color).copy(alpha = 0.4f),
                        containerColor = Color(opcion.color).copy(alpha = 0.15f)
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
