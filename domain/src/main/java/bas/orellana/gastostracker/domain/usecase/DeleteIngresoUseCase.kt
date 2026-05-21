package bas.orellana.gastostracker.domain.usecase

import bas.orellana.gastostracker.domain.repository.IngresoRepository

class DeleteIngresoUseCase(
    private val repository: IngresoRepository
) {
    suspend operator fun invoke(id: String) {
        repository.deleteIngreso(id)
    }
}
