package bas.orellana.gastostracker.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import bas.orellana.gastostracker.data.local.entity.PresupuestoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PresupuestoDao {

    @Query("SELECT * FROM presupuestos ORDER BY rowid ASC")
    fun observarTodos(): Flow<List<PresupuestoEntity>>

    @Upsert
    suspend fun guardar(presupuesto: PresupuestoEntity)

    @Query("DELETE FROM presupuestos WHERE id = :id")
    suspend fun eliminar(id: String)
}
