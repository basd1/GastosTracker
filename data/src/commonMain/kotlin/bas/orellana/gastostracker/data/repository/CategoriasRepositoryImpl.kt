package bas.orellana.gastostracker.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import bas.orellana.gastostracker.domain.model.Categoria
import bas.orellana.gastostracker.domain.model.CategoriaPersonalizada
import bas.orellana.gastostracker.domain.repository.CategoriasRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

class CategoriasRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : CategoriasRepository {

    companion object {
        private val CATEGORIAS_KEY = stringPreferencesKey("categorias_personalizadas")
    }

    override fun getCategoriasPersonalizadas(): Flow<List<CategoriaPersonalizada>> {
        return dataStore.data.map { preferences ->
            val json = preferences[CATEGORIAS_KEY] ?: "[]"
            parseCategoriasFromJson(json)
        }
    }

    override suspend fun addCategoriaPersonalizada(nombre: String, color: Long) {
        val currentId = getNextId()
        val newCategoria = CategoriaPersonalizada(
            id = currentId,
            nombre = nombre,
            color = color
        )

        dataStore.edit { preferences ->
            val json = preferences[CATEGORIAS_KEY] ?: "[]"
            val categorias = parseCategoriasFromJson(json).toMutableList()
            categorias.add(newCategoria)
            preferences[CATEGORIAS_KEY] = categoriasToJson(categorias)
        }
    }

    override suspend fun updateCategoriaPersonalizada(categoria: CategoriaPersonalizada) {
        dataStore.edit { preferences ->
            val json = preferences[CATEGORIAS_KEY] ?: "[]"
            val categorias = parseCategoriasFromJson(json).toMutableList()
            val index = categorias.indexOfFirst { it.id == categoria.id }
            if (index != -1) {
                categorias[index] = categoria
                preferences[CATEGORIAS_KEY] = categoriasToJson(categorias)
            }
        }
    }

    override suspend fun deleteCategoriaPersonalizada(id: String) {
        dataStore.edit { preferences ->
            val json = preferences[CATEGORIAS_KEY] ?: "[]"
            val categorias = parseCategoriasFromJson(json).filter { it.id != id }
            preferences[CATEGORIAS_KEY] = categoriasToJson(categorias)
        }
    }

    private suspend fun getNextId(): String {
        val json = dataStore.data.first()[CATEGORIAS_KEY] ?: "[]"
        val categorias = parseCategoriasFromJson(json)
        val maxId = categorias.maxOfOrNull { it.id.toIntOrNull() ?: 0 } ?: 0
        return (maxId + 1).toString()
    }

    private fun getRandomColor(): Long {
        return Categoria.PALETA_COLORES_PERSONALIZADOS.random()
    }

    private fun parseCategoriasFromJson(json: String): List<CategoriaPersonalizada> {
        return try {
            repositoryJson.decodeFromString<List<CategoriaPersonalizadaDto>>(json).map { dto ->
                CategoriaPersonalizada(
                    id = dto.id,
                    nombre = dto.nombre,
                    color = dto.color ?: getRandomColor()
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun categoriasToJson(categorias: List<CategoriaPersonalizada>): String {
        val dtos = categorias.map { categoria ->
            CategoriaPersonalizadaDto(
                id = categoria.id,
                nombre = categoria.nombre,
                color = categoria.color
            )
        }
        return repositoryJson.encodeToString(dtos)
    }
}