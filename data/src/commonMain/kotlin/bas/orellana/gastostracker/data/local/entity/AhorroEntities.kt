package bas.orellana.gastostracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlinx.datetime.LocalDate

@Serializable
@Entity(tableName = "metas_ahorro")
data class MetaAhorroEntity(
    @PrimaryKey val id: String,
    val nombre: String,
    val objetivo: Double?,
    @ColumnInfo(name = "fecha_objetivo") val fechaObjetivo: LocalDate?,
    val color: Long,
    val creada: LocalDate,
    val archivada: Boolean
)

/** Sin clave foránea: al borrar una meta se borran sus aportaciones en la misma transacción. */
@Serializable
@Entity(tableName = "aportaciones", indices = [Index("meta_id"), Index("fecha")])
data class AportacionEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "meta_id") val metaId: String,
    val monto: Double,
    val fecha: LocalDate,
    val nota: String?
)

/** Resultado de la consulta de metas con lo ahorrado en cada una. */
data class MetaConAhorradoRow(
    @Embedded val meta: MetaAhorroEntity,
    val ahorrado: Double
)
