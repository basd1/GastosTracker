package bas.orellana.gastostracker.domain.usecase

import bas.orellana.gastostracker.domain.model.CategoriaPersonalizada
import bas.orellana.gastostracker.domain.repository.CategoriasRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeCategoriasRepository : CategoriasRepository {
    private val state = MutableStateFlow<List<CategoriaPersonalizada>>(emptyList())
    private var nextId = 0

    override fun getCategoriasPersonalizadas(): Flow<List<CategoriaPersonalizada>> = state

    override suspend fun addCategoriaPersonalizada(nombre: String, color: Long) {
        val categoria = CategoriaPersonalizada(id = (nextId++).toString(), nombre = nombre, color = color)
        state.value = state.value + categoria
    }

    override suspend fun updateCategoriaPersonalizada(categoria: CategoriaPersonalizada) {
        state.value = state.value.map { if (it.id == categoria.id) categoria else it }
    }

    override suspend fun deleteCategoriaPersonalizada(id: String) {
        state.value = state.value.filterNot { it.id == id }
    }
}
