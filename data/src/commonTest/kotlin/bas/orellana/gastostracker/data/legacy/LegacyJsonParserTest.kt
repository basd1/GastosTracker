package bas.orellana.gastostracker.data.legacy

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.mutablePreferencesOf
import bas.orellana.gastostracker.domain.model.Categoria
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LegacyJsonParserTest {

    private fun fuente(
        gastos: String? = null,
        ingresos: String? = null,
        categorias: String? = null
    ): Preferences = mutablePreferencesOf().apply {
        gastos?.let { this[ClavesLegacy.GASTOS] = it }
        ingresos?.let { this[ClavesLegacy.INGRESOS] = it }
        categorias?.let { this[ClavesLegacy.CATEGORIAS_PERSONALIZADAS] = it }
    }

    @Test
    fun `un JSON corrupto lanza en vez de devolver una lista vacia`() {
        val corrupto = """[{"id":"1","nombre":"Cafe","monto";3.5,"fecha":"2024-01-15"}]"""

        assertFails { LegacyJsonParser.combinar(listOf(fuente(gastos = corrupto))) }
    }

    @Test
    fun `una fecha invalida lanza`() {
        val json = """[{"id":"1","nombre":"Cafe","monto":3.5,"fecha":"15/01/2024"}]"""

        assertFails { LegacyJsonParser.parseGastos(json) }
    }

    @Test
    fun `el texto null de la app Android original se normaliza a null`() {
        // La app original leía los null con optString ("null") y los volvía a guardar como texto.
        val json = """[{"id":"1","nombre":"Cafe","monto":3.5,"fecha":"2024-01-15","categoria":"COMIDA","categoriaPersonalizadaId":"null"}]"""

        val gasto = LegacyJsonParser.parseGastos(json).single()

        assertEquals("COMIDA", gasto.categoria)
        assertNull(gasto.categoriaPersonalizadaId)
    }

    @Test
    fun `una categoria desconocida se guarda como sin categoria`() {
        val json = """[{"id":"1","nombre":"Cafe","monto":3.5,"fecha":"2024-01-15","categoria":"NO_EXISTE"}]"""

        assertNull(LegacyJsonParser.parseGastos(json).single().categoria)
    }

    @Test
    fun `los movimientos se devuelven del mas antiguo al mas reciente`() {
        // El JSON guardaba el último añadido primero.
        val json = """[
            {"id":"nuevo","nombre":"B","monto":2,"fecha":"2024-01-15"},
            {"id":"viejo","nombre":"A","monto":1,"fecha":"2024-01-15"}
        ]"""

        val datos = LegacyJsonParser.combinar(listOf(fuente(gastos = json, ingresos = json)))

        assertEquals(listOf("viejo", "nuevo"), datos.gastos.map { it.id })
        assertEquals(listOf("viejo", "nuevo"), datos.ingresos.map { it.id })
    }

    @Test
    fun `combina las dos fuentes y renombra ids de categoria que chocan`() {
        val androidOriginal = fuente(
            categorias = """[{"id":"1","nombre":"Mascotas","color":1}]""",
            gastos = """[{"id":"g1","nombre":"Pienso","monto":12,"fecha":"2024-01-01","categoriaPersonalizadaId":"1"}]"""
        )
        val versionKmp = fuente(
            categorias = """[{"id":"1","nombre":"Viajes","color":2},{"id":"2","nombre":"Regalos","color":3}]""",
            gastos = """[{"id":"g2","nombre":"Hotel","monto":80,"fecha":"2024-02-01","categoriaPersonalizadaId":"1"}]"""
        )

        val datos = LegacyJsonParser.combinar(listOf(androidOriginal, versionKmp))

        assertEquals(
            listOf("1" to "Mascotas", "1_v1" to "Viajes", "2" to "Regalos"),
            datos.categorias.map { it.id to it.nombre }
        )
        assertEquals("1", datos.gastos.single { it.id == "g1" }.categoriaPersonalizadaId)
        assertEquals("1_v1", datos.gastos.single { it.id == "g2" }.categoriaPersonalizadaId)
    }

    @Test
    fun `la misma categoria en las dos fuentes no se duplica`() {
        val categoria = """[{"id":"1","nombre":"Mascotas","color":1}]"""

        val datos = LegacyJsonParser.combinar(listOf(fuente(categorias = categoria), fuente(categorias = categoria)))

        assertEquals(listOf("1"), datos.categorias.map { it.id })
    }

    @Test
    fun `una categoria sin color recibe uno de la paleta`() {
        val categoria = LegacyJsonParser.parseCategorias("""[{"id":"1","nombre":"Mascotas"}]""").single()

        assertTrue(categoria.color in Categoria.PALETA_COLORES_PERSONALIZADOS)
    }
}
