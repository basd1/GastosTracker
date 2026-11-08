package bas.orellana.gastostracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import bas.orellana.gastostracker.data.local.entity.IngresoEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Dao
interface IngresoDao {

    @Query("SELECT * FROM ingresos ORDER BY fecha DESC, rowid DESC")
    fun observarTodos(): Flow<List<IngresoEntity>>

    @Query("SELECT * FROM ingresos WHERE fecha BETWEEN :desde AND :hasta ORDER BY fecha DESC, rowid DESC")
    fun observarEntre(desde: LocalDate, hasta: LocalDate): Flow<List<IngresoEntity>>

    @Query("SELECT COALESCE(SUM(monto), 0) FROM ingresos WHERE fecha BETWEEN :desde AND :hasta")
    fun observarTotal(desde: LocalDate, hasta: LocalDate): Flow<Double>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(ingreso: IngresoEntity)

    /** Solo para la migración: no pisa filas que ya existan. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarSiNoExisten(ingresos: List<IngresoEntity>)

    @Update
    suspend fun actualizar(ingreso: IngresoEntity)

    @Query("DELETE FROM ingresos WHERE id = :id")
    suspend fun eliminar(id: String)
}
