package bas.orellana.gastostracker.presentation

import bas.orellana.gastostracker.domain.usecase.GenerarRecurrentesUseCase
import bas.orellana.gastostracker.presentation.util.ejecutarSinFallar

/**
 * Lo que la app hace cada vez que pasa a primer plano (al abrirla o al volver a ella).
 * Todo es idempotente: se puede ejecutar las veces que haga falta.
 */
class TareasAlAbrir(
    private val generarRecurrentes: GenerarRecurrentesUseCase
) {
    suspend fun ejecutar() {
        ejecutarSinFallar("generar recurrentes") { generarRecurrentes() }
    }
}
