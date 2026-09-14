package bas.orellana.gastostracker.domain.usecase

import bas.orellana.gastostracker.domain.model.CategoriaPersonalizada
import bas.orellana.gastostracker.domain.repository.CategoriasRepository

class UpdateCategoriaPersonalizadaUseCase(
    private val repository: CategoriasRepository
) {
    suspend operator fun invoke(categoria: CategoriaPersonalizada) {
        repository.updateCategoriaPersonalizada(categoria)
    }
}
