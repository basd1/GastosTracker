package bas.orellana.gastostracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlinx.datetime.LocalDate

@Serializable
@Entity(tableName = "gastos", indices = [Index("fecha")])
data class GastoEntity(
    @PrimaryKey val id: String,
    val nombre: String,
    val monto: Double,
    val fecha: LocalDate,
    /** Nombre del enum [bas.orellana.gastostracker.domain.model.Categoria]. */
    val categoria: String?,
    @ColumnInfo(name = "categoria_personalizada_id") val categoriaPersonalizadaId: String?,
    // Columnas añadidas después: con valor por defecto para poder leer copias de seguridad antiguas.
    val nota: String? = null,
    @ColumnInfo(name = "metodo_pago") val metodoPago: String? = null
)
