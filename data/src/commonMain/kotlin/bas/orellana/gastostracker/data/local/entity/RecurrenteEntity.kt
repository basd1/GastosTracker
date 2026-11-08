package bas.orellana.gastostracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlinx.datetime.LocalDate

@Serializable
@Entity(tableName = "recurrentes")
data class RecurrenteEntity(
    @PrimaryKey val id: String,
    /** Nombre del enum [bas.orellana.gastostracker.domain.model.TipoMovimiento]. */
    val tipo: String,
    val nombre: String,
    val monto: Double,
    val categoria: String?,
    @ColumnInfo(name = "categoria_personalizada_id") val categoriaPersonalizadaId: String?,
    @ColumnInfo(name = "dia_del_mes") val diaDelMes: Int,
    @ColumnInfo(name = "fecha_inicio") val fechaInicio: LocalDate,
    /** `2026-11` (ver [bas.orellana.gastostracker.domain.model.Mes.toString]). */
    @ColumnInfo(name = "ultimo_mes_generado") val ultimoMesGenerado: String?,
    val activo: Boolean
)
