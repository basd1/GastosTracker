package bas.orellana.gastostracker.data.repository

import androidx.room.immediateTransaction
import androidx.room.useWriterConnection
import bas.orellana.gastostracker.data.legacy.MigradorDatosLegacy
import bas.orellana.gastostracker.data.local.AppDatabase
import bas.orellana.gastostracker.data.local.toEntity
import bas.orellana.gastostracker.data.local.toModel
import bas.orellana.gastostracker.domain.model.Aportacion
import bas.orellana.gastostracker.domain.model.MetaAhorro
import bas.orellana.gastostracker.domain.model.ProgresoMeta
import bas.orellana.gastostracker.domain.repository.AhorroRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

class AhorroRepositoryImpl(
    private val db: AppDatabase,
    private val migrador: MigradorDatosLegacy
) : AhorroRepository {

    private val dao = db.ahorroDao()

    override fun getProgresoMetas(): Flow<List<ProgresoMeta>> =
        migrador.despuesDeMigrar { dao.observarMetasConAhorrado() }
            .map { filas -> filas.mapNotNull { fila -> fila.meta.toModel()?.let { ProgresoMeta(it, fila.ahorrado) } } }

    override fun getAportaciones(metaId: String): Flow<List<Aportacion>> =
        migrador.despuesDeMigrar { dao.observarAportaciones(metaId) }
            .map { filas -> filas.map { it.toModel() } }

    override fun getTodasLasAportaciones(): Flow<List<Aportacion>> =
        migrador.despuesDeMigrar { dao.observarTodasLasAportaciones() }
            .map { filas -> filas.map { it.toModel() } }

    override fun getTotalAhorrado(desde: LocalDate, hasta: LocalDate): Flow<Double> =
        migrador.despuesDeMigrar { dao.observarTotalAhorrado(desde, hasta) }

    override suspend fun guardarMeta(meta: MetaAhorro) {
        migrador.asegurarMigracion()
        dao.guardarMeta(meta.toEntity())
    }

    override suspend fun eliminarMeta(id: String) {
        migrador.asegurarMigracion()
        db.useWriterConnection { conexion ->
            conexion.immediateTransaction {
                dao.eliminarAportacionesDeMeta(id)
                dao.eliminarMeta(id)
            }
        }
    }

    override suspend fun guardarAportacion(aportacion: Aportacion) {
        migrador.asegurarMigracion()
        dao.guardarAportacion(aportacion.toEntity())
    }

    override suspend fun eliminarAportacion(id: String) {
        migrador.asegurarMigracion()
        dao.eliminarAportacion(id)
    }
}
