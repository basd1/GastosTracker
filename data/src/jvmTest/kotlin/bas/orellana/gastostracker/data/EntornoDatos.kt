package bas.orellana.gastostracker.data

import androidx.room.Room
import bas.orellana.gastostracker.data.legacy.FuentesLegacy
import bas.orellana.gastostracker.data.legacy.MigradorDatosLegacy
import bas.orellana.gastostracker.data.local.AppDatabase
import bas.orellana.gastostracker.data.local.construirAppDatabase
import bas.orellana.gastostracker.data.preferences.abrirPreferenciasDataStore
import bas.orellana.gastostracker.data.repository.CategoriasRepositoryImpl
import bas.orellana.gastostracker.data.repository.GastoRepositoryImpl
import bas.orellana.gastostracker.data.repository.IngresoRepositoryImpl
import bas.orellana.gastostracker.data.repository.PresupuestoRepositoryImpl
import bas.orellana.gastostracker.data.repository.RecurrenteRepositoryImpl
import bas.orellana.gastostracker.data.repository.AhorroRepositoryImpl
import bas.orellana.gastostracker.data.repository.CopiaSeguridadRepositoryImpl
import bas.orellana.gastostracker.data.repository.CuentaRepositoryImpl
import okio.FileSystem
import okio.Path
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Base de datos Room en memoria + DataStore de preferencias en un directorio temporal,
 * montados igual que en la app (misma configuración de Room y mismo migrador).
 *
 * Los repositorios migran de forma perezosa en el primer acceso, así que el fichero antiguo
 * ([rutaLegacy]) se puede escribir después de crear el entorno y antes de usar los repositorios.
 */
@OptIn(ExperimentalUuidApi::class)
internal class EntornoDatos(
    val directorio: Path = crearDirectorioTemporal()
) : AutoCloseable {

    val rutaLegacy: Path = directorio / "settings.preferences_pb"

    val db: AppDatabase = construirAppDatabase(Room.inMemoryDatabaseBuilder<AppDatabase>())
    val fuentes = FuentesLegacy(rutas = listOf(rutaLegacy), fileSystem = FileSystem.SYSTEM)
    val preferencias = abrirPreferenciasDataStore(directorio / "preferencias.preferences_pb", fuentes)
    val migrador = MigradorDatosLegacy(db, preferencias, fuentes)

    val gastos = GastoRepositoryImpl(db.gastoDao(), migrador)
    val ingresos = IngresoRepositoryImpl(db.ingresoDao(), migrador)
    val categorias = CategoriasRepositoryImpl(db, migrador)
    val presupuestos = PresupuestoRepositoryImpl(db.presupuestoDao(), migrador)
    val recurrentes = RecurrenteRepositoryImpl(db, migrador)
    val ahorro = AhorroRepositoryImpl(db, migrador)
    val copias = CopiaSeguridadRepositoryImpl(db, migrador)
    val cuentas = CuentaRepositoryImpl(db, migrador)

    override fun close() {
        db.close()
        // En Windows el DataStore puede tener aún el fichero abierto: no es un fallo del test.
        runCatching { FileSystem.SYSTEM.deleteRecursively(directorio) }
    }

    companion object {
        fun crearDirectorioTemporal(): Path =
            (FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "gastostracker-test-${Uuid.random()}")
                .also { FileSystem.SYSTEM.createDirectories(it) }
    }
}
