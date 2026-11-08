package bas.orellana.gastostracker.presentation.state

import bas.orellana.gastostracker.domain.model.Categoria
import bas.orellana.gastostracker.domain.model.GastoModel
import kotlinx.datetime.LocalDate

/**
 * Filtros de la lista de Gastos. Todo es opcional y se combina (Y): categorías (varias a la vez,
 * vale cualquiera de ellas), importe mínimo/máximo y un rango de fechas que sustituye al mes elegido.
 */
data class FiltroGastos(
    val categorias: Set<Categoria> = emptySet(),
    val personalizadas: Set<String> = emptySet(),
    val importeMin: Double? = null,
    val importeMax: Double? = null,
    val desde: LocalDate? = null,
    val hasta: LocalDate? = null
) {
    val hayCategorias: Boolean get() = categorias.isNotEmpty() || personalizadas.isNotEmpty()
    val hayImporte: Boolean get() = importeMin != null || importeMax != null

    /** Rango de fechas propio (solo cuenta si tiene las dos fechas). */
    val rangoFechas: ClosedRange<LocalDate>?
        get() {
            val inicio = desde ?: return null
            val fin = hasta ?: return null
            return if (inicio <= fin) inicio..fin else fin..inicio
        }

    val activo: Boolean get() = hayCategorias || hayImporte || rangoFechas != null

    /** Cuántos tipos de filtro hay puestos (para el contador del botón de filtros). */
    val numeroActivos: Int get() = listOf(hayCategorias, hayImporte, rangoFechas != null).count { it }

    fun alternarCategoria(categoria: Categoria) =
        copy(categorias = if (categoria in categorias) categorias - categoria else categorias + categoria)

    fun alternarPersonalizada(id: String) =
        copy(personalizadas = if (id in personalizadas) personalizadas - id else personalizadas + id)

    /** El rango de fechas no se comprueba aquí: los gastos ya llegan filtrados por fecha desde SQL. */
    fun acepta(gasto: GastoModel): Boolean =
        (!hayCategorias || gasto.categoria in categorias || gasto.categoriaPersonalizadaId in personalizadas) &&
            (importeMin == null || gasto.monto >= importeMin) &&
            (importeMax == null || gasto.monto <= importeMax)

    fun sinCategorias() = copy(categorias = emptySet(), personalizadas = emptySet())
}
