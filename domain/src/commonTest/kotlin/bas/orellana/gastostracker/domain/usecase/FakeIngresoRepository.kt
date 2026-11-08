package bas.orellana.gastostracker.domain.usecase

import bas.orellana.gastostracker.domain.model.IngresoModel
import bas.orellana.gastostracker.domain.repository.IngresoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

class FakeIngresoRepository : IngresoRepository {
    private val state = MutableStateFlow<List<IngresoModel>>(emptyList())

    override fun getIngresos(): Flow<List<IngresoModel>> = state

    override fun getIngresosEntre(desde: LocalDate, hasta: LocalDate): Flow<List<IngresoModel>> =
        state.map { ingresos -> ingresos.filter { it.fecha in desde..hasta } }

    override fun getTotalIngresos(desde: LocalDate, hasta: LocalDate): Flow<Double> =
        getIngresosEntre(desde, hasta).map { ingresos -> ingresos.sumOf { it.monto } }

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
