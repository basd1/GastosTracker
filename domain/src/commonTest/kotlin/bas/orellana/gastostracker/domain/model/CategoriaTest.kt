package bas.orellana.gastostracker.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CategoriaTest {

    @Test
    fun `cada categoria tiene un nombre visible no vacio`() {
        Categoria.entries.forEach { categoria ->
            assertTrue(categoria.displayName.isNotBlank(), "displayName vacio para $categoria")
        }
    }

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
}
