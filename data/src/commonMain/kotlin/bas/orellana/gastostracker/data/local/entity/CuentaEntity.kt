package bas.orellana.gastostracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "cuentas")
data class CuentaEntity(
    @PrimaryKey val id: String,
    val nombre: String,
    /** Nombre del enum [bas.orellana.gastostracker.domain.model.TipoCuenta]. */
    val tipo: String,
    @ColumnInfo(name = "saldo_inicial") val saldoInicial: Double,
    val color: Long,
    val archivada: Boolean
)

data class CuentaConSaldoRow(
    @Embedded val cuenta: CuentaEntity,
    val saldo: Double
)
