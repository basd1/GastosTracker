package bas.orellana.gastostracker.data.repository

import androidx.room.immediateTransaction
import androidx.room.useWriterConnection
import bas.orellana.gastostracker.data.legacy.MigradorDatosLegacy
import bas.orellana.gastostracker.data.local.AppDatabase
import bas.orellana.gastostracker.data.local.entity.CategoriaPersonalizadaEntity
import bas.orellana.gastostracker.data.local.toEntity
import bas.orellana.gastostracker.data.local.toModel
import bas.orellana.gastostracker.domain.model.CategoriaDestino
import bas.orellana.gastostracker.domain.model.CategoriaPersonalizada
import bas.orellana.gastostracker.domain.model.Presupuesto
import bas.orellana.gastostracker.domain.model.CategoriaIngreso
import bas.orellana.gastostracker.domain.model.TipoMovimiento
import bas.orellana.gastostracker.domain.repository.CategoriasRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class CategoriasRepositoryImpl(
    private val db: AppDatabase,
    private val migrador: MigradorDatosLegacy
) : CategoriasRepository {

    private val dao = db.categoriaPersonalizadaDao()

    override fun getCategoriasPersonalizadas(): Flow<List<CategoriaPersonalizada>> =
        migrador.despuesDeMigrar { dao.observarTodas() }
            .map { categorias -> categorias.map { it.toModel() } }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun addCategoriaPersonalizada(nombre: String, color: Long) {
        migrador.asegurarMigracion()
        // UUID en lugar de "máximo + 1": no puede chocar con ids de categorías antiguas.
        dao.insertar(
            CategoriaPersonalizadaEntity(
                id = Uuid.random().toString(),
                nombre = nombre,
                color = color
            )
        )
    }

    override suspend fun updateCategoriaPersonalizada(categoria: CategoriaPersonalizada) {
        migrador.asegurarMigracion()
        dao.actualizar(categoria.toEntity())
    }

    override suspend fun contarMovimientos(id: String): Int {
        migrador.asegurarMigracion()
        return db.gastoDao().contarConCategoriaPersonalizada(id) +
            db.ingresoDao().contarConCategoriaPersonalizada(id)
    }

    override suspend fun deleteCategoriaPersonalizada(id: String, destino: CategoriaDestino) {
        migrador.asegurarMigracion()
        val (categoria, categoriaPersonalizadaId) = when (destino) {
            is CategoriaDestino.Predefinida -> destino.categoria.name to null
            is CategoriaDestino.Personalizada -> null to destino.id
        }
        // Los ingresos tienen sus propias categorías: una de gasto se traduce a la equivalente (u "Otros").
        val categoriaIngreso = (destino as? CategoriaDestino.Predefinida)?.let { CategoriaIngreso.equivalente(it.categoria).name }
        // Todo o nada: si algo falla, la categoría y sus movimientos se quedan como estaban.
        db.useWriterConnection { conexion ->
            conexion.immediateTransaction {
                db.gastoDao().reasignarCategoriaPersonalizada(id, categoria, categoriaPersonalizadaId)
                db.ingresoDao().reasignarCategoriaPersonalizada(id, categoriaIngreso, categoriaPersonalizadaId)
                db.recurrenteDao().reasignarCategoriaPersonalizada(id, TipoMovimiento.GASTO.name, categoria, categoriaPersonalizadaId)
                db.recurrenteDao().reasignarCategoriaPersonalizada(id, TipoMovimiento.INGRESO.name, categoriaIngreso, categoriaPersonalizadaId)
                db.presupuestoDao().eliminar(Presupuesto.claveDe(null, id))
                dao.eliminar(id)
            }
        }
    }
}
