package bas.orellana.gastostracker.presentation.navigation

object NavRoutes {
    const val BALANCE = "balance"
    const val HOME = "home"
    const val GRAPH_SCREEN = "graph_screen"
    const val METAS = "metas"
    const val AJUSTES = "ajustes"
    const val PRESUPUESTOS = "presupuestos"
    const val RECURRENTES = "recurrentes"

    const val ARG_ID = "id"
    const val META_DETALLE = "meta/{$ARG_ID}"

    fun metaDetalle(id: String) = "meta/$id"
}
