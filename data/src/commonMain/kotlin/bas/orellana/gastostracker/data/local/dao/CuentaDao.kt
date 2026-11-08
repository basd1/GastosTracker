package bas.orellana.gastostracker.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import bas.orellana.gastostracker.data.local.entity.CuentaConSaldoRow
import bas.orellana.gastostracker.data.local.entity.CuentaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CuentaDao {

    @Query(
        """
        SELECT c.*,
            c.saldo_inicial
            + COALESCE((SELECT SUM(i.monto) FROM ingresos i WHERE i.cuenta_id = c.id), 0)
            - COALESCE((SELECT SUM(g.monto) FROM gastos g WHERE g.cuenta_id = c.id), 0) AS saldo
        FROM cuentas c
        ORDER BY c.archivada ASC, c.rowid ASC
        """
    )
    fun observarConSaldo(): Flow<List<CuentaConSaldoRow>>

    @Upsert
    suspend fun guardar(cuenta: CuentaEntity)

    @Query("DELETE FROM cuentas WHERE id = :id")
    suspend fun eliminar(id: String)

    @Query("UPDATE gastos SET cuenta_id = NULL WHERE cuenta_id = :id")
    suspend fun desasignarGastos(id: String)

    @Query("UPDATE ingresos SET cuenta_id = NULL WHERE cuenta_id = :id")
    suspend fun desasignarIngresos(id: String)
}
