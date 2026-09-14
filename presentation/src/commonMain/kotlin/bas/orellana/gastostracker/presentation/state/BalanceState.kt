package bas.orellana.gastostracker.presentation.state

import bas.orellana.gastostracker.domain.model.IngresoModel
import bas.orellana.gastostracker.domain.util.todayLocalDate

data class BalanceState(
    val ingresos: List<IngresoModel> = emptyList(),
    val isLoading: Boolean = false,
    val showAddIngresoDialog: Boolean = false,
    val showSettingsDialog: Boolean = false,
    val showMonthPicker: Boolean = false,
    val selectedMonth: Int = todayLocalDate().monthNumber,
    val selectedYear: Int = todayLocalDate().year,
    val totalIngresosMes: Double = 0.0,
    val totalGastosMes: Double = 0.0,
    val totalAhorroMes: Double = 0.0
)
