package bas.orellana.gastostracker.domain.model

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class EstadisticasTest {

    private fun gasto(id: String, monto: Double, fecha: LocalDate, categoria: Categoria? = null, personalizada: String? = null) =
        GastoModel(id, "G $id", monto, fecha, categoria, personalizada)

    @Test
    fun `ultimos meses en orden cronologico cruzando el año`() {
        assertEquals(listOf(Mes(2025, 11), Mes(2025, 12), Mes(2026, 1)), ultimosMeses(Mes(2026, 1), 3))
    }

    @Test
    fun `totales mensuales con meses vacios a cero`() {
        val totales = totalesMensuales(
            gastos = listOf(gasto("a", 10.0, LocalDate(2026, 10, 5)), gasto("b", 5.0, LocalDate(2026, 10, 20))),
            ingresos = listOf(IngresoModel("i", "Nómina", 100.0, LocalDate(2026, 11, 1))),
            meses = listOf(Mes(2026, 9), Mes(2026, 10), Mes(2026, 11))
        )

        assertEquals(listOf(0.0, 15.0, 0.0), totales.map { it.gastos })
        assertEquals(listOf(0.0, 0.0, 100.0), totales.map { it.ingresos })
        assertEquals(100.0, totales.last().balance)
    }

    @Test
    fun `evolucion de una categoria`() {
        val gastos = listOf(
            gasto("a", 10.0, LocalDate(2026, 10, 5), Categoria.COMIDA),
            gasto("b", 99.0, LocalDate(2026, 10, 6), Categoria.OCIO),
            gasto("c", 7.0, LocalDate(2026, 11, 1), personalizada = "gato")
        )

        assertEquals(
            listOf(Mes(2026, 10) to 10.0, Mes(2026, 11) to 0.0),
            gastoMensualDeCategoria(gastos, Categoria.COMIDA, null, listOf(Mes(2026, 10), Mes(2026, 11)))
        )
        assertEquals(7.0, gastoMensualDeCategoria(gastos, null, "gato", listOf(Mes(2026, 11))).single().second)
    }

    @Test
    fun `comparativa con el mismo mes del año anterior`() {
        val gastos = listOf(
            gasto("a", 100.0, LocalDate(2026, 11, 3), Categoria.COMIDA),
            gasto("b", 50.0, LocalDate(2026, 11, 4), Categoria.OCIO),
            gasto("c", 80.0, LocalDate(2025, 11, 3), Categoria.COMIDA),
            gasto("d", 999.0, LocalDate(2026, 10, 3), Categoria.COMIDA)
        )

        val comparativa = comparativaConAnioAnterior(gastos, Mes(2026, 11))

        assertEquals(150.0, comparativa.total.actual)
        assertEquals(80.0, comparativa.total.anterior)
        assertEquals(70.0 / 80.0, comparativa.total.variacion!!, 1e-9)
        assertEquals(listOf(Categoria.OCIO, Categoria.COMIDA), comparativa.porCategoria.map { it.categoria })
        assertNull(comparativa.porCategoria.first().comparativa.variacion)
    }
}
