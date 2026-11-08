package bas.orellana.gastostracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categorias_personalizadas")
data class CategoriaPersonalizadaEntity(
    @PrimaryKey val id: String,
    val nombre: String,
    val color: Long
)
