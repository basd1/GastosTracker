package bas.orellana.gastostracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import bas.orellana.gastostracker.data.local.entity.GastoEntity
import bas.orellana.gastostracker.data.local.entity.TotalPorCategoriaRow
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Dao
interface GastoDao {

    // rowid DESC deja primero el último añadido dentro del mismo día, como hacía la versión JSON.
    @Query("SELECT * FROM gastos ORDER BY fecha DESC, rowid DESC")
    fun observarTodos(): Flow<List<GastoEntity>>

    @Query("SELECT * FROM gastos WHERE fecha BETWEEN :desde AND :hasta ORDER BY fecha DESC, rowid DESC")
    fun observarEntre(desde: LocalDate, hasta: LocalDate): Flow<List<GastoEntity>>

    @Query(
        """
        SELECT categoria, categoria_personalizada_id, SUM(monto) AS total
        FROM gastos
        WHERE fecha BETWEEN :desde AND :hasta
        GROUP BY categoria, categoria_personalizada_id
        """
    )
    fun observarTotalesPorCategoria(desde: LocalDate, hasta: LocalDate): Flow<List<TotalPorCategoriaRow>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(gasto: GastoEntity)

    /** Solo para la migración: no pisa filas que ya existan. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarSiNoExisten(gastos: List<GastoEntity>)

    @Update
    suspend fun actualizar(gasto: GastoEntity)

    @Query("DELETE FROM gastos WHERE id = :id")
    suspend fun eliminar(id: String)

    @Query("SELECT COUNT(*) FROM gastos WHERE categoria_personalizada_id = :idCategoria")
    suspend fun contarConCategoriaPersonalizada(idCategoria: String): Int

    /** Pasa a otra categoría todo lo que usaba la categoría personalizada [idOrigen]. */
    @Query(
        """
        UPDATE gastos SET categoria = :categoria, categoria_personalizada_id = :categoriaPersonalizadaId
        WHERE categoria_personalizada_id = :idOrigen
        """
    )
    suspend fun reasignarCategoriaPersonalizada(idOrigen: String, categoria: String?, categoriaPersonalizadaId: String?)

    /** Pasa a [categoria] lo que apunta a una categoría personalizada que ya no existe. */
    @Query(
        """
        UPDATE gastos SET categoria = :categoria, categoria_personalizada_id = NULL
        WHERE categoria_personalizada_id IS NOT NULL
          AND categoria_personalizada_id NOT IN (SELECT id FROM categorias_personalizadas)
        """
    )
    suspend fun reasignarHuerfanos(categoria: String): Int

    @Query("DELETE FROM gastos")
    suspend fun eliminarTodos()
}
