package bas.orellana.gastostracker.data.repository

import bas.orellana.gastostracker.data.legacy.MigradorDatosLegacy
import bas.orellana.gastostracker.data.local.dao.IngresoDao
import bas.orellana.gastostracker.data.local.toEntity
import bas.orellana.gastostracker.data.local.toModel
import bas.orellana.gastostracker.domain.model.IngresoModel
import bas.orellana.gastostracker.domain.repository.IngresoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

class IngresoRepositoryImpl(
    private val dao: IngresoDao,
    private val migrador: MigradorDatosLegacy
) : IngresoRepository {

    override fun getIngresos(): Flow<List<IngresoModel>> =
        migrador.despuesDeMigrar { dao.observarTodos() }
            .map { ingresos -> ingresos.map { it.toModel() } }

    override fun getIngresosEntre(desde: LocalDate, hasta: LocalDate): Flow<List<IngresoModel>> =
        migrador.despuesDeMigrar { dao.observarEntre(desde, hasta) }
            .map { ingresos -> ingresos.map { it.toModel() } }

    override fun getTotalIngresos(desde: LocalDate, hasta: LocalDate): Flow<Double> =
        migrador.despuesDeMigrar { dao.observarTotal(desde, hasta) }

    override suspend fun addIngreso(ingreso: IngresoModel) {
        migrador.asegurarMigracion()
        dao.insertar(ingreso.toEntity())
    }

    override suspend fun updateIngreso(ingreso: IngresoModel) {
        migrador.asegurarMigracion()
        dao.actualizar(ingreso.toEntity())
    }

    override suspend fun deleteIngreso(id: String) {
        migrador.asegurarMigracion()
        dao.eliminar(id)
    }
}
