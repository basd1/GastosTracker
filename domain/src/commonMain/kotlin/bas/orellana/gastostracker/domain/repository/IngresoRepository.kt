package bas.orellana.gastostracker.domain.repository

import bas.orellana.gastostracker.domain.model.IngresoModel
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface IngresoRepository {
    fun getIngresos(): Flow<List<IngresoModel>>
    fun getIngresosEntre(desde: LocalDate, hasta: LocalDate): Flow<List<IngresoModel>>
    fun getTotalIngresos(desde: LocalDate, hasta: LocalDate): Flow<Double>
    suspend fun addIngreso(ingreso: IngresoModel)
    suspend fun updateIngreso(ingreso: IngresoModel)
    suspend fun deleteIngreso(id: String)
}
