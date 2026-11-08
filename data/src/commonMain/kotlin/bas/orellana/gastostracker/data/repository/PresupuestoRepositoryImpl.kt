package bas.orellana.gastostracker.data.repository

import bas.orellana.gastostracker.data.legacy.MigradorDatosLegacy
import bas.orellana.gastostracker.data.local.dao.PresupuestoDao
import bas.orellana.gastostracker.data.local.toEntity
import bas.orellana.gastostracker.data.local.toModel
import bas.orellana.gastostracker.domain.model.Presupuesto
import bas.orellana.gastostracker.domain.repository.PresupuestoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PresupuestoRepositoryImpl(
    private val dao: PresupuestoDao,
    private val migrador: MigradorDatosLegacy
) : PresupuestoRepository {

    override fun getPresupuestos(): Flow<List<Presupuesto>> =
        migrador.despuesDeMigrar { dao.observarTodos() }
            .map { filas -> filas.mapNotNull { it.toModel() } }

    override suspend fun guardarPresupuesto(presupuesto: Presupuesto) {
        migrador.asegurarMigracion()
        dao.guardar(presupuesto.toEntity())
    }

    override suspend fun eliminarPresupuesto(clave: String) {
        migrador.asegurarMigracion()
        dao.eliminar(clave)
    }
}
