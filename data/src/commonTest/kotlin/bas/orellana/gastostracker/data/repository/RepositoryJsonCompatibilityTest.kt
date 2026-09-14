package bas.orellana.gastostracker.data.repository

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Verifica que el reemplazo de org.json por kotlinx.serialization pueda seguir leyendo
 * el JSON tal como lo escribía el código anterior (org.json), guardado hoy en los
 * DataStore de usuarios reales. Los fixtures de este archivo reproducen a mano el
 * formato exacto que generaba el código viejo, no el nuevo.
 */
class RepositoryJsonCompatibilityTest {

    // --- Gasto: el código viejo siempre escribía "categoria" y "categoriaPersonalizadaId",
    // usando JSONObject.NULL cuando no había valor ---

    @Test
    fun `decodifica Gasto legado con categoria y categoriaPersonalizadaId nulos`() {
        val legacyJson = """[{"id":"1","nombre":"Cafe","monto":3.5,"fecha":"2024-01-15","categoria":null,"categoriaPersonalizadaId":null}]"""

        val dtos = repositoryJson.decodeFromString<List<GastoDto>>(legacyJson)

        assertEquals(1, dtos.size)
        assertEquals("1", dtos[0].id)
        assertEquals("Cafe", dtos[0].nombre)
        assertEquals(3.5, dtos[0].monto)
        assertEquals("2024-01-15", dtos[0].fecha)
        assertNull(dtos[0].categoria)
        assertNull(dtos[0].categoriaPersonalizadaId)
    }

    @Test
    fun `decodifica Gasto legado con categoria estandar`() {
        val legacyJson = """[{"id":"2","nombre":"Alquiler","monto":500,"fecha":"2024-02-01","categoria":"HOGAR","categoriaPersonalizadaId":null}]"""

        val dtos = repositoryJson.decodeFromString<List<GastoDto>>(legacyJson)

        // org.json escribe los Double enteros sin punto decimal (500 en vez de 500.0);
        // el decoder debe poder leerlo igual como Double.
        assertEquals(500.0, dtos[0].monto)
        assertEquals("HOGAR", dtos[0].categoria)
    }

    @Test
    fun `decodifica Gasto legado con categoria personalizada`() {
        val legacyJson = """[{"id":"3","nombre":"Manicura","monto":25.0,"fecha":"2024-03-10","categoria":null,"categoriaPersonalizadaId":"7"}]"""

        val dtos = repositoryJson.decodeFromString<List<GastoDto>>(legacyJson)

        assertNull(dtos[0].categoria)
        assertEquals("7", dtos[0].categoriaPersonalizadaId)
    }

    // --- Ingreso: el código viejo OMITÍA por completo las claves "categoria" y
    // "categoriaPersonalizadaId" cuando eran nulas (a diferencia de Gasto) ---

    @Test
    fun `decodifica Ingreso legado sin las claves opcionales presentes`() {
        val legacyJson = """[{"id":"1","nombre":"Salario","monto":1500,"fecha":"2024-01-01"}]"""

        val dtos = repositoryJson.decodeFromString<List<IngresoDto>>(legacyJson)

        assertEquals(1, dtos.size)
        assertEquals(1500.0, dtos[0].monto)
        assertNull(dtos[0].categoria)
        assertNull(dtos[0].categoriaPersonalizadaId)
    }

    @Test
    fun `decodifica Ingreso legado con categoria presente`() {
        val legacyJson = """[{"id":"2","nombre":"Freelance","monto":250.75,"fecha":"2024-01-10","categoria":"OTROS","categoriaPersonalizadaId":"5"}]"""

        val dtos = repositoryJson.decodeFromString<List<IngresoDto>>(legacyJson)

        assertEquals("OTROS", dtos[0].categoria)
        assertEquals("5", dtos[0].categoriaPersonalizadaId)
    }

    // --- CategoriaPersonalizada: el código viejo tenía un fallback a color aleatorio
    // cuando faltaba "color" en un item; el DTO debe seguir permitiendo decodificar eso ---

    @Test
    fun `decodifica CategoriaPersonalizada legado con color presente`() {
        val legacyJson = """[{"id":"1","nombre":"Mascotas","color":16746346}]"""

        val dtos = repositoryJson.decodeFromString<List<CategoriaPersonalizadaDto>>(legacyJson)

        assertEquals(16746346L, dtos[0].color)
    }

    @Test
    fun `decodifica CategoriaPersonalizada legado sin color`() {
        val legacyJson = """[{"id":"2","nombre":"Suscripciones"}]"""

        val dtos = repositoryJson.decodeFromString<List<CategoriaPersonalizadaDto>>(legacyJson)

        assertNull(dtos[0].color)
    }

    // --- Round-trip: lo que el código nuevo escribe, el código nuevo lo tiene que poder
    // volver a leer sin perder datos ---

    @Test
    fun `round trip Gasto preserva los datos`() {
        val original = listOf(
            GastoDto(
                id = "1",
                nombre = "Super",
                monto = 42.9,
                fecha = "2024-05-01",
                categoria = "COMIDA",
                categoriaPersonalizadaId = null
            ),
            GastoDto(
                id = "2",
                nombre = "Otro",
                monto = 10.0,
                fecha = "2024-05-02",
                categoria = null,
                categoriaPersonalizadaId = "3"
            )
        )

        val json = repositoryJson.encodeToString(original)
        val decoded = repositoryJson.decodeFromString<List<GastoDto>>(json)

        assertEquals(original, decoded)
    }

    @Test
    fun `round trip Ingreso preserva los datos`() {
        val original = listOf(
            IngresoDto(
                id = "1",
                nombre = "Nomina",
                monto = 2000.0,
                fecha = "2024-05-01",
                categoria = null,
                categoriaPersonalizadaId = null
            )
        )

        val json = repositoryJson.encodeToString(original)
        val decoded = repositoryJson.decodeFromString<List<IngresoDto>>(json)

        assertEquals(original, decoded)
    }

    @Test
    fun `round trip CategoriaPersonalizada preserva los datos`() {
        val original = listOf(CategoriaPersonalizadaDto(id = "1", nombre = "Viajes", color = 123456L))

        val json = repositoryJson.encodeToString(original)
        val decoded = repositoryJson.decodeFromString<List<CategoriaPersonalizadaDto>>(json)

        assertEquals(original, decoded)
    }
}
