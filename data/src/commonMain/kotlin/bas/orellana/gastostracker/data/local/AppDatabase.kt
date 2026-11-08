package bas.orellana.gastostracker.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import bas.orellana.gastostracker.data.local.dao.CategoriaPersonalizadaDao
import bas.orellana.gastostracker.data.local.dao.GastoDao
import bas.orellana.gastostracker.data.local.dao.IngresoDao
import bas.orellana.gastostracker.data.local.entity.CategoriaPersonalizadaEntity
import bas.orellana.gastostracker.data.local.entity.GastoEntity
import bas.orellana.gastostracker.data.local.entity.IngresoEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Database(
    entities = [GastoEntity::class, IngresoEntity::class, CategoriaPersonalizadaEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gastoDao(): GastoDao
    abstract fun ingresoDao(): IngresoDao
    abstract fun categoriaPersonalizadaDao(): CategoriaPersonalizadaDao
}

// Room genera el `actual` de cada plataforma.
@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

internal const val NOMBRE_BASE_DATOS = "gastos_tracker.db"

/**
 * Configuración común de la base de datos; cada plataforma solo aporta el builder con la ruta.
 * Sin fallbackToDestructiveMigration: un cambio de esquema sin migración debe fallar, nunca borrar datos.
 */
internal fun construirAppDatabase(builder: RoomDatabase.Builder<AppDatabase>): AppDatabase =
    builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
