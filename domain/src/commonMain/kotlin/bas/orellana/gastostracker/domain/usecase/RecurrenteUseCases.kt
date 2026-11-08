package bas.orellana.gastostracker.domain.usecase

import bas.orellana.gastostracker.domain.model.Recurrente
import bas.orellana.gastostracker.domain.repository.RecurrenteRepository
import bas.orellana.gastostracker.domain.util.todayLocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

class GetRecurrentesUseCase(private val repository: RecurrenteRepository) {
    operator fun invoke(): Flow<List<Recurrente>> = repository.getRecurrentes()
}

class GuardarRecurrenteUseCase(private val repository: RecurrenteRepository) {
    suspend operator fun invoke(recurrente: Recurrente) {
        require(recurrente.monto > 0) { "El importe tiene que ser mayor que 0" }
        require(recurrente.nombre.isNotBlank()) { "El recurrente necesita un nombre" }
        repository.guardarRecurrente(recurrente)
    }
}

class EliminarRecurrenteUseCase(private val repository: RecurrenteRepository) {
    suspend operator fun invoke(id: String) = repository.eliminarRecurrente(id)
}

/** Crea los movimientos recurrentes que tocan hasta hoy. Se puede llamar las veces que se quiera. */
class GenerarRecurrentesUseCase(private val repository: RecurrenteRepository) {
    suspend operator fun invoke(hoy: LocalDate = todayLocalDate()): Int = repository.generarPendientes(hoy)
}
