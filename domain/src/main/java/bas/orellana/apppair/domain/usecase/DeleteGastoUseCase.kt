package bas.orellana.gastostracker.domain.usecase

import bas.orellana.gastostracker.domain.repository.GastoRepository

class DeleteGastoUseCase(
    private val repository: GastoRepository
) {
    suspend operator fun invoke(id: String) {
        repository.deleteGasto(id)
    }
}