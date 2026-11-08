package bas.orellana.gastostracker.domain.usecase

import bas.orellana.gastostracker.domain.model.TotalPorCategoria
import bas.orellana.gastostracker.domain.repository.GastoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

class GetTotalesGastosPorCategoriaUseCase(private val repository: GastoRepository) {
    operator fun invoke(desde: LocalDate, hasta: LocalDate): Flow<List<TotalPorCategoria>> =
        repository.getTotalesPorCategoria(desde, hasta)
}
