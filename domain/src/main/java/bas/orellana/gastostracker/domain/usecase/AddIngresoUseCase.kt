package bas.orellana.gastostracker.domain.usecase

import bas.orellana.gastostracker.domain.model.IngresoModel
import bas.orellana.gastostracker.domain.repository.IngresoRepository

class AddIngresoUseCase(
    private val repository: IngresoRepository
) {
    suspend operator fun invoke(ingreso: IngresoModel) {
        repository.addIngreso(ingreso)
    }
}
