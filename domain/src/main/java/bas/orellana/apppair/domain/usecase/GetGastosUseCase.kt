package bas.orellana.apppair.domain.usecase

import bas.orellana.apppair.domain.model.GastoModel
import bas.orellana.apppair.domain.repository.GastoRepository
import kotlinx.coroutines.flow.Flow

class GetGastosUseCase(private val repository: GastoRepository) {
    operator fun invoke(): Flow<List<GastoModel>> = repository.getGastos()
}