package bas.orellana.gastostracker.domain.usecase

import bas.orellana.gastostracker.domain.repository.CategoriasRepository

class DeleteCategoriaPersonalizadaUseCase(
    private val repository: CategoriasRepository
) {
    suspend operator fun invoke(id: String) {
        repository.deleteCategoriaPersonalizada(id)
    }
}