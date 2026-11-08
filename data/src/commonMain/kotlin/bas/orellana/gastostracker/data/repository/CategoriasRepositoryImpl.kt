package bas.orellana.gastostracker.data.repository

import bas.orellana.gastostracker.data.legacy.MigradorDatosLegacy
import bas.orellana.gastostracker.data.local.dao.CategoriaPersonalizadaDao
import bas.orellana.gastostracker.data.local.entity.CategoriaPersonalizadaEntity
import bas.orellana.gastostracker.data.local.toEntity
import bas.orellana.gastostracker.data.local.toModel
import bas.orellana.gastostracker.domain.model.CategoriaPersonalizada
import bas.orellana.gastostracker.domain.repository.CategoriasRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class CategoriasRepositoryImpl(
    private val dao: CategoriaPersonalizadaDao,
    private val migrador: MigradorDatosLegacy
) : CategoriasRepository {

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

    override suspend fun deleteCategoriaPersonalizada(id: String) {
        migrador.asegurarMigracion()
        dao.eliminar(id)
    }
}
