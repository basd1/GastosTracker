package bas.orellana.gastostracker.domain.usecase

import bas.orellana.gastostracker.domain.repository.IngresoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

class GetTotalIngresosUseCase(private val repository: IngresoRepository) {
    operator fun invoke(desde: LocalDate, hasta: LocalDate): Flow<Double> =
        repository.getTotalIngresos(desde, hasta)
}
