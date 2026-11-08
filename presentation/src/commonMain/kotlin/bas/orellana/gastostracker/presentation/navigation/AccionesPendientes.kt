package bas.orellana.gastostracker.presentation.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Acciones que llegan de fuera de la app (atajo del icono, widget, notificación) y que la pantalla
 * de Gastos ejecuta en cuanto se muestra.
 */
class AccionesPendientes {
    private val _anadirGasto = MutableStateFlow(false)
    val anadirGasto: StateFlow<Boolean> = _anadirGasto.asStateFlow()

    fun pedirAnadirGasto() {
        _anadirGasto.value = true
    }

    /** true si había una petición de añadir gasto (y la borra). */
    fun consumirAnadirGasto(): Boolean {
        val habia = _anadirGasto.value
        _anadirGasto.value = false
        return habia
    }
}
