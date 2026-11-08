package bas.orellana.gastostracker.domain.usecase

import bas.orellana.gastostracker.domain.model.EstadoPresupuesto
import bas.orellana.gastostracker.domain.model.Presupuesto
import bas.orellana.gastostracker.domain.model.calcularEstadoPresupuestos
import bas.orellana.gastostracker.domain.repository.GastoRepository
import bas.orellana.gastostracker.domain.repository.PresupuestoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.LocalDate

class GetPresupuestosUseCase(private val repository: PresupuestoRepository) {
    operator fun invoke(): Flow<List<Presupuesto>> = repository.getPresupuestos()
}

class GuardarPresupuestoUseCase(private val repository: PresupuestoRepository) {
    suspend operator fun invoke(presupuesto: Presupuesto) {
        require(presupuesto.monto > 0) { "El presupuesto tiene que ser mayor que 0" }
        repository.guardarPresupuesto(presupuesto)
    }
}

class EliminarPresupuestoUseCase(private val repository: PresupuestoRepository) {
    suspend operator fun invoke(clave: String) = repository.eliminarPresupuesto(clave)
}

/** Estado de cada presupuesto en el periodo [desde]..[hasta], con los totales calculados en SQL. */
class GetEstadoPresupuestosUseCase(
    private val presupuestos: PresupuestoRepository,
    private val gastos: GastoRepository
) {
    operator fun invoke(desde: LocalDate, hasta: LocalDate): Flow<List<EstadoPresupuesto>> =
        combine(presupuestos.getPresupuestos(), gastos.getTotalesPorCategoria(desde, hasta)) { lista, totales ->
            calcularEstadoPresupuestos(lista, totales)
        }
}
