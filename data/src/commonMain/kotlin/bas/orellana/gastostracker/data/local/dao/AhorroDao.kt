package bas.orellana.gastostracker.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import bas.orellana.gastostracker.data.local.entity.AportacionEntity
import bas.orellana.gastostracker.data.local.entity.MetaAhorroEntity
import bas.orellana.gastostracker.data.local.entity.MetaConAhorradoRow
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Dao
interface AhorroDao {

    @Query(
        """
        SELECT m.*, COALESCE(SUM(a.monto), 0) AS ahorrado
        FROM metas_ahorro m LEFT JOIN aportaciones a ON a.meta_id = m.id
        GROUP BY m.id
        ORDER BY m.archivada ASC, m.rowid ASC
        """
    )
    fun observarMetasConAhorrado(): Flow<List<MetaConAhorradoRow>>

    @Query("SELECT * FROM aportaciones WHERE meta_id = :metaId ORDER BY fecha DESC, rowid DESC")
    fun observarAportaciones(metaId: String): Flow<List<AportacionEntity>>

    @Query("SELECT * FROM aportaciones ORDER BY fecha ASC, rowid ASC")
    fun observarTodasLasAportaciones(): Flow<List<AportacionEntity>>

    @Query("SELECT COALESCE(SUM(monto), 0) FROM aportaciones WHERE fecha BETWEEN :desde AND :hasta")
    fun observarTotalAhorrado(desde: LocalDate, hasta: LocalDate): Flow<Double>

    @Upsert
    suspend fun guardarMeta(meta: MetaAhorroEntity)

    @Query("DELETE FROM metas_ahorro WHERE id = :id")
    suspend fun eliminarMeta(id: String)

    @Query("DELETE FROM aportaciones WHERE meta_id = :metaId")
    suspend fun eliminarAportacionesDeMeta(metaId: String)

    @Upsert
    suspend fun guardarAportacion(aportacion: AportacionEntity)

    @Query("DELETE FROM aportaciones WHERE id = :id")
    suspend fun eliminarAportacion(id: String)
}
