package bas.orellana.gastostracker.data.repository

import androidx.room.immediateTransaction
import androidx.room.useWriterConnection
import bas.orellana.gastostracker.data.legacy.MigradorDatosLegacy
import bas.orellana.gastostracker.data.local.AppDatabase
import bas.orellana.gastostracker.data.local.toEntity
import bas.orellana.gastostracker.data.local.toModel
import bas.orellana.gastostracker.domain.model.Recurrente
import bas.orellana.gastostracker.domain.model.TipoMovimiento
import bas.orellana.gastostracker.domain.model.gastoEn
import bas.orellana.gastostracker.domain.model.ingresoEn
import bas.orellana.gastostracker.domain.repository.RecurrenteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.LocalDate

class RecurrenteRepositoryImpl(
    private val db: AppDatabase,
    private val migrador: MigradorDatosLegacy
) : RecurrenteRepository {

    private val dao = db.recurrenteDao()

    /** Evita que dos llamadas simultáneas (p. ej. al volver a la app) generen a la vez. */
    private val generando = Mutex()

    override fun getRecurrentes(): Flow<List<Recurrente>> =
        migrador.despuesDeMigrar { dao.observarTodos() }
            .map { filas -> filas.mapNotNull { it.toModel() } }

    override suspend fun guardarRecurrente(recurrente: Recurrente) {
        migrador.asegurarMigracion()
        dao.guardar(recurrente.toEntity())
    }

    override suspend fun eliminarRecurrente(id: String) {
        migrador.asegurarMigracion()
        dao.eliminar(id)
    }

    override suspend fun generarPendientes(hoy: LocalDate): Int = generando.withLock {
        migrador.asegurarMigracion()
        var creados = 0
        db.useWriterConnection { conexion ->
            conexion.immediateTransaction {
                dao.todos().mapNotNull { it.toModel() }.forEach { recurrente ->
                    val generacion = recurrente.ocurrenciasPendientes(hoy)
                    when (recurrente.tipo) {
                        // IGNORE: si el movimiento de ese mes ya existe (mismo id), no se pisa.
                        TipoMovimiento.GASTO ->
                            db.gastoDao().insertarSiNoExisten(generacion.meses.map { recurrente.gastoEn(it).toEntity() })
                        TipoMovimiento.INGRESO ->
                            db.ingresoDao().insertarSiNoExisten(generacion.meses.map { recurrente.ingresoEn(it).toEntity() })
                    }
                    creados += generacion.meses.size
                    val ultimo = generacion.ultimoMesRevisado
                    if (ultimo != null && ultimo != recurrente.ultimoMesGenerado) {
                        dao.marcarGenerado(recurrente.id, ultimo.toString())
                    }
                }
            }
        }
        creados
    }
}
