package bas.orellana.gastostracker.domain.usecase

import bas.orellana.gastostracker.domain.model.GastoModel
import bas.orellana.gastostracker.domain.repository.GastoRepository

class AddGastoUseCase(
    private val repository: GastoRepository
) {
    suspend operator fun invoke(gasto: GastoModel) {
        repository.addGasto(gasto)
    }
}