package bas.orellana.gastostracker.domain.repository

import bas.orellana.gastostracker.domain.model.Cuenta
import bas.orellana.gastostracker.domain.model.SaldoCuenta
import kotlinx.coroutines.flow.Flow

interface CuentaRepository {
    /** Cuentas con su saldo (calculado en SQL); las archivadas al final. */
    fun getSaldos(): Flow<List<SaldoCuenta>>

    suspend fun guardarCuenta(cuenta: Cuenta)

    /** Elimina la cuenta; sus movimientos se quedan, pero sin cuenta. */
    suspend fun eliminarCuenta(id: String)
}
