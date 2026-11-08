package bas.orellana.gastostracker.presentation

import bas.orellana.gastostracker.domain.usecase.GenerarRecurrentesUseCase
import bas.orellana.gastostracker.domain.usecase.GetGastosUseCase
import bas.orellana.gastostracker.presentation.util.AlmacenTickets
import bas.orellana.gastostracker.presentation.util.ejecutarSinFallar
import bas.orellana.gastostracker.presentation.util.nombreFotoTicket
import kotlinx.coroutines.flow.first
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Lo que la app hace cada vez que pasa a primer plano (al abrirla o al volver a ella).
 * Todo es idempotente: se puede ejecutar las veces que haga falta.
 */
@OptIn(ExperimentalTime::class)
class TareasAlAbrir(
    private val generarRecurrentes: GenerarRecurrentesUseCase,
    private val getGastos: GetGastosUseCase,
    private val almacenTickets: AlmacenTickets,
    private val ahoraMs: () -> Long = { Clock.System.now().toEpochMilliseconds() }
) {
    suspend fun ejecutar() {
        ejecutarSinFallar("generar recurrentes") { generarRecurrentes() }
        ejecutarSinFallar("borrar fotos sin gasto") { borrarFotosSinGasto() }
    }

    /**
     * Borra las fotos de tickets que ya no usa ningún gasto (gastos borrados, foto cambiada, diálogo
     * cancelado tras hacer la foto, copia restaurada…). Solo las de hace más de un día: así no se
     * pierde la foto de un gasto que se está escribiendo ni la de uno que se puede deshacer.
     */
    private suspend fun borrarFotosSinGasto() {
        // Primero las fotos y luego los gastos: una foto que aparezca entre medias no está en la lista.
        val fotos = almacenTickets.fotos()
        if (fotos.isEmpty()) return
        val usadas = getGastos().first().mapNotNullTo(HashSet()) { gasto -> gasto.fotoTicket?.let(::nombreFotoTicket) }
        val limite = ahoraMs() - ANTIGUEDAD_MINIMA_MS
        val sobrantes = fotos.filter { it.nombre !in usadas && it.modificadaMs < limite }.map { it.nombre }
        if (sobrantes.isNotEmpty()) almacenTickets.borrar(sobrantes)
    }

    private companion object {
        const val ANTIGUEDAD_MINIMA_MS = 24L * 60 * 60 * 1000
    }
}
