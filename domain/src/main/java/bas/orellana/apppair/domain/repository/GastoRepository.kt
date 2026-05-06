package bas.orellana.gastostracker.domain.repository

import bas.orellana.gastostracker.domain.model.GastoModel
import kotlinx.coroutines.flow.Flow

interface GastoRepository {
    fun getGastos(): Flow<List<GastoModel>>
    suspend fun addGasto(gasto: GastoModel)
    suspend fun deleteGasto(id: String)
}