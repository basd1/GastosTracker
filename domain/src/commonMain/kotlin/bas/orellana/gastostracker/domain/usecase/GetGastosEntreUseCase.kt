package bas.orellana.gastostracker.domain.usecase

import bas.orellana.gastostracker.domain.model.GastoModel
import bas.orellana.gastostracker.domain.repository.GastoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/** Gastos entre dos fechas (ambas incluidas), filtrados en SQL. */
class GetGastosEntreUseCase(private val repository: GastoRepository) {
    operator fun invoke(desde: LocalDate, hasta: LocalDate): Flow<List<GastoModel>> =
        repository.getGastosEntre(desde, hasta)
}
