package bas.orellana.gastostracker.domain.model

/**
 * Categorías predefinidas. El nombre visible de cada una vive en los recursos de texto de
 * `:presentation`, para poder traducirlo; aquí solo queda el color.
 */
enum class Categoria(val color: Long) {
    COMIDA(0xFF81C784),
    HOGAR(0xFFFFB74D),
    TRANSPORTE(0xFF64B5F6),
    OCIO(0xFFBA68C8),
    ROPA(0xFFFFD54F),
    SALUD(0xFFE57373),
    TECNOLOGIA(0xFF4DD0E1),
    SERVICIOS(0xFF9575CD),
    REGALOS(0xFFF06292),
    AHORRO(0xFFFFD700),
    OTROS(0xFF90A4AE);

    companion object {
        val PALETA_COLORES_PERSONALIZADOS = listOf(
            0xFFEF9A9A, 0xFFF48FB1, 0xFFCE93D8, 0xFFB39DDB,
            0xFF9FA8DA, 0xFF81D4FA, 0xFF80DEEA, 0xFF80CBC4,
            0xFFA5D6A7, 0xFFC5E1A5, 0xFFDCE775, 0xFFFFE082,
            0xFFFFCC80, 0xFFFFAB91, 0xFFBCAAA4, 0xFFB0BEC5
        )

        /** Categorías que tiene sentido asignar a un ingreso (todas menos el ahorro). */
        val PARA_INGRESOS: List<Categoria> get() = entries.filterNot { it.esAhorro }
    }
}
