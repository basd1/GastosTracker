package bas.orellana.gastostracker.domain.usecase

import bas.orellana.gastostracker.domain.model.GastoModel
import bas.orellana.gastostracker.domain.repository.GastoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeGastoRepository : GastoRepository {
    private val state = MutableStateFlow<List<GastoModel>>(emptyList())

    override fun getGastos(): Flow<List<GastoModel>> = state

    override suspend fun addGasto(gasto: GastoModel) {
        state.value = state.value + gasto
    }

    override suspend fun updateGasto(gasto: GastoModel) {
        state.value = state.value.map { if (it.id == gasto.id) gasto else it }
    }

    override suspend fun deleteGasto(id: String) {
        state.value = state.value.filterNot { it.id == id }
    }

    override suspend fun deleteAllGastos() {
        state.value = emptyList()
    }
}
