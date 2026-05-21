package bas.orellana.gastostracker.domain.repository

import bas.orellana.gastostracker.domain.model.IngresoModel
import kotlinx.coroutines.flow.Flow

interface IngresoRepository {
    fun getIngresos(): Flow<List<IngresoModel>>
    suspend fun addIngreso(ingreso: IngresoModel)
    suspend fun deleteIngreso(id: String)
}
