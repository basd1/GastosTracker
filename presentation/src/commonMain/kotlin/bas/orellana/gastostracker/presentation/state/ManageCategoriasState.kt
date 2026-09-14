package bas.orellana.gastostracker.presentation.state

import bas.orellana.gastostracker.domain.model.Categoria
import bas.orellana.gastostracker.domain.model.CategoriaPersonalizada

data class ManageCategoriasState(
    val nuevaCategoria: String = "",
    val colorSeleccionado: Long = Categoria.PALETA_COLORES_PERSONALIZADOS.first(),
    val editCategoria: CategoriaPersonalizada? = null,
    val editNombre: String = "",
    val editColor: Long = Categoria.PALETA_COLORES_PERSONALIZADOS.first()
) {
    fun reset() = copy(
        nuevaCategoria = "",
        colorSeleccionado = Categoria.PALETA_COLORES_PERSONALIZADOS.first(),
        editCategoria = null,
        editNombre = "",
        editColor = Categoria.PALETA_COLORES_PERSONALIZADOS.first()
    )
}