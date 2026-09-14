package bas.orellana.gastostracker.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import bas.orellana.gastostracker.domain.model.Categoria
import bas.orellana.gastostracker.domain.model.IngresoModel
import bas.orellana.gastostracker.domain.repository.IngresoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

class IngresoRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : IngresoRepository {

    companion object {
        private val INGRESOS_KEY = stringPreferencesKey("ingresos")
    }

    override fun getIngresos(): Flow<List<IngresoModel>> {
        return dataStore.data.map { preferences ->
            val json = preferences[INGRESOS_KEY] ?: "[]"
            parseIngresosFromJson(json)
        }
    }

    override suspend fun addIngreso(ingreso: IngresoModel) {
        dataStore.edit { preferences ->
            val json = preferences[INGRESOS_KEY] ?: "[]"
            val ingresos = parseIngresosFromJson(json).toMutableList()
            ingresos.add(0, ingreso)
            preferences[INGRESOS_KEY] = ingresosToJson(ingresos)
        }
    }

    override suspend fun updateIngreso(ingreso: IngresoModel) {
        dataStore.edit { preferences ->
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
        dataStore.edit { preferences ->
            val json = preferences[INGRESOS_KEY] ?: "[]"
            val ingresos = parseIngresosFromJson(json).filter { it.id != id }
            preferences[INGRESOS_KEY] = ingresosToJson(ingresos)
        }
    }

    private fun parseIngresosFromJson(json: String): List<IngresoModel> {
        return try {
            repositoryJson.decodeFromString<List<IngresoDto>>(json).map { dto ->
                IngresoModel(
                    id = dto.id,
                    nombre = dto.nombre,
                    monto = dto.monto,
                    fecha = LocalDate.parse(dto.fecha),
                    categoria = dto.categoria?.let { name -> Categoria.entries.find { it.name == name } },
                    categoriaPersonalizadaId = dto.categoriaPersonalizadaId
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun ingresosToJson(ingresos: List<IngresoModel>): String {
        val dtos = ingresos.map { ingreso ->
            IngresoDto(
                id = ingreso.id,
                nombre = ingreso.nombre,
                monto = ingreso.monto,
                fecha = ingreso.fecha.toString(),
                categoria = ingreso.categoria?.name,
                categoriaPersonalizadaId = ingreso.categoriaPersonalizadaId
            )
        }
        return repositoryJson.encodeToString(dtos)
    }
}
