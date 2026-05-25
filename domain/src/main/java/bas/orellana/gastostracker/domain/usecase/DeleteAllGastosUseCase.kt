package bas.orellana.gastostracker.domain.usecase

import bas.orellana.gastostracker.domain.repository.GastoRepository

class DeleteAllGastosUseCase(
    private val repository: GastoRepository
) {
    suspend operator fun invoke() {
        repository.deleteAllGastos()
    }
}
