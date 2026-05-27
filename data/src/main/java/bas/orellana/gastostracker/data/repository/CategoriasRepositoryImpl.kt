package bas.orellana.gastostracker.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import bas.orellana.gastostracker.domain.model.Categoria
import bas.orellana.gastostracker.domain.model.CategoriaPersonalizada
import bas.orellana.gastostracker.domain.repository.CategoriasRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

class CategoriasRepositoryImpl(
    private val context: Context
) : CategoriasRepository {

    companion object {
        private val CATEGORIAS_KEY = stringPreferencesKey("categorias_personalizadas")
    }

    override fun getCategoriasPersonalizadas(): Flow<List<CategoriaPersonalizada>> {
        return context.dataStore.data.map { preferences ->
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

        context.dataStore.edit { preferences ->
            val json = preferences[CATEGORIAS_KEY] ?: "[]"
            val categorias = parseCategoriasFromJson(json).toMutableList()
            categorias.add(newCategoria)
            preferences[CATEGORIAS_KEY] = categoriasToJson(categorias)
        }
    }

    override suspend fun updateCategoriaPersonalizada(categoria: CategoriaPersonalizada) {
        context.dataStore.edit { preferences ->
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
        context.dataStore.edit { preferences ->
            val json = preferences[CATEGORIAS_KEY] ?: "[]"
            val categorias = parseCategoriasFromJson(json).filter { it.id != id }
            preferences[CATEGORIAS_KEY] = categoriasToJson(categorias)
        }
    }

    private suspend fun getNextId(): String {
        val json = context.dataStore.data.first()[CATEGORIAS_KEY] ?: "[]"
        val categorias = parseCategoriasFromJson(json)
        val maxId = categorias.maxOfOrNull { it.id.toIntOrNull() ?: 0 } ?: 0
        return (maxId + 1).toString()
    }

    private fun getRandomColor(): Long {
        return Categoria.PALETA_COLORES_PERSONALIZADOS.random()
    }

    private fun parseCategoriasFromJson(json: String): List<CategoriaPersonalizada> {
        return try {
            val jsonArray = JSONArray(json)
            (0 until jsonArray.length()).map { i ->
                val obj = jsonArray.getJSONObject(i)
                val color = try {
                    obj.getLong("color")
                } catch (e: Exception) {
                    getRandomColor()
                }
                CategoriaPersonalizada(
                    id = obj.getString("id"),
                    nombre = obj.getString("nombre"),
                    color = color
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun categoriasToJson(categorias: List<CategoriaPersonalizada>): String {
        val jsonArray = JSONArray()
        categorias.forEach { categoria ->
            val obj = JSONObject()
            obj.put("id", categoria.id)
            obj.put("nombre", categoria.nombre)
            obj.put("color", categoria.color)
            jsonArray.put(obj)
        }
        return jsonArray.toString()
    }
}