package bas.orellana.gastostracker.data.local

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import bas.orellana.gastostracker.domain.model.MetaAhorro

/**
 * Nombre con el que se guardaba la antigua categoría de gasto "Ahorro". Ya no existe en el enum,
 * pero puede aparecer en bases de datos anteriores a la versión 5 y en el JSON antiguo.
 */
internal const val CATEGORIA_AHORRO_ANTIGUA = "AHORRO"

/** Color de la antigua categoría "Ahorro", para la meta que hereda sus movimientos. */
private const val COLOR_AHORRO_ANTIGUO = 0xFFFFD700

/**
 * Pasa los gastos de la antigua categoría "Ahorro" a aportaciones de la meta "Ahorro" (que se crea
 * solo si hay algo que pasar). Se usa en la migración 4 → 5 y al importar el JSON antiguo.
 * Las aportaciones conservan el id, el importe y la fecha; el concepto pasa a la nota.
 */
internal val SQL_MOVER_AHORRO_A_METAS: List<String> = listOf(
    """
    INSERT OR IGNORE INTO metas_ahorro (id, nombre, objetivo, fecha_objetivo, color, creada, archivada)
    SELECT '${MetaAhorro.ID_AHORRO_GENERAL}', 'Ahorro', NULL, NULL, $COLOR_AHORRO_ANTIGUO, MIN(fecha), 0
    FROM gastos WHERE categoria = '$CATEGORIA_AHORRO_ANTIGUA'
    HAVING COUNT(*) > 0
    """.trimIndent(),
    """
    INSERT OR IGNORE INTO aportaciones (id, meta_id, monto, fecha, nota)
    SELECT id, '${MetaAhorro.ID_AHORRO_GENERAL}', monto, fecha, nombre
    FROM gastos WHERE categoria = '$CATEGORIA_AHORRO_ANTIGUA'
    """.trimIndent(),
    "DELETE FROM gastos WHERE categoria = '$CATEGORIA_AHORRO_ANTIGUA'",
    // Un recurrente no puede "ahorrar": pierde la categoría (sus gastos pasarían a ser gastos normales).
    "UPDATE recurrentes SET categoria = NULL WHERE categoria = '$CATEGORIA_AHORRO_ANTIGUA'",
    "UPDATE ingresos SET categoria = NULL WHERE categoria = '$CATEGORIA_AHORRO_ANTIGUA'"
)

/** 4 → 5: el ahorro deja de ser una categoría de gasto y pasa a metas de ahorro con aportaciones. */
internal val MIGRACION_4_5 = object : Migration(4, 5) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            "CREATE TABLE IF NOT EXISTS `metas_ahorro` (`id` TEXT NOT NULL, `nombre` TEXT NOT NULL, " +
                "`objetivo` REAL, `fecha_objetivo` TEXT, `color` INTEGER NOT NULL, `creada` TEXT NOT NULL, " +
                "`archivada` INTEGER NOT NULL, PRIMARY KEY(`id`))"
        )
        connection.execSQL(
            "CREATE TABLE IF NOT EXISTS `aportaciones` (`id` TEXT NOT NULL, `meta_id` TEXT NOT NULL, " +
                "`monto` REAL NOT NULL, `fecha` TEXT NOT NULL, `nota` TEXT, PRIMARY KEY(`id`))"
        )
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_aportaciones_meta_id` ON `aportaciones` (`meta_id`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_aportaciones_fecha` ON `aportaciones` (`fecha`)")
        SQL_MOVER_AHORRO_A_METAS.forEach { connection.execSQL(it) }
    }
}
