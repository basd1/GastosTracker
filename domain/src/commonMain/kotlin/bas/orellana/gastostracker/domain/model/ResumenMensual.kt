package bas.orellana.gastostracker.domain.model

/** Resumen de un mes cerrado: cuánto se gastó, cómo va frente al anterior y la categoría más alta. */
data class ResumenMensual(
    val mes: Mes,
    val total: Double,
    val comparativa: Comparativa,
    val categoriaTop: TotalPorCategoria?
)

/** null si en [mes] no hubo gastos (no hay nada que resumir). */
fun calcularResumenMensual(
    mes: Mes,
    totalesMes: List<TotalPorCategoria>,
    totalesMesAnterior: List<TotalPorCategoria>
): ResumenMensual? {
    val total = totalesMes.sumOf { it.total }
    if (total <= 0.0) return null
    return ResumenMensual(
        mes = mes,
        total = total,
        comparativa = Comparativa(total, totalesMesAnterior.sumOf { it.total }),
        // Los gastos sin categoría no cuentan como "categoría más alta".
        categoriaTop = totalesMes.filter { it.categoria != null || it.categoriaPersonalizadaId != null }.maxByOrNull { it.total }
    )
}
