package bas.orellana.gastostracker.presentation.state

import bas.orellana.gastostracker.domain.model.Categoria
import bas.orellana.gastostracker.domain.model.CategoriaPersonalizada
import bas.orellana.gastostracker.domain.model.GastoModel
import bas.orellana.gastostracker.domain.model.Mes
import bas.orellana.gastostracker.domain.model.EstadoPresupuesto
import bas.orellana.gastostracker.domain.model.ResumenMensual

/** Una categoría en la que este mes se ha gastado más que el mes pasado. */
data class CategoryAlert(
    /** Identifica la categoría para poder descartar la alerta. */
    val clave: String,
    val categoria: Categoria?,
    val categoriaPersonalizada: CategoriaPersonalizada?,
    val gastoActual: Double,
    val gastoAnterior: Double
)
data class HomeState(
    val gastos: List<GastoModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showAddGastoDialog: Boolean = false,
    val alerts: List<CategoryAlert> = emptyList(),
    val dismissedAlertKeys: Set<String> = emptySet(),
    val searchQuery: String = "",
    val filtro: FiltroGastos = FiltroGastos(),
    val showFiltrosDialog: Boolean = false,
    val sortOrder: SortOrder = SortOrder.DATE_DESC,
    /** Mes que se ve en la pantalla de Gastos: la lista, el total y las alertas son de este mes. */
    val mes: Mes = Mes.actual(),
    val showMonthPicker: Boolean = false,
    /** Cómo van los presupuestos en el mes seleccionado. */
    val presupuestos: List<EstadoPresupuesto> = emptyList(),
    /** Día en que empieza el mes contable (Ajustes). */
    val diaInicioMes: Int = 1,
    /** Resumen del mes anterior, solo hasta que se descarta. */
    val resumenMensual: ResumenMensual? = null
) {
    val hayBusquedaOFiltro: Boolean get() = searchQuery.isNotBlank() || filtro.activo

    /** Los gastos que se ven en la lista: con la búsqueda y el filtro aplicados y en el orden elegido. */
    val gastosVisibles: List<GastoModel> by lazy { filtrarYOrdenarGastos(gastos, searchQuery, filtro, sortOrder) }
}

internal fun filtrarYOrdenarGastos(
    gastos: List<GastoModel>,
    busqueda: String,
    filtro: FiltroGastos,
    orden: SortOrder
): List<GastoModel> {
    val texto = busqueda.trim()
    val filtrados = gastos.filter { gasto ->
        (texto.isEmpty() || gasto.nombre.contains(texto, ignoreCase = true) || gasto.nota?.contains(texto, ignoreCase = true) == true) &&
            filtro.acepta(gasto)
    }
    // Las ordenaciones son estables: a igualdad, se mantiene el orden del repositorio.
    return when (orden) {
        SortOrder.DATE_DESC -> filtrados.sortedByDescending { it.fecha }
        SortOrder.DATE_ASC -> filtrados.sortedBy { it.fecha }
        SortOrder.AMOUNT_DESC -> filtrados.sortedByDescending { it.monto }
        SortOrder.AMOUNT_ASC -> filtrados.sortedBy { it.monto }
        SortOrder.NAME_ASC -> filtrados.sortedBy { it.nombre.lowercase() }
        SortOrder.NAME_DESC -> filtrados.sortedByDescending { it.nombre.lowercase() }
    }
}
