package bas.orellana.gastostracker.domain.model

/** Categoría a la que se pasan los gastos e ingresos de una categoría personalizada que se elimina. */
sealed interface CategoriaDestino {
    data class Predefinida(val categoria: Categoria) : CategoriaDestino
    data class Personalizada(val id: String) : CategoriaDestino

    companion object {
        val POR_DEFECTO: CategoriaDestino = Predefinida(Categoria.OTROS)
    }
}
