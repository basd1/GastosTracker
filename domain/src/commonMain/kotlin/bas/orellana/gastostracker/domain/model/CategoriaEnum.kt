package bas.orellana.gastostracker.domain.model

enum class Categoria(val displayName: String, val color: Long) {
    COMIDA("Comida", 0xFF81C784),
    HOGAR("Hogar", 0xFFFFB74D),
    TRANSPORTE("Transporte", 0xFF64B5F6),
    OCIO("Ocio", 0xFFBA68C8),
    ROPA("Ropa", 0xFFFFD54F),
    SALUD("Salud", 0xFFE57373),
    TECNOLOGIA("Tecnología", 0xFF4DD0E1),
    SERVICIOS("Servicios", 0xFF9575CD),
    REGALOS("Regalos", 0xFFF06292),
    AHORRO("Ahorro", 0xFFFFD700),
    OTROS("Otros", 0xFF90A4AE);

    companion object {
        val PALETA_COLORES_PERSONALIZADOS = listOf(
            0xFFEF9A9A, 0xFFF48FB1, 0xFFCE93D8, 0xFFB39DDB,
            0xFF9FA8DA, 0xFF81D4FA, 0xFF80DEEA, 0xFF80CBC4,
            0xFFA5D6A7, 0xFFC5E1A5, 0xFFDCE775, 0xFFFFE082,
            0xFFFFCC80, 0xFFFFAB91, 0xFFBCAAA4, 0xFFB0BEC5
        )
    }
}