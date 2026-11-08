package bas.orellana.gastostracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import bas.orellana.gastostracker.data.local.entity.AportacionEntity
import bas.orellana.gastostracker.data.local.entity.CategoriaPersonalizadaEntity
import bas.orellana.gastostracker.data.local.entity.GastoEntity
import bas.orellana.gastostracker.data.local.entity.IngresoEntity
import bas.orellana.gastostracker.data.local.entity.MetaAhorroEntity
import bas.orellana.gastostracker.data.local.entity.PresupuestoEntity
import bas.orellana.gastostracker.data.local.entity.RecurrenteEntity

/** Lectura y escritura de todas las tablas para la copia de seguridad (en orden de inserción). */
@Dao
interface CopiaSeguridadDao {

    @Query("SELECT * FROM gastos ORDER BY rowid ASC")
    suspend fun gastos(): List<GastoEntity>

    @Query("SELECT * FROM ingresos ORDER BY rowid ASC")
    suspend fun ingresos(): List<IngresoEntity>

    @Query("SELECT * FROM categorias_personalizadas ORDER BY rowid ASC")
    suspend fun categorias(): List<CategoriaPersonalizadaEntity>

    @Query("SELECT * FROM presupuestos ORDER BY rowid ASC")
    suspend fun presupuestos(): List<PresupuestoEntity>

    @Query("SELECT * FROM recurrentes ORDER BY rowid ASC")
    suspend fun recurrentes(): List<RecurrenteEntity>

    @Query("SELECT * FROM metas_ahorro ORDER BY rowid ASC")
    suspend fun metas(): List<MetaAhorroEntity>

    @Query("SELECT * FROM aportaciones ORDER BY rowid ASC")
    suspend fun aportaciones(): List<AportacionEntity>

    @Query("DELETE FROM gastos")
    suspend fun borrarGastos()

    @Query("DELETE FROM ingresos")
    suspend fun borrarIngresos()

    @Query("DELETE FROM categorias_personalizadas")
    suspend fun borrarCategorias()

    @Query("DELETE FROM presupuestos")
    suspend fun borrarPresupuestos()

    @Query("DELETE FROM recurrentes")
    suspend fun borrarRecurrentes()

    @Query("DELETE FROM metas_ahorro")
    suspend fun borrarMetas()

    @Query("DELETE FROM aportaciones")
    suspend fun borrarAportaciones()

    @Insert
    suspend fun insertarGastos(filas: List<GastoEntity>)

    @Insert
    suspend fun insertarIngresos(filas: List<IngresoEntity>)

    @Insert
    suspend fun insertarCategorias(filas: List<CategoriaPersonalizadaEntity>)

    @Insert
    suspend fun insertarPresupuestos(filas: List<PresupuestoEntity>)

    @Insert
    suspend fun insertarRecurrentes(filas: List<RecurrenteEntity>)

    @Insert
    suspend fun insertarMetas(filas: List<MetaAhorroEntity>)

    @Insert
    suspend fun insertarAportaciones(filas: List<AportacionEntity>)
}
