package bas.orellana.gastostracker.domain.usecase

import bas.orellana.gastostracker.domain.model.IngresoModel
import bas.orellana.gastostracker.domain.repository.IngresoRepository
import kotlinx.coroutines.flow.Flow

class GetIngresosUseCase(private val repository: IngresoRepository) {
    operator fun invoke(): Flow<List<IngresoModel>> = repository.getIngresos()
}
