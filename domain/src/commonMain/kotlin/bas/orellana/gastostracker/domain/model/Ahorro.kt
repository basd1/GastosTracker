package bas.orellana.gastostracker.domain.model

/*
 * El ahorro se apunta como un gasto de la categoría [Categoria.AHORRO], pero no es un gasto:
 * no cuenta en los totales de gasto, ni en las alertas, ni en el gráfico de categorías.
 * Toda la app decide qué es ahorro a través de estas funciones.
 */

val Categoria?.esAhorro: Boolean get() = this == Categoria.AHORRO

val GastoModel.esAhorro: Boolean get() = categoria.esAhorro

val TotalPorCategoria.esAhorro: Boolean get() = categoria.esAhorro

/** Solo los gastos reales, sin el dinero apartado como ahorro. */
fun List<GastoModel>.sinAhorro(): List<GastoModel> = filterNot { it.esAhorro }

/** Solo los movimientos de ahorro. */
fun List<GastoModel>.soloAhorro(): List<GastoModel> = filter { it.esAhorro }

/** Totales por categoría sin la categoría de ahorro. */
fun List<TotalPorCategoria>.totalesSinAhorro(): List<TotalPorCategoria> = filterNot { it.esAhorro }
