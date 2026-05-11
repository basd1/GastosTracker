package bas.orellana.gastostracker.presentation.state

import bas.orellana.gastostracker.domain.model.Categoria

data class ManageCategoriasState(
    val nuevaCategoria: String = "",
    val colorSeleccionado: Long = Categoria.PALETA_COLORES_PERSONALIZADOS.first()
) {
    fun reset() = copy(
        nuevaCategoria = "",
        colorSeleccionado = Categoria.PALETA_COLORES_PERSONALIZADOS.first()
    )
}