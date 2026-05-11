package bas.orellana.gastostracker.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import bas.orellana.gastostracker.domain.model.Categoria
import bas.orellana.gastostracker.domain.model.GastoModel
import bas.orellana.gastostracker.domain.repository.GastoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

class GastoRepositoryImpl(
    private val context: Context
) : GastoRepository {

    companion object {
        private val GASTOS_KEY = stringPreferencesKey("gastos")
    }

    override fun getGastos(): Flow<List<GastoModel>> {
        return context.dataStore.data.map { preferences ->
            val json = preferences[GASTOS_KEY] ?: "[]"
            parseGastosFromJson(json)
        }
    }

    override suspend fun addGasto(gasto: GastoModel) {
        context.dataStore.edit { preferences ->
            val json = preferences[GASTOS_KEY] ?: "[]"
            val gastos = parseGastosFromJson(json).toMutableList()
            gastos.add(0, gasto)
            preferences[GASTOS_KEY] = gastosToJson(gastos)
        }
    }

    override suspend fun deleteGasto(id: String) {
        context.dataStore.edit { preferences ->
            val json = preferences[GASTOS_KEY] ?: "[]"
            val gastos = parseGastosFromJson(json).filter { it.id != id }
            preferences[GASTOS_KEY] = gastosToJson(gastos)
        }
    }

    private fun parseGastosFromJson(json: String): List<GastoModel> {
        return try {
            val jsonArray = JSONArray(json)
            (0 until jsonArray.length()).map { i ->
                val obj = jsonArray.getJSONObject(i)
                val categoriaName = obj.optString("categoria", null)
                val categoria = if (categoriaName != null) {
                    Categoria.entries.find { it.name == categoriaName }
                } else null

                GastoModel(
                    id = obj.getString("id"),
                    nombre = obj.getString("nombre"),
                    monto = obj.getDouble("monto"),
                    fecha = LocalDate.parse(obj.getString("fecha")),
                    categoria = categoria,
                    categoriaPersonalizadaId = obj.optString("categoriaPersonalizadaId", null)
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun gastosToJson(gastos: List<GastoModel>): String {
        val jsonArray = JSONArray()
        gastos.forEach { gasto ->
            val obj = JSONObject()
            obj.put("id", gasto.id)
            obj.put("nombre", gasto.nombre)
            obj.put("monto", gasto.monto)
            obj.put("fecha", gasto.fecha.toString())
            obj.put("categoria", gasto.categoria?.name ?: JSONObject.NULL)
            obj.put("categoriaPersonalizadaId", gasto.categoriaPersonalizadaId ?: JSONObject.NULL)
            jsonArray.put(obj)
        }
        return jsonArray.toString()
    }
}