package bas.orellana.gastostracker.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import bas.orellana.gastostracker.domain.model.IngresoModel
import bas.orellana.gastostracker.domain.repository.IngresoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

class IngresoRepositoryImpl(
    private val context: Context
) : IngresoRepository {

    companion object {
        private val INGRESOS_KEY = stringPreferencesKey("ingresos")
    }

    override fun getIngresos(): Flow<List<IngresoModel>> {
        return context.dataStore.data.map { preferences ->
            val json = preferences[INGRESOS_KEY] ?: "[]"
            parseIngresosFromJson(json)
        }
    }

    override suspend fun addIngreso(ingreso: IngresoModel) {
        context.dataStore.edit { preferences ->
            val json = preferences[INGRESOS_KEY] ?: "[]"
            val ingresos = parseIngresosFromJson(json).toMutableList()
            ingresos.add(0, ingreso)
            preferences[INGRESOS_KEY] = ingresosToJson(ingresos)
        }
    }

    override suspend fun updateIngreso(ingreso: IngresoModel) {
        context.dataStore.edit { preferences ->
            val json = preferences[INGRESOS_KEY] ?: "[]"
            val ingresos = parseIngresosFromJson(json).toMutableList()
            val index = ingresos.indexOfFirst { it.id == ingreso.id }
            if (index != -1) {
                ingresos[index] = ingreso
                preferences[INGRESOS_KEY] = ingresosToJson(ingresos)
            }
        }
    }

    override suspend fun deleteIngreso(id: String) {
        context.dataStore.edit { preferences ->
            val json = preferences[INGRESOS_KEY] ?: "[]"
            val ingresos = parseIngresosFromJson(json).filter { it.id != id }
            preferences[INGRESOS_KEY] = ingresosToJson(ingresos)
        }
    }

    private fun parseIngresosFromJson(json: String): List<IngresoModel> {
        return try {
            val jsonArray = JSONArray(json)
            (0 until jsonArray.length()).map { i ->
                val obj = jsonArray.getJSONObject(i)
                IngresoModel(
                    id = obj.getString("id"),
                    nombre = obj.getString("nombre"),
                    monto = obj.getDouble("monto"),
                    fecha = LocalDate.parse(obj.getString("fecha"))
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun ingresosToJson(ingresos: List<IngresoModel>): String {
        val jsonArray = JSONArray()
        ingresos.forEach { ingreso ->
            val obj = JSONObject()
            obj.put("id", ingreso.id)
            obj.put("nombre", ingreso.nombre)
            obj.put("monto", ingreso.monto)
            obj.put("fecha", ingreso.fecha.toString())
            jsonArray.put(obj)
        }
        return jsonArray.toString()
    }
}
