package bas.orellana.gastostracker.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PresupuestoTest {

    private fun total(categoria: Categoria?, total: Double, personalizada: String? = null) =
        TotalPorCategoria(categoria, personalizada, total)

    @Test
    fun `el nivel pasa de verde a ambar al 80 por ciento y a rojo al pasarse`() {
        val presupuesto = Presupuesto(Categoria.COMIDA, null, 100.0)
        assertEquals(NivelPresupuesto.BIEN, EstadoPresupuesto(presupuesto, 79.99).nivel)
        assertEquals(NivelPresupuesto.AVISO, EstadoPresupuesto(presupuesto, 80.0).nivel)
        assertEquals(NivelPresupuesto.AVISO, EstadoPresupuesto(presupuesto, 100.0).nivel)
        assertEquals(NivelPresupuesto.SUPERADO, EstadoPresupuesto(presupuesto, 100.01).nivel)
        assertEquals(-20.0, EstadoPresupuesto(presupuesto, 120.0).restante)
    }

    @Test
    fun `cada presupuesto cuenta solo su categoria y el global cuenta todo menos el ahorro`() {
        val estados = calcularEstadoPresupuestos(
            presupuestos = listOf(
                Presupuesto(Categoria.COMIDA, null, 200.0),
                Presupuesto(null, "mascotas", 50.0),
                Presupuesto.global(1000.0),
                Presupuesto(Categoria.OCIO, null, 100.0)
            ),
            totalesPorCategoria = listOf(
                total(Categoria.COMIDA, 150.0),
                total(null, 60.0, personalizada = "mascotas"),
                total(null, 10.0),
                total(Categoria.AHORRO, 500.0)
            )
        )

        assertEquals(listOf("global", "per:mascotas", "cat:COMIDA", "cat:OCIO"), estados.map { it.presupuesto.clave })
        assertEquals(listOf(220.0, 60.0, 150.0, 0.0), estados.map { it.gastado })
    }

    @Test
    fun `no se puede presupuestar el ahorro ni mezclar categorias`() {
        assertFailsWith<IllegalArgumentException> { Presupuesto(Categoria.AHORRO, null, 10.0) }
        assertFailsWith<IllegalArgumentException> { Presupuesto(Categoria.COMIDA, "x", 10.0) }
    }
}
