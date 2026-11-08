package bas.orellana.gastostracker.presentation.state

import bas.orellana.gastostracker.domain.model.IngresoModel
import bas.orellana.gastostracker.domain.model.Mes

data class BalanceState(
    /** Ingresos del mes seleccionado. */
    val ingresos: List<IngresoModel> = emptyList(),
    val isLoading: Boolean = false,
    val showAddIngresoDialog: Boolean = false,
    val showMonthPicker: Boolean = false,
    val mes: Mes = Mes.actual(),
    val totalIngresosMes: Double = 0.0,
    val totalGastosMes: Double = 0.0,
    val totalAhorroMes: Double = 0.0,
    val diaInicioMes: Int = 1
) {
    val balanceMes: Double get() = totalIngresosMes - totalGastosMes - totalAhorroMes
}
