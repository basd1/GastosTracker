package bas.orellana.gastostracker.presentation.state

data class AddIngresoState(
    val concepto: String = "",
    val monto: String = ""
) {
    fun reset() = copy(
        concepto = "",
        monto = ""
    )
}
