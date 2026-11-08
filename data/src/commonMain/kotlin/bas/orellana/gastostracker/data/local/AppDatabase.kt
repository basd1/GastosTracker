package bas.orellana.gastostracker.data.local

import androidx.room.AutoMigration
import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import bas.orellana.gastostracker.data.local.dao.CategoriaPersonalizadaDao
import bas.orellana.gastostracker.data.local.dao.GastoDao
import bas.orellana.gastostracker.data.local.dao.IngresoDao
import bas.orellana.gastostracker.data.local.dao.PresupuestoDao
import bas.orellana.gastostracker.data.local.dao.RecurrenteDao
import bas.orellana.gastostracker.data.local.dao.AhorroDao
import bas.orellana.gastostracker.data.local.dao.CopiaSeguridadDao
import bas.orellana.gastostracker.data.local.entity.CategoriaPersonalizadaEntity
import bas.orellana.gastostracker.data.local.entity.GastoEntity
import bas.orellana.gastostracker.data.local.entity.IngresoEntity
import bas.orellana.gastostracker.data.local.entity.PresupuestoEntity
import bas.orellana.gastostracker.data.local.entity.RecurrenteEntity
import bas.orellana.gastostracker.data.local.entity.MetaAhorroEntity
import bas.orellana.gastostracker.data.local.entity.AportacionEntity
import bas.orellana.gastostracker.domain.model.Categoria
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Database(
    entities = [
        GastoEntity::class,
        IngresoEntity::class,
        CategoriaPersonalizadaEntity::class,
        PresupuestoEntity::class,
        RecurrenteEntity::class,
        MetaAhorroEntity::class,
        AportacionEntity::class
    ],
    version = VERSION_ACTUAL,
    exportSchema = true,
    autoMigrations = [
        // 2 → 3: tabla de presupuestos.
        AutoMigration(from = 2, to = 3),
        // 3 → 4: gastos e ingresos recurrentes.
        AutoMigration(from = 3, to = 4),
        // 6 → 7: nota y método de pago en los movimientos.
        AutoMigration(from = 6, to = 7)
    ]
)
@TypeConverters(Converters::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gastoDao(): GastoDao
    abstract fun ingresoDao(): IngresoDao
    abstract fun categoriaPersonalizadaDao(): CategoriaPersonalizadaDao
    abstract fun presupuestoDao(): PresupuestoDao
    abstract fun recurrenteDao(): RecurrenteDao
    abstract fun ahorroDao(): AhorroDao
    abstract fun copiaSeguridadDao(): CopiaSeguridadDao
}

// Room genera el `actual` de cada plataforma.
@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

/** Versión actual del esquema (la de @Database). */
internal const val VERSION_ACTUAL = 7

// Si cambia el nombre o la ruta, revisa app/src/main/res/xml/data_extraction_rules.xml (copia de seguridad).
internal const val NOMBRE_BASE_DATOS = "gastos_tracker.db"

/** Categoría a la que van los movimientos cuya categoría personalizada ya no existe. */
internal val CATEGORIA_PARA_HUERFANOS = Categoria.OTROS

/**
 * 1 → 2: sin cambios de esquema. Antes, borrar una categoría personalizada dejaba sus gastos e
 * ingresos apuntando a un id que ya no existía (se veían grises y sin nombre). Esta migración pasa
 * esos movimientos huérfanos a "Otros"; desde la versión 2, al borrar una categoría se reasignan.
 */
internal val MIGRACION_1_2 = object : Migration(1, 2) {
    override fun migrate(connection: SQLiteConnection) {
        listOf("gastos", "ingresos").forEach { tabla ->
            connection.execSQL(
                """
                UPDATE $tabla SET categoria = '${CATEGORIA_PARA_HUERFANOS.name}', categoria_personalizada_id = NULL
                WHERE categoria_personalizada_id IS NOT NULL
                  AND categoria_personalizada_id NOT IN (SELECT id FROM categorias_personalizadas)
                """.trimIndent()
            )
        }
    }
}

/**
 * Configuración común de la base de datos; cada plataforma solo aporta el builder con la ruta.
 * Sin fallbackToDestructiveMigration: un cambio de esquema sin migración debe fallar, nunca borrar datos.
 */
internal fun construirAppDatabase(builder: RoomDatabase.Builder<AppDatabase>): AppDatabase =
    builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .addMigrations(*MIGRACIONES.toTypedArray())
        .build()

/** Migraciones manuales (las que solo añaden tablas o columnas son AutoMigration en @Database). */
internal val MIGRACIONES: List<Migration> = listOf(MIGRACION_1_2, MIGRACION_4_5, MIGRACION_5_6)
