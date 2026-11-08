package bas.orellana.gastostracker.data.repository

import androidx.room.immediateTransaction
import androidx.room.useWriterConnection
import bas.orellana.gastostracker.data.legacy.MigradorDatosLegacy
import bas.orellana.gastostracker.data.local.AppDatabase
import bas.orellana.gastostracker.data.local.toEntity
import bas.orellana.gastostracker.data.local.toModel
import bas.orellana.gastostracker.domain.model.Cuenta
import bas.orellana.gastostracker.domain.model.SaldoCuenta
import bas.orellana.gastostracker.domain.repository.CuentaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CuentaRepositoryImpl(
    private val db: AppDatabase,
    private val migrador: MigradorDatosLegacy
) : CuentaRepository {

    private val dao = db.cuentaDao()

    override fun getSaldos(): Flow<List<SaldoCuenta>> =
        migrador.despuesDeMigrar { dao.observarConSaldo() }
            .map { filas -> filas.map { SaldoCuenta(it.cuenta.toModel(), it.saldo) } }

    override suspend fun guardarCuenta(cuenta: Cuenta) {
        migrador.asegurarMigracion()
        dao.guardar(cuenta.toEntity())
    }

    override suspend fun eliminarCuenta(id: String) {
        migrador.asegurarMigracion()
        db.useWriterConnection { conexion ->
            conexion.immediateTransaction {
                dao.desasignarGastos(id)
                dao.desasignarIngresos(id)
                dao.eliminar(id)
            }
        }
    }
}
