package bas.orellana.gastostracker.domain.usecase

import bas.orellana.gastostracker.domain.model.Cuenta
import bas.orellana.gastostracker.domain.model.SaldoCuenta
import bas.orellana.gastostracker.domain.repository.CuentaRepository
import kotlinx.coroutines.flow.Flow

class GetSaldosCuentasUseCase(private val repository: CuentaRepository) {
    operator fun invoke(): Flow<List<SaldoCuenta>> = repository.getSaldos()
}

class GuardarCuentaUseCase(private val repository: CuentaRepository) {
    suspend operator fun invoke(cuenta: Cuenta) {
        require(cuenta.nombre.isNotBlank()) { "La cuenta necesita un nombre" }
        repository.guardarCuenta(cuenta)
    }
}

class EliminarCuentaUseCase(private val repository: CuentaRepository) {
    suspend operator fun invoke(id: String) = repository.eliminarCuenta(id)
}
