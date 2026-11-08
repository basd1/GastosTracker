package bas.orellana.gastostracker.presentation.state

import bas.orellana.gastostracker.presentation.resources.Res
import bas.orellana.gastostracker.presentation.resources.orden_mas_antiguo
import bas.orellana.gastostracker.presentation.resources.orden_mas_reciente
import bas.orellana.gastostracker.presentation.resources.orden_mayor_importe
import bas.orellana.gastostracker.presentation.resources.orden_menor_importe
import bas.orellana.gastostracker.presentation.resources.orden_nombre_asc
import bas.orellana.gastostracker.presentation.resources.orden_nombre_desc
import org.jetbrains.compose.resources.StringResource

enum class SortOrder(val etiqueta: StringResource) {
    DATE_DESC(Res.string.orden_mas_reciente),
    DATE_ASC(Res.string.orden_mas_antiguo),
    AMOUNT_DESC(Res.string.orden_mayor_importe),
    AMOUNT_ASC(Res.string.orden_menor_importe),
    NAME_ASC(Res.string.orden_nombre_asc),
    NAME_DESC(Res.string.orden_nombre_desc)
}
