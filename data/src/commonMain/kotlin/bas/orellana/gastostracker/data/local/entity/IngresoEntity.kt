package bas.orellana.gastostracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate

@Entity(tableName = "ingresos", indices = [Index("fecha")])
data class IngresoEntity(
    @PrimaryKey val id: String,
    val nombre: String,
    val monto: Double,
    val fecha: LocalDate,
    /** Nombre del enum [bas.orellana.gastostracker.domain.model.Categoria]. */
    val categoria: String?,
    @ColumnInfo(name = "categoria_personalizada_id") val categoriaPersonalizadaId: String?
)
