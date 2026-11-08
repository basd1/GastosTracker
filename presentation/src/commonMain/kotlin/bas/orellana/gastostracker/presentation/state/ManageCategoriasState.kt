package bas.orellana.gastostracker.presentation.state

import bas.orellana.gastostracker.domain.model.Categoria
import bas.orellana.gastostracker.domain.model.CategoriaDestino
import bas.orellana.gastostracker.domain.model.CategoriaPersonalizada

data class ManageCategoriasState(
    val nuevaCategoria: String = "",
    val colorSeleccionado: Long = Categoria.PALETA_COLORES_PERSONALIZADOS.first(),
    val editCategoria: CategoriaPersonalizada? = null,
    val editNombre: String = "",
    val editColor: Long = Categoria.PALETA_COLORES_PERSONALIZADOS.first(),
    /** Categoría que se quiere borrar y que todavía tiene movimientos: hay que preguntar a dónde pasarlos. */
    val categoriaAEliminar: CategoriaPersonalizada? = null,
    val movimientosCategoriaAEliminar: Int = 0,
    val destinoReasignacion: CategoriaDestino = CategoriaDestino.POR_DEFECTO
)
