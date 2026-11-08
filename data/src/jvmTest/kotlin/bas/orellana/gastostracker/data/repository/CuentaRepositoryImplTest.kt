package bas.orellana.gastostracker.data.repository

import bas.orellana.gastostracker.data.EntornoDatos
import bas.orellana.gastostracker.domain.model.Cuenta
import bas.orellana.gastostracker.domain.model.GastoModel
import bas.orellana.gastostracker.domain.model.IngresoModel
import bas.orellana.gastostracker.domain.model.TipoCuenta
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CuentaRepositoryImplTest {

    private val entorno = EntornoDatos()
    private val repo = entorno.cuentas
    private val fecha = LocalDate(2026, 11, 8)

    @AfterTest
    fun cerrar() = entorno.close()

    @Test
    fun `el saldo es el inicial mas sus ingresos menos sus gastos`() = runTest {
        repo.guardarCuenta(Cuenta("banco", "Banco", TipoCuenta.BANCO, 1000.0, 0xFF64B5F6))
        repo.guardarCuenta(Cuenta("cartera", "Cartera", TipoCuenta.EFECTIVO, 50.0, 0xFF81C784))
        entorno.ingresos.addIngreso(IngresoModel("i", "Nómina", 1800.0, fecha, cuentaId = "banco"))
        entorno.gastos.addGasto(GastoModel("g1", "Alquiler", 700.0, fecha, cuentaId = "banco"))
        entorno.gastos.addGasto(GastoModel("g2", "Café", 2.5, fecha, cuentaId = "cartera"))
        entorno.gastos.addGasto(GastoModel("g3", "Sin cuenta", 99.0, fecha))

        val saldos = repo.getSaldos().first().associate { it.cuenta.id to it.saldo }

        assertEquals(2100.0, saldos.getValue("banco"))
        assertEquals(47.5, saldos.getValue("cartera"))
    }

    @Test
    fun `eliminar una cuenta deja sus movimientos sin cuenta`() = runTest {
        repo.guardarCuenta(Cuenta("banco", "Banco", color = 0xFF64B5F6))
        entorno.gastos.addGasto(GastoModel("g1", "Alquiler", 700.0, fecha, cuentaId = "banco"))

        repo.eliminarCuenta("banco")

        assertTrue(repo.getSaldos().first().isEmpty())
        assertNull(entorno.gastos.getGastos().first().single().cuentaId)
    }
}
