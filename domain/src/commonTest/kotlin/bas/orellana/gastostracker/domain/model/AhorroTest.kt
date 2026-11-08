package bas.orellana.gastostracker.domain.model

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class AhorroTest {

    private val fecha = LocalDate(2026, 11, 8)
    private val comida = GastoModel("1", "Súper", 50.0, fecha, Categoria.COMIDA)
    private val ahorro = GastoModel("2", "Hucha", 100.0, fecha, Categoria.AHORRO)
    private val sinCategoria = GastoModel("3", "Varios", 10.0, fecha)
    private val personalizada = GastoModel("4", "Gato", 20.0, fecha, categoriaPersonalizadaId = "x")
    private val gastos = listOf(comida, ahorro, sinCategoria, personalizada)

    @Test
    fun `sinAhorro quita solo los movimientos de ahorro`() {
        assertEquals(listOf(comida, sinCategoria, personalizada), gastos.sinAhorro())
    }

    @Test
    fun `soloAhorro devuelve solo los movimientos de ahorro`() {
        assertEquals(listOf(ahorro), gastos.soloAhorro())
    }

    @Test
    fun `totalesSinAhorro quita el total de la categoria de ahorro`() {
        val totales = listOf(
            TotalPorCategoria(Categoria.COMIDA, null, 50.0),
            TotalPorCategoria(Categoria.AHORRO, null, 100.0),
            TotalPorCategoria(null, "x", 20.0)
        )
        assertEquals(listOf(totales[0], totales[2]), totales.totalesSinAhorro())
    }
}
