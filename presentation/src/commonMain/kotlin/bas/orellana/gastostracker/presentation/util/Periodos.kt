package bas.orellana.gastostracker.presentation.util

import androidx.compose.runtime.Composable
import bas.orellana.gastostracker.domain.model.Mes
import bas.orellana.gastostracker.domain.repository.AjustesRepository
import bas.orellana.gastostracker.presentation.resources.Res
import bas.orellana.gastostracker.presentation.resources.periodo_rango
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.jetbrains.compose.resources.stringResource

/** Día de inicio del mes guardado en Ajustes (1 mientras no se ha leído o si falla la lectura). */
fun AjustesRepository.diaInicioMes(scope: CoroutineScope): StateFlow<Int> =
    ajustes.map { it.diaInicioMes }
        .distinctUntilChanged()
        .catch { emit(1) }
        .stateIn(scope, SharingStarted.Eagerly, 1)

/** "25/10/2026 – 24/11/2026" si el mes no empieza el día 1; null si es el mes natural. */
@Composable
fun subtituloPeriodo(mes: Mes, diaInicio: Int): String? {
    if (diaInicio <= 1) return null
    val periodo = mes.periodo(diaInicio)
    return stringResource(Res.string.periodo_rango, periodo.start.formatDDMMYYYY(), periodo.endInclusive.formatDDMMYYYY())
}
