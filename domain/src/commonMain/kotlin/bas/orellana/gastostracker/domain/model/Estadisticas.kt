package bas.orellana.gastostracker.domain.model

/** Los [n] meses que terminan en [hasta] (incluido), del más antiguo al más reciente. */
fun ultimosMeses(hasta: Mes, n: Int): List<Mes> = (n - 1 downTo 0).map { hasta.mas(-it) }

/** Ingresos y gastos de un mes. */
data class TotalMensual(val mes: Mes, val ingresos: Double, val gastos: Double) {
    val balance: Double get() = ingresos - gastos
}

/** Ingresos frente a gastos de cada uno de [meses] (los meses sin movimientos salen a 0). */
fun totalesMensuales(gastos: List<GastoModel>, ingresos: List<IngresoModel>, meses: List<Mes>): List<TotalMensual> {
    val gastosPorMes = gastos.groupBy { Mes.de(it.fecha) }.mapValues { (_, lista) -> lista.sumOf { it.monto } }
    val ingresosPorMes = ingresos.groupBy { Mes.de(it.fecha) }.mapValues { (_, lista) -> lista.sumOf { it.monto } }
    return meses.map { TotalMensual(it, ingresosPorMes[it] ?: 0.0, gastosPorMes[it] ?: 0.0) }
}

/** Lo gastado cada mes en una categoría (predefinida o personalizada). */
fun gastoMensualDeCategoria(
    gastos: List<GastoModel>,
    categoria: Categoria?,
    categoriaPersonalizadaId: String?,
    meses: List<Mes>
): List<Pair<Mes, Double>> {
    val porMes = gastos
        .filter { it.categoria == categoria && it.categoriaPersonalizadaId == categoriaPersonalizadaId }
        .groupBy { Mes.de(it.fecha) }
        .mapValues { (_, lista) -> lista.sumOf { it.monto } }
    return meses.map { it to (porMes[it] ?: 0.0) }
}

/** Un total comparado con otro (p. ej. el mismo mes del año anterior). */
data class Comparativa(val actual: Double, val anterior: Double) {
    val diferencia: Double get() = actual - anterior

    /** Variación en tanto por uno (0.12 = +12 %); null si antes no había nada con qué comparar. */
    val variacion: Double? get() = if (anterior > 0) diferencia / anterior else null
}

/** Variación de una categoría entre dos periodos. */
data class ComparativaCategoria(
    val categoria: Categoria?,
    val categoriaPersonalizadaId: String?,
    val comparativa: Comparativa
)

/** Lo gastado en [mes] frente al mismo mes del año anterior, en total y por categoría. */
data class ComparativaAnual(
    val mes: Mes,
    val total: Comparativa,
    /** Ordenadas por la diferencia en valor absoluto, de mayor a menor. */
    val porCategoria: List<ComparativaCategoria>
)

fun comparativaConAnioAnterior(gastos: List<GastoModel>, mes: Mes): ComparativaAnual {
    val anterior = mes.mas(-12)
    val delMes = gastos.filter { it.fecha in mes }
    val delAnterior = gastos.filter { it.fecha in anterior }
    fun totales(lista: List<GastoModel>) =
        lista.groupBy { it.categoria to it.categoriaPersonalizadaId }.mapValues { (_, g) -> g.sumOf { it.monto } }
    val actuales = totales(delMes)
    val anteriores = totales(delAnterior)
    val porCategoria = (actuales.keys + anteriores.keys).map { clave ->
        ComparativaCategoria(clave.first, clave.second, Comparativa(actuales[clave] ?: 0.0, anteriores[clave] ?: 0.0))
    }.sortedByDescending { kotlin.math.abs(it.comparativa.diferencia) }
    return ComparativaAnual(
        mes = mes,
        total = Comparativa(delMes.sumOf { it.monto }, delAnterior.sumOf { it.monto }),
        porCategoria = porCategoria
    )
}
