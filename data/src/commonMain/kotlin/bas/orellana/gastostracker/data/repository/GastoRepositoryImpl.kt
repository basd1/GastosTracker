package bas.orellana.gastostracker.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import bas.orellana.gastostracker.domain.model.Categoria
import bas.orellana.gastostracker.domain.model.GastoModel
import bas.orellana.gastostracker.domain.repository.GastoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

class GastoRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : GastoRepository {

    companion object {
        private val GASTOS_KEY = stringPreferencesKey("gastos")
    }

    override fun getGastos(): Flow<List<GastoModel>> {
        return dataStore.data.map { preferences ->
            val json = preferences[GASTOS_KEY] ?: "[]"
            parseGastosFromJson(json)
        }
    }

    override suspend fun addGasto(gasto: GastoModel) {
        dataStore.edit { preferences ->
            val json = preferences[GASTOS_KEY] ?: "[]"
            val gastos = parseGastosFromJson(json).toMutableList()
            gastos.add(0, gasto)
            preferences[GASTOS_KEY] = gastosToJson(gastos)
        }
    }

    override suspend fun updateGasto(gasto: GastoModel) {
        dataStore.edit { preferences ->
            val json = preferences[GASTOS_KEY] ?: "[]"
            val gastos = parseGastosFromJson(json).toMutableList()
            val index = gastos.indexOfFirst { it.id == gasto.id }
            if (index != -1) {
                gastos[index] = gasto
                preferences[GASTOS_KEY] = gastosToJson(gastos)
            }
        }
    }

    override suspend fun deleteGasto(id: String) {
        dataStore.edit { preferences ->
            val json = preferences[GASTOS_KEY] ?: "[]"
            val gastos = parseGastosFromJson(json).filter { it.id != id }
            preferences[GASTOS_KEY] = gastosToJson(gastos)
        }
    }

    override suspend fun deleteAllGastos() {
        dataStore.edit { preferences ->
            preferences[GASTOS_KEY] = "[]"
        }
    }

    private fun parseGastosFromJson(json: String): List<GastoModel> {
        return try {
            repositoryJson.decodeFromString<List<GastoDto>>(json).map { dto ->
                GastoModel(
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

    private fun gastosToJson(gastos: List<GastoModel>): String {
        val dtos = gastos.map { gasto ->
            GastoDto(
                id = gasto.id,
                nombre = gasto.nombre,
                monto = gasto.monto,
                fecha = gasto.fecha.toString(),
                categoria = gasto.categoria?.name,
                categoriaPersonalizadaId = gasto.categoriaPersonalizadaId
            )
        }
        return repositoryJson.encodeToString(dtos)
    }
}