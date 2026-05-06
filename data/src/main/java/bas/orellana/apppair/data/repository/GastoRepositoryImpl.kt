package bas.orellana.gastostracker.data.repository

import bas.orellana.gastostracker.data.cache.GastoCache
import bas.orellana.gastostracker.domain.model.GastoModel
import bas.orellana.gastostracker.domain.repository.GastoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GastoRepositoryImpl : GastoRepository {

    override fun getGastos(): Flow<List<GastoModel>> = flow {
        emit(GastoCache.getAll())
    }

    override suspend fun addGasto(gasto: GastoModel) {
        GastoCache.add(gasto)
    }

    override suspend fun deleteGasto(id: String) {
        GastoCache.delete(id)
    }
}