package bas.orellana.gastostracker.data.legacy

import androidx.datastore.preferences.core.PreferencesSerializer
import androidx.datastore.preferences.core.mutablePreferencesOf
import bas.orellana.gastostracker.data.EntornoDatos
import bas.orellana.gastostracker.data.preferences.ClavesPreferencias
import bas.orellana.gastostracker.domain.model.Categoria
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import okio.Path
import okio.buffer
import okio.use
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class MigradorDatosLegacyTest {

    private val entornos = mutableListOf<EntornoDatos>()

    @AfterTest
    fun cerrar() = entornos.forEach { it.close() }

    private fun entorno(directorio: Path = EntornoDatos.crearDirectorioTemporal()) =
        EntornoDatos(directorio).also { entornos += it }

    private suspend fun escribirLegacy(ruta: Path, gastos: String? = null, ingresos: String? = null, categorias: String? = null) {
        val preferencias = mutablePreferencesOf().apply {
            gastos?.let { this[ClavesLegacy.GASTOS] = it }
            ingresos?.let { this[ClavesLegacy.INGRESOS] = it }
            categorias?.let { this[ClavesLegacy.CATEGORIAS_PERSONALIZADAS] = it }
            this[ClavesLegacy.DARK_THEME] = true
        }
        FileSystem.SYSTEM.sink(ruta).buffer().use { PreferencesSerializer.writeTo(preferencias, it) }
    }

    @Test
    fun `migra gastos, ingresos y categorias del JSON antiguo y lo marca como migrado`() = runTest {
        val entorno = entorno()
        escribirLegacy(
            entorno.rutaLegacy,
            gastos = """[
                {"id":"g2","nombre":"Pienso","monto":20.5,"fecha":"2026-10-02","categoriaPersonalizadaId":"1"},
                {"id":"g1","nombre":"Súper","monto":40,"fecha":"2026-10-01","categoria":"COMIDA"}
            ]""",
            ingresos = """[{"id":"i1","nombre":"Nómina","monto":1500,"fecha":"2026-10-01"}]""",
            categorias = """[{"id":"1","nombre":"Mascotas","color":4294940314}]"""
        )

        val gastos = entorno.gastos.getGastos().first()

        assertEquals(listOf("g2", "g1"), gastos.map { it.id })
        assertEquals(Categoria.COMIDA, gastos.single { it.id == "g1" }.categoria)
        assertEquals("1", gastos.single { it.id == "g2" }.categoriaPersonalizadaId)
        assertEquals(listOf("Nómina"), entorno.ingresos.getIngresos().first().map { it.nombre })
        assertEquals(listOf("Mascotas"), entorno.categorias.getCategoriasPersonalizadas().first().map { it.nombre })
        assertEquals(true, entorno.preferencias.data.first()[ClavesPreferencias.DATOS_MIGRADOS_A_ROOM])
        assertEquals(true, entorno.preferencias.data.first()[ClavesPreferencias.DARK_THEME])
    }

    @Test
    fun `los movimientos con una categoria personalizada que ya no existe pasan a Otros`() = runTest {
        val entorno = entorno()
        escribirLegacy(
            entorno.rutaLegacy,
            gastos = """[{"id":"g1","nombre":"Huérfano","monto":5,"fecha":"2026-10-01","categoriaPersonalizadaId":"borrada"}]""",
            ingresos = """[{"id":"i1","nombre":"Huérfano","monto":5,"fecha":"2026-10-01","categoriaPersonalizadaId":"borrada"}]""",
            categorias = "[]"
        )

        val gasto = entorno.gastos.getGastos().first().single()
        val ingreso = entorno.ingresos.getIngresos().first().single()

        assertEquals(Categoria.OTROS, gasto.categoria)
        assertEquals(null, gasto.categoriaPersonalizadaId)
        assertEquals(Categoria.OTROS, ingreso.categoria)
        assertEquals(null, ingreso.categoriaPersonalizadaId)
    }

    @Test
    fun `si el JSON esta corrupto no escribe nada, no lo marca y lo reintenta en el siguiente arranque`() = runTest {
        val directorio = EntornoDatos.crearDirectorioTemporal()
        val primerArranque = entorno(directorio)
        escribirLegacy(primerArranque.rutaLegacy, gastos = """[{"id":"g1","nombre":"Roto","monto";5}]""")
        val contenidoLegacy = FileSystem.SYSTEM.read(primerArranque.rutaLegacy) { readByteString() }

        assertTrue(primerArranque.gastos.getGastos().first().isEmpty())
        assertNotEquals(true, primerArranque.preferencias.data.first()[ClavesPreferencias.DATOS_MIGRADOS_A_ROOM])
        // El fichero antiguo sigue intacto.
        assertEquals(contenidoLegacy, FileSystem.SYSTEM.read(primerArranque.rutaLegacy) { readByteString() })

        // Se arregla el fichero (p. ej. con una versión nueva del parser) y la app vuelve a arrancar.
        escribirLegacy(primerArranque.rutaLegacy, gastos = """[{"id":"g1","nombre":"Bien","monto":5,"fecha":"2026-10-01"}]""")
        val segundoArranque = MigradorDatosLegacy(primerArranque.db, primerArranque.preferencias, primerArranque.fuentes)

        segundoArranque.asegurarMigracion()

        assertEquals(listOf("Bien"), primerArranque.gastos.getGastos().first().map { it.nombre })
        assertEquals(true, primerArranque.preferencias.data.first()[ClavesPreferencias.DATOS_MIGRADOS_A_ROOM])
    }

    @Test
    fun `sin ficheros antiguos marca la migracion como hecha y empieza vacio`() = runTest {
        val entorno = entorno()

        assertTrue(entorno.gastos.getGastos().first().isEmpty())
        assertEquals(true, entorno.preferencias.data.first()[ClavesPreferencias.DATOS_MIGRADOS_A_ROOM])
    }
}
