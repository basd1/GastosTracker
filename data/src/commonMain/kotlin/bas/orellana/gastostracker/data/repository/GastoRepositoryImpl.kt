package bas.orellana.gastostracker.data.repository

import bas.orellana.gastostracker.data.legacy.MigradorDatosLegacy
import bas.orellana.gastostracker.data.local.dao.GastoDao
import bas.orellana.gastostracker.data.local.toEntity
import bas.orellana.gastostracker.data.local.toModel
import bas.orellana.gastostracker.domain.model.GastoModel
import bas.orellana.gastostracker.domain.model.TotalPorCategoria
import bas.orellana.gastostracker.domain.repository.GastoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

class GastoRepositoryImpl(
    private val dao: GastoDao,
    private val migrador: MigradorDatosLegacy
) : GastoRepository {

    override fun getGastos(): Flow<List<GastoModel>> =
        migrador.despuesDeMigrar { dao.observarTodos() }
            .map { gastos -> gastos.map { it.toModel() } }

    override fun getGastosEntre(desde: LocalDate, hasta: LocalDate): Flow<List<GastoModel>> =
        migrador.despuesDeMigrar { dao.observarEntre(desde, hasta) }
            .map { gastos -> gastos.map { it.toModel() } }

    override fun getTotalesPorCategoria(desde: LocalDate, hasta: LocalDate): Flow<List<TotalPorCategoria>> =
        migrador.despuesDeMigrar { dao.observarTotalesPorCategoria(desde, hasta) }
            .map { filas -> filas.map { it.toModel() } }

    override suspend fun addGasto(gasto: GastoModel) {
        migrador.asegurarMigracion()
        dao.insertar(gasto.toEntity())
    }

    override suspend fun updateGasto(gasto: GastoModel) {
        migrador.asegurarMigracion()
        dao.actualizar(gasto.toEntity())
    }

    override suspend fun deleteGasto(id: String) {
        migrador.asegurarMigracion()
        dao.eliminar(id)
    }

    override suspend fun deleteAllGastos() {
        migrador.asegurarMigracion()
        dao.eliminarTodos()
    }
}
