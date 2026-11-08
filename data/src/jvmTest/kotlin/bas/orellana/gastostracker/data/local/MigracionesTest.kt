package bas.orellana.gastostracker.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import kotlin.io.path.createTempDirectory
import kotlin.io.path.Path
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Comprueba cada migración contra los esquemas exportados en `data/schemas`: Room valida que el
 * resultado coincide exactamente con el esquema de la versión de destino.
 */
class MigracionesTest {

    private val directorio = createTempDirectory("gastostracker-migraciones")
    private val rutaBd = directorio.resolve("migraciones.db")

    private val helper = MigrationTestHelper(
        schemaDirectoryPath = Path("schemas"),
        databasePath = rutaBd,
        driver = BundledSQLiteDriver(),
        databaseClass = AppDatabase::class
    )

    @AfterTest
    fun cerrar() {
        directorio.toFile().deleteRecursively()
    }

    @Test
    fun `de la version 1 a la ultima se conservan los datos`() {
        helper.createDatabase(1).use { conexion ->
            conexion.execSQL("INSERT INTO gastos VALUES ('g1', 'Pan', 1.5, '2026-11-01', 'COMIDA', NULL)")
            conexion.execSQL("INSERT INTO ingresos VALUES ('i1', 'Nómina', 1500, '2026-11-01', NULL, NULL)")
        }

        helper.runMigrationsAndValidate(VERSION_ACTUAL, MIGRACIONES).use { conexion ->
            conexion.prepare("SELECT COUNT(*) FROM gastos").use { st ->
                st.step()
                assertEquals(1, st.getLong(0).toInt())
            }
            conexion.prepare("SELECT COUNT(*) FROM ingresos").use { st ->
                st.step()
                assertEquals(1, st.getLong(0).toInt())
            }
        }
    }

    @Test
    fun `cada version migra a la siguiente`() {
        for (desde in 1 until VERSION_ACTUAL) {
            val ruta = directorio.resolve("paso-$desde.db")
            val pasoHelper = MigrationTestHelper(Path("schemas"), ruta, BundledSQLiteDriver(), AppDatabase::class)
            pasoHelper.createDatabase(desde).close()
            pasoHelper.runMigrationsAndValidate(desde + 1, MIGRACIONES).close()
        }
    }

    @Test
    fun `la 4 a 5 pasa los gastos de Ahorro a aportaciones de una meta`() {
        val ruta = directorio.resolve("ahorro.db")
        val pasoHelper = MigrationTestHelper(Path("schemas"), ruta, BundledSQLiteDriver(), AppDatabase::class)
        pasoHelper.createDatabase(4).use { conexion ->
            conexion.execSQL("INSERT INTO gastos VALUES ('a1', 'Hucha', 100, '2026-09-01', 'AHORRO', NULL)")
            conexion.execSQL("INSERT INTO gastos VALUES ('a2', 'Hucha', 50, '2026-10-01', 'AHORRO', NULL)")
            conexion.execSQL("INSERT INTO gastos VALUES ('g1', 'Pan', 2, '2026-10-01', 'COMIDA', NULL)")
        }

        pasoHelper.runMigrationsAndValidate(5, MIGRACIONES).use { conexion ->
            conexion.prepare("SELECT id FROM gastos").use { st ->
                st.step()
                assertEquals("g1", st.getText(0))
                assertEquals(false, st.step())
            }
            conexion.prepare("SELECT COUNT(*), SUM(monto), MIN(meta_id) FROM aportaciones").use { st ->
                st.step()
                assertEquals(2, st.getLong(0).toInt())
                assertEquals(150.0, st.getDouble(1))
                assertEquals("ahorro-general", st.getText(2))
            }
            conexion.prepare("SELECT nombre, creada FROM metas_ahorro").use { st ->
                st.step()
                assertEquals("Ahorro", st.getText(0))
                assertEquals("2026-09-01", st.getText(1))
            }
        }
    }

    @Test
    fun `la 4 a 5 sin ahorro no crea ninguna meta`() {
        val ruta = directorio.resolve("sin-ahorro.db")
        val pasoHelper = MigrationTestHelper(Path("schemas"), ruta, BundledSQLiteDriver(), AppDatabase::class)
        pasoHelper.createDatabase(4).use { conexion ->
            conexion.execSQL("INSERT INTO gastos VALUES ('g1', 'Pan', 2, '2026-10-01', 'COMIDA', NULL)")
        }
        pasoHelper.runMigrationsAndValidate(5, MIGRACIONES).use { conexion ->
            conexion.prepare("SELECT COUNT(*) FROM metas_ahorro").use { st ->
                st.step()
                assertEquals(0, st.getLong(0).toInt())
            }
        }
    }

    @Test
    fun `la 5 a 6 pasa a Otros las categorias de ingreso que no existen`() {
        val ruta = directorio.resolve("ingresos.db")
        val pasoHelper = MigrationTestHelper(Path("schemas"), ruta, BundledSQLiteDriver(), AppDatabase::class)
        pasoHelper.createDatabase(5).use { conexion ->
            conexion.execSQL("INSERT INTO ingresos VALUES ('i1', 'Venta', 10, '2026-10-01', 'ROPA', NULL)")
            conexion.execSQL("INSERT INTO ingresos VALUES ('i2', 'Cumple', 20, '2026-10-01', 'REGALOS', NULL)")
            conexion.execSQL("INSERT INTO ingresos VALUES ('i3', 'Sin', 30, '2026-10-01', NULL, NULL)")
            conexion.execSQL(
                "INSERT INTO recurrentes VALUES ('r1', 'INGRESO', 'Nómina', 1800, 'COMIDA', NULL, 28, '2026-10-28', NULL, 1)"
            )
            conexion.execSQL(
                "INSERT INTO recurrentes VALUES ('r2', 'GASTO', 'Súper', 50, 'COMIDA', NULL, 1, '2026-10-01', NULL, 1)"
            )
        }

        pasoHelper.runMigrationsAndValidate(6, MIGRACIONES).use { conexion ->
            val ingresos = conexion.prepare("SELECT categoria FROM ingresos ORDER BY id").use { st ->
                buildList { while (st.step()) add(if (st.isNull(0)) null else st.getText(0)) }
            }
            assertEquals(listOf("OTROS", "REGALOS", null), ingresos)
            val recurrentes = conexion.prepare("SELECT categoria FROM recurrentes ORDER BY id").use { st ->
                buildList { while (st.step()) add(st.getText(0)) }
            }
            assertEquals(listOf("OTROS", "COMIDA"), recurrentes)
        }
    }
}
