package bas.orellana.gastostracker.domain.usecase

import bas.orellana.gastostracker.domain.repository.CategoriasRepository

class ContarMovimientosCategoriaUseCase(
    private val repository: CategoriasRepository
) {
    suspend operator fun invoke(idCategoriaPersonalizada: String): Int =
        repository.contarMovimientos(idCategoriaPersonalizada)
}
