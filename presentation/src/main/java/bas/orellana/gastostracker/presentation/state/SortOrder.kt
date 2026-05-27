package bas.orellana.gastostracker.presentation.state

enum class SortOrder(val displayName: String) {
    DATE_DESC("M\u00E1s reciente"),
    DATE_ASC("M\u00E1s antiguo"),
    AMOUNT_DESC("Mayor importe"),
    AMOUNT_ASC("Menor importe"),
    NAME_ASC("A-Z"),
    NAME_DESC("Z-A")
}
