package bas.orellana.gastostracker.domain.usecase

import bas.orellana.gastostracker.domain.model.CategoriaDestino
import bas.orellana.gastostracker.domain.repository.CategoriasRepository

class DeleteCategoriaPersonalizadaUseCase(
    private val repository: CategoriasRepository
) {
    suspend operator fun invoke(id: String, destino: CategoriaDestino = CategoriaDestino.POR_DEFECTO) {
        require(destino != CategoriaDestino.Personalizada(id)) {
            "No se pueden reasignar los movimientos a la misma categoría que se elimina"
        }
        repository.deleteCategoriaPersonalizada(id, destino)
    }
}
