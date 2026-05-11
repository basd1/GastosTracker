package bas.orellana.gastostracker.domain.model

enum class Categoria(val displayName: String, val color: Long) {
    COMIDA("Comida", 0xFFE8F5E9),
    HOGAR("Hogar", 0xFFFFF3E0),
    TRANSPORTE("Transporte", 0xFFE3F2FD),
    OCIO("Ocio", 0xFFF3E5F5),
    ROPA("Ropa", 0xFFFFFDE7),
    SALUD("Salud", 0xFFFFEBEE),
    TECNOLOGIA("Tecnología", 0xFFE0F7FA),
    SERVICIOS("Servicios", 0xFFEDE7F6),
    REGALOS("Regalos", 0xFFFCE4EC),
    OTROS("Otros", 0xFFECEFF1);

    companion object {
        val PALETA_COLORES_PERSONALIZADOS = listOf(
            0xFFFFCDD2, 0xFFF8BBD0, 0xFFE1BEE7, 0xFFD1C4E9,
            0xFFC5CAE9, 0xFFBBDEFB, 0xFFB3E5FC, 0xFFB2EBF2,
            0xFFB2DFDB, 0xFFC8E6C9, 0xFFDCEDC8, 0xFFF0F4C3,
            0xFFFFF9C4, 0xFFFFECB3, 0xFFFFE0B2, 0xFFFFCCBC,
            0xFFD7CCC8, 0xFFCFD8DC
        )
    }
}