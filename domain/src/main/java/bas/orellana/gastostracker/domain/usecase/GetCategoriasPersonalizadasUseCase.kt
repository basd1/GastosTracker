package bas.orellana.gastostracker.domain.usecase

import bas.orellana.gastostracker.domain.model.CategoriaPersonalizada
import bas.orellana.gastostracker.domain.repository.CategoriasRepository
import kotlinx.coroutines.flow.Flow

class GetCategoriasPersonalizadasUseCase(
    private val repository: CategoriasRepository
) {
    operator fun invoke(): Flow<List<CategoriaPersonalizada>> {
        return repository.getCategoriasPersonalizadas()
    }
}