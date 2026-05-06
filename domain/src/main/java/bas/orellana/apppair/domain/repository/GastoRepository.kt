package bas.orellana.apppair.domain.repository

import bas.orellana.apppair.domain.model.GastoModel
import kotlinx.coroutines.flow.Flow

interface GastoRepository {
    fun getGastos(): Flow<List<GastoModel>>
    suspend fun addGasto(gasto: GastoModel)
    suspend fun deleteGasto(id: String)
}