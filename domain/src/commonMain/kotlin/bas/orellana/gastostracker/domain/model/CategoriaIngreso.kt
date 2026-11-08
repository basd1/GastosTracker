package bas.orellana.gastostracker.domain.model

/**
 * Categorías predefinidas de los ingresos (los gastos usan [Categoria]). El nombre visible vive en
 * los recursos de texto de `:presentation`. `REGALOS` y `OTROS` se llaman igual que en [Categoria]
 * a propósito: así los ingresos antiguos con esas categorías las conservan.
 */
enum class CategoriaIngreso(val color: Long) {
    NOMINA(0xFF66BB6A),
    FREELANCE(0xFF42A5F5),
    VENTAS(0xFFFFA726),
    REEMBOLSOS(0xFF26C6DA),
    INTERESES(0xFFAB47BC),
    REGALOS(0xFFF06292),
    OTROS(0xFF90A4AE);

    companion object {
        /** A dónde van los ingresos con una categoría que no es de ingreso. */
        val POR_DEFECTO = OTROS

        fun desdeNombre(nombre: String?): CategoriaIngreso? = nombre?.let { n -> entries.find { it.name == n } }

        /** Categoría de ingreso equivalente a una de gasto (misma clave) o [POR_DEFECTO]. */
        fun equivalente(categoria: Categoria): CategoriaIngreso = desdeNombre(categoria.name) ?: POR_DEFECTO
    }
}
