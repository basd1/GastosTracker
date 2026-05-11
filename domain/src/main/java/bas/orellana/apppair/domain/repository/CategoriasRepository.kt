package bas.orellana.gastostracker.domain.repository

import bas.orellana.gastostracker.domain.model.CategoriaPersonalizada
import kotlinx.coroutines.flow.Flow

interface CategoriasRepository {
    fun getCategoriasPersonalizadas(): Flow<List<CategoriaPersonalizada>>
    suspend fun addCategoriaPersonalizada(nombre: String, color: Long)
    suspend fun deleteCategoriaPersonalizada(id: String)
}