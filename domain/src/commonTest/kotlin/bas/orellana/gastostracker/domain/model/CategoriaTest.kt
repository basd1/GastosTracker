package bas.orellana.gastostracker.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CategoriaTest {

    @Test
    fun `cada categoria tiene un color opaco valido`() {
        Categoria.entries.forEach { categoria ->
            val alpha = (categoria.color ushr 24) and 0xFF
            assertEquals(0xFF, alpha, "El color de $categoria no es completamente opaco")
        }
    }

    @Test
    fun `la paleta de colores personalizados no tiene duplicados y es opaca`() {
        val paleta = Categoria.PALETA_COLORES_PERSONALIZADOS
        assertEquals(paleta.size, paleta.toSet().size, "Hay colores repetidos en la paleta")
        paleta.forEach { color ->
            val alpha = (color ushr 24) and 0xFF
            assertEquals(0xFF, alpha, "Color $color de la paleta no es opaco")
        }
    }

    @Test
    fun `las categorias de ingreso son todas menos el ahorro`() {
        assertFalse(Categoria.AHORRO in Categoria.PARA_INGRESOS)
        assertEquals(Categoria.entries.size - 1, Categoria.PARA_INGRESOS.size)
    }

    @Test
    fun `la categoria por defecto al reasignar es Otros`() {
        assertEquals(CategoriaDestino.Predefinida(Categoria.OTROS), CategoriaDestino.POR_DEFECTO)
    }

    @Test
    fun `solo la categoria AHORRO es ahorro`() {
        assertTrue(Categoria.AHORRO.esAhorro)
        Categoria.entries.filter { it != Categoria.AHORRO }.forEach { assertFalse(it.esAhorro, "$it") }
        val sinCategoria: Categoria? = null
        assertFalse(sinCategoria.esAhorro)
    }
}
