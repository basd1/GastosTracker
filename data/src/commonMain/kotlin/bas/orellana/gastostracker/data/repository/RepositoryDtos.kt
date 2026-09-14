package bas.orellana.gastostracker.data.repository

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

internal val repositoryJson = Json {
    ignoreUnknownKeys = true
}

@Serializable
internal data class GastoDto(
    val id: String,
    val nombre: String,
    val monto: Double,
    val fecha: String,
    val categoria: String? = null,
    val categoriaPersonalizadaId: String? = null
)

@Serializable
internal data class IngresoDto(
    val id: String,
    val nombre: String,
    val monto: Double,
    val fecha: String,
    val categoria: String? = null,
    val categoriaPersonalizadaId: String? = null
)

@Serializable
internal data class CategoriaPersonalizadaDto(
    val id: String,
    val nombre: String,
    val color: Long? = null
)
