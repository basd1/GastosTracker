package bas.orellana.gastostracker.domain.usecase

import bas.orellana.gastostracker.domain.model.IngresoModel
import bas.orellana.gastostracker.domain.repository.IngresoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/** Ingresos entre dos fechas (ambas incluidas), filtrados en SQL. */
class GetIngresosEntreUseCase(private val repository: IngresoRepository) {
    operator fun invoke(desde: LocalDate, hasta: LocalDate): Flow<List<IngresoModel>> =
        repository.getIngresosEntre(desde, hasta)
}
