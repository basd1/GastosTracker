package bas.orellana.gastostracker.domain.model

enum class TipoCuenta { BANCO, EFECTIVO, TARJETA_CREDITO, OTRA }

/**
 * Donde está el dinero (cuenta del banco, efectivo, tarjeta de crédito...). Asignar una cuenta a un
 * gasto o ingreso es opcional; el saldo solo tiene en cuenta los movimientos asignados a ella.
 */
data class Cuenta(
    val id: String,
    val nombre: String,
    val tipo: TipoCuenta = TipoCuenta.BANCO,
    val saldoInicial: Double = 0.0,
    val color: Long,
    val archivada: Boolean = false
)

/** Saldo actual de una cuenta: saldo inicial + sus ingresos − sus gastos. */
data class SaldoCuenta(val cuenta: Cuenta, val saldo: Double)
