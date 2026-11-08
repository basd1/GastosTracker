package bas.orellana.gastostracker.data.local

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import bas.orellana.gastostracker.domain.model.CategoriaIngreso

private val NOMBRES_CATEGORIAS_INGRESO = CategoriaIngreso.entries.joinToString(", ") { "'${it.name}'" }

/**
 * Los ingresos usaban las categorías de gasto (Comida, Ropa...). Las que no existen como categoría
 * de ingreso pasan a "Otros"; "Regalos" y "Otros" se conservan porque existen en las dos.
 * Se usa en la migración 5 → 6, al importar el JSON antiguo y al restaurar copias de seguridad.
 */
internal val SQL_NORMALIZAR_CATEGORIAS_INGRESO: List<String> = listOf(
    """
    UPDATE ingresos SET categoria = '${CategoriaIngreso.POR_DEFECTO.name}'
    WHERE categoria IS NOT NULL AND categoria NOT IN ($NOMBRES_CATEGORIAS_INGRESO)
    """.trimIndent(),
    """
    UPDATE recurrentes SET categoria = '${CategoriaIngreso.POR_DEFECTO.name}'
    WHERE tipo = 'INGRESO' AND categoria IS NOT NULL AND categoria NOT IN ($NOMBRES_CATEGORIAS_INGRESO)
    """.trimIndent()
)

/** 5 → 6: sin cambios de esquema; los ingresos pasan a tener categorías propias. */
internal val MIGRACION_5_6 = object : Migration(5, 6) {
    override fun migrate(connection: SQLiteConnection) {
        SQL_NORMALIZAR_CATEGORIAS_INGRESO.forEach { connection.execSQL(it) }
    }
}
