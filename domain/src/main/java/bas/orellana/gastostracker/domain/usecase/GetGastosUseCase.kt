package bas.orellana.gastostracker.domain.usecase

import bas.orellana.gastostracker.domain.model.GastoModel
import bas.orellana.gastostracker.domain.repository.GastoRepository
import kotlinx.coroutines.flow.Flow

class GetGastosUseCase(private val repository: GastoRepository) {
    operator fun invoke(): Flow<List<GastoModel>> = repository.getGastos()
}