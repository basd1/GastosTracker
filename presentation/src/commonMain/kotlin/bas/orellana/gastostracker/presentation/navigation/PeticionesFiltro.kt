package bas.orellana.gastostracker.presentation.navigation

import bas.orellana.gastostracker.domain.model.Mes
import bas.orellana.gastostracker.presentation.state.FiltroGastos
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Abrir la lista de Gastos con un filtro ya puesto, y opcionalmente en un mes concreto. */
data class PeticionFiltro(val filtro: FiltroGastos, val mes: Mes? = null)

/**
 * Buzón para pedir desde otra pantalla (p. ej. al pulsar una categoría del gráfico) que la lista de
 * Gastos se abra filtrada. Solo lo consume la pantalla de Gastos, así que da igual cuántos
 * ViewModels de Gastos existan en la pila de navegación.
 */
class PeticionesFiltro {
    private val _pendiente = MutableStateFlow<PeticionFiltro?>(null)
    val pendiente: StateFlow<PeticionFiltro?> = _pendiente.asStateFlow()

    fun pedir(peticion: PeticionFiltro) {
        _pendiente.value = peticion
    }

    /** Devuelve la petición pendiente (si hay) y la borra. */
    fun consumir(): PeticionFiltro? {
        val peticion = _pendiente.value
        _pendiente.value = null
        return peticion
    }
}
