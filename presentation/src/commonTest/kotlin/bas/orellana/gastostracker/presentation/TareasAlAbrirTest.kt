package bas.orellana.gastostracker.presentation

import bas.orellana.gastostracker.domain.model.GastoModel
import bas.orellana.gastostracker.domain.usecase.GenerarRecurrentesUseCase
import bas.orellana.gastostracker.domain.usecase.GetGastosUseCase
import bas.orellana.gastostracker.domain.util.todayLocalDate
import bas.orellana.gastostracker.presentation.util.AlmacenTickets
import bas.orellana.gastostracker.presentation.util.FotoGuardada
import bas.orellana.gastostracker.presentation.viewmodel.FakeGastoRepository
import bas.orellana.gastostracker.presentation.viewmodel.FakeIngresoRepository
import bas.orellana.gastostracker.presentation.viewmodel.FakeRecurrenteRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class TareasAlAbrirTest {

    private val gastos = FakeGastoRepository()
    private val almacen = FakeAlmacenTickets()
    private val ahora = 10L * DIA

    private val tareas = TareasAlAbrir(
        generarRecurrentes = GenerarRecurrentesUseCase(FakeRecurrenteRepository(gastos, FakeIngresoRepository())),
        getGastos = GetGastosUseCase(gastos),
        almacenTickets = almacen,
        ahoraMs = { ahora }
    )

    private fun gastoConFoto(id: String, ruta: String) =
        GastoModel(id, "Gasto $id", 10.0, todayLocalDate(), null, null, fotoTicket = ruta)

    @Test
    fun `borra las fotos antiguas que no usa ningun gasto y deja el resto`() = runTest {
        gastos.datos.value = listOf(
            gastoConFoto("a", "/data/user/0/app/files/tickets/usada.jpg"),
            // Ruta de otro móvil (copia restaurada): cuenta por el nombre del fichero.
            gastoConFoto("b", "/data/user/10/otra/files/tickets/restaurada.jpg")
        )
        almacen.guardadas += listOf(
            FotoGuardada("usada.jpg", ahora - 5 * DIA),
            FotoGuardada("restaurada.jpg", ahora - 5 * DIA),
            FotoGuardada("huerfana.jpg", ahora - 2 * DIA),
            FotoGuardada("recien-hecha.jpg", ahora - 60_000)
        )

        tareas.ejecutar()

        assertEquals(
            setOf("usada.jpg", "restaurada.jpg", "recien-hecha.jpg"),
            almacen.guardadas.map { it.nombre }.toSet()
        )
    }

    private class FakeAlmacenTickets : AlmacenTickets {
        val guardadas = mutableListOf<FotoGuardada>()
        override suspend fun fotos(): List<FotoGuardada> = guardadas.toList()
        override suspend fun borrar(nombres: Collection<String>) {
            guardadas.removeAll { it.nombre in nombres }
        }
        override suspend fun instalarFotosRestauradas() = Unit
    }

    private companion object {
        const val DIA = 24L * 60 * 60 * 1000
    }
}
