package bas.orellana.gastostracker.domain.repository

import bas.orellana.gastostracker.domain.model.GastoModel
import bas.orellana.gastostracker.domain.model.TotalPorCategoria
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface GastoRepository {
    fun getGastos(): Flow<List<GastoModel>>
    fun getGastosEntre(desde: LocalDate, hasta: LocalDate): Flow<List<GastoModel>>
    fun getTotalesPorCategoria(desde: LocalDate, hasta: LocalDate): Flow<List<TotalPorCategoria>>
    suspend fun addGasto(gasto: GastoModel)
    suspend fun updateGasto(gasto: GastoModel)
    suspend fun deleteGasto(id: String)
    suspend fun deleteAllGastos()
}
