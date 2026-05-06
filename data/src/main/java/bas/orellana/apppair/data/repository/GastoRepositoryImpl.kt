package bas.orellana.apppair.data.repository

import bas.orellana.apppair.data.cache.GastoCache
import bas.orellana.apppair.domain.model.GastoModel
import bas.orellana.apppair.domain.repository.GastoRepository
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