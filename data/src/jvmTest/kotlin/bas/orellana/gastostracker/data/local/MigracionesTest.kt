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
}
