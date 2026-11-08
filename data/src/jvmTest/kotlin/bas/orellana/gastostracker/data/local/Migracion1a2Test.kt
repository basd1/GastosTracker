package bas.orellana.gastostracker.data.local

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** Ejecuta la migración 1 → 2 sobre las tablas tal y como estaban en la versión 1. */
class Migracion1a2Test {

    private val conexion: SQLiteConnection = BundledSQLiteDriver().open(":memory:")

    @AfterTest
    fun cerrar() = conexion.close()

    private fun filas(tabla: String): List<Triple<String, String?, String?>> =
        conexion.prepare("SELECT id, categoria, categoria_personalizada_id FROM $tabla ORDER BY id").use { st ->
            buildList {
                while (st.step()) {
                    add(
                        Triple(
                            st.getText(0),
                            if (st.isNull(1)) null else st.getText(1),
                            if (st.isNull(2)) null else st.getText(2)
                        )
                    )
                }
            }
        }

    @Test
    fun `pasa a Otros solo los movimientos cuya categoria personalizada ya no existe`() {
        conexion.execSQL("CREATE TABLE categorias_personalizadas (id TEXT NOT NULL PRIMARY KEY, nombre TEXT NOT NULL, color INTEGER NOT NULL)")
        listOf("gastos", "ingresos").forEach { tabla ->
            conexion.execSQL(
                "CREATE TABLE $tabla (id TEXT NOT NULL PRIMARY KEY, nombre TEXT NOT NULL, monto REAL NOT NULL, " +
                    "fecha TEXT NOT NULL, categoria TEXT, categoria_personalizada_id TEXT)"
            )
            conexion.execSQL(
                "INSERT INTO $tabla VALUES " +
                    "('1', 'a', 1, '2026-11-01', NULL, 'existe'), " +
                    "('2', 'b', 1, '2026-11-01', NULL, 'borrada'), " +
                    "('3', 'c', 1, '2026-11-01', 'COMIDA', NULL), " +
                    "('4', 'd', 1, '2026-11-01', NULL, NULL)"
            )
        }
        conexion.execSQL("INSERT INTO categorias_personalizadas VALUES ('existe', 'Mascotas', 0)")

        MIGRACION_1_2.migrate(conexion)

        val esperado = listOf(
            Triple("1", null, "existe"),
            Triple("2", "OTROS", null),
            Triple("3", "COMIDA", null),
            Triple("4", null, null)
        )
        assertEquals(esperado, filas("gastos"))
        assertEquals(esperado, filas("ingresos"))
    }
}
