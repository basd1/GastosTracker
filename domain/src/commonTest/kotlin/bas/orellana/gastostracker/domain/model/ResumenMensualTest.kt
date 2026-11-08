package bas.orellana.gastostracker.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ResumenMensualTest {

    @Test
    fun `resume el total, la variacion y la categoria mas alta`() {
        val resumen = calcularResumenMensual(
            mes = Mes(2026, 10),
            totalesMes = listOf(
                TotalPorCategoria(Categoria.HOGAR, null, 500.0),
                TotalPorCategoria(Categoria.COMIDA, null, 300.0),
                TotalPorCategoria(null, null, 900.0)
            ),
            totalesMesAnterior = listOf(TotalPorCategoria(Categoria.HOGAR, null, 2000.0))
        )!!

        assertEquals(1700.0, resumen.total)
        assertEquals(-0.15, resumen.comparativa.variacion!!, 1e-9)
        assertEquals(Categoria.HOGAR, resumen.categoriaTop?.categoria)
    }

    @Test
    fun `sin gastos no hay resumen`() {
        assertNull(calcularResumenMensual(Mes(2026, 10), emptyList(), emptyList()))
    }
}
