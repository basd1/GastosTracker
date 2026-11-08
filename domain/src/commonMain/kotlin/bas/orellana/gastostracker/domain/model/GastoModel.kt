package bas.orellana.gastostracker.domain.model

import kotlinx.datetime.LocalDate

data class GastoModel(
    val id: String,
    val nombre: String,
    val monto: Double,
    val fecha: LocalDate,
    val categoria: Categoria? = null,
    val categoriaPersonalizadaId: String? = null,
    val nota: String? = null,
    val metodoPago: MetodoPago? = null,
    val cuentaId: String? = null,
    /** Ruta de la foto del ticket en el almacenamiento privado de la app. */
    val fotoTicket: String? = null
)
