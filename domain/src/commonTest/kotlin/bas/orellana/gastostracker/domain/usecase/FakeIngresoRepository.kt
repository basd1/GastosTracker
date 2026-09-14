package bas.orellana.gastostracker.domain.usecase

import bas.orellana.gastostracker.domain.model.IngresoModel
import bas.orellana.gastostracker.domain.repository.IngresoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeIngresoRepository : IngresoRepository {
    private val state = MutableStateFlow<List<IngresoModel>>(emptyList())

    override fun getIngresos(): Flow<List<IngresoModel>> = state

    override suspend fun addIngreso(ingreso: IngresoModel) {
        state.value = state.value + ingreso
    }

    override suspend fun updateIngreso(ingreso: IngresoModel) {
        state.value = state.value.map { if (it.id == ingreso.id) ingreso else it }
    }

    override suspend fun deleteIngreso(id: String) {
        state.value = state.value.filterNot { it.id == id }
    }
}
