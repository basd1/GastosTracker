package bas.orellana.gastostracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import bas.orellana.gastostracker.data.local.entity.CategoriaPersonalizadaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoriaPersonalizadaDao {

    // Orden de creación, como en la versión JSON.
    @Query("SELECT * FROM categorias_personalizadas ORDER BY rowid ASC")
    fun observarTodas(): Flow<List<CategoriaPersonalizadaEntity>>

    @Insert
    suspend fun insertar(categoria: CategoriaPersonalizadaEntity)

    /** Solo para la migración: no pisa filas que ya existan. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarSiNoExisten(categorias: List<CategoriaPersonalizadaEntity>)

    @Update
    suspend fun actualizar(categoria: CategoriaPersonalizadaEntity)

    @Query("DELETE FROM categorias_personalizadas WHERE id = :id")
    suspend fun eliminar(id: String)
}
