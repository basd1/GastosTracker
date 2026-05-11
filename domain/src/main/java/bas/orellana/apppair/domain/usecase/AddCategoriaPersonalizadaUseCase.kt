package bas.orellana.gastostracker.domain.usecase

import bas.orellana.gastostracker.domain.repository.CategoriasRepository

class AddCategoriaPersonalizadaUseCase(
    private val repository: CategoriasRepository
) {
    suspend operator fun invoke(nombre: String) {
        repository.addCategoriaPersonalizada(nombre)
    }
}