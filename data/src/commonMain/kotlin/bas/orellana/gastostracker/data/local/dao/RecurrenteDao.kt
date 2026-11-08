package bas.orellana.gastostracker.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import bas.orellana.gastostracker.data.local.entity.RecurrenteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurrenteDao {

    @Query("SELECT * FROM recurrentes ORDER BY tipo DESC, dia_del_mes ASC, nombre ASC")
    fun observarTodos(): Flow<List<RecurrenteEntity>>

    @Query("SELECT * FROM recurrentes")
    suspend fun todos(): List<RecurrenteEntity>

    @Upsert
    suspend fun guardar(recurrente: RecurrenteEntity)

    @Query("UPDATE recurrentes SET ultimo_mes_generado = :mes WHERE id = :id")
    suspend fun marcarGenerado(id: String, mes: String)

    @Query("DELETE FROM recurrentes WHERE id = :id")
    suspend fun eliminar(id: String)

    /** Pasa a otra categoría los recurrentes de [tipo] que usaban la categoría personalizada [idOrigen]. */
    @Query(
        """
        UPDATE recurrentes SET categoria = :categoria, categoria_personalizada_id = :categoriaPersonalizadaId
        WHERE categoria_personalizada_id = :idOrigen AND tipo = :tipo
        """
    )
    suspend fun reasignarCategoriaPersonalizada(idOrigen: String, tipo: String, categoria: String?, categoriaPersonalizadaId: String?)
}
