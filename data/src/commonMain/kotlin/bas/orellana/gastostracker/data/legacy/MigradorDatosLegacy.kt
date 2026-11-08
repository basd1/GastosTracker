package bas.orellana.gastostracker.data.legacy

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.room.immediateTransaction
import androidx.room.useWriterConnection
import bas.orellana.gastostracker.data.local.AppDatabase
import bas.orellana.gastostracker.data.local.CATEGORIA_PARA_HUERFANOS
import bas.orellana.gastostracker.data.preferences.ClavesPreferencias
import bas.orellana.gastostracker.data.util.registrarError
import bas.orellana.gastostracker.data.util.registrarInfo
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.concurrent.Volatile

/**
 * Migración única de los datos guardados en JSON (DataStore antiguo) a Room.
 *
 * Garantías:
 * - Los repositorios llaman a [asegurarMigracion] antes de cualquier lectura o escritura,
 *   así que nada se lee ni se escribe en Room antes de haber migrado.
 * - Si el JSON antiguo no se puede leer, no se escribe nada en Room y NO se marca como migrado:
 *   se vuelve a intentar en el siguiente arranque. Los ficheros antiguos nunca se modifican.
 * - Las inserciones ignoran ids que ya existan, así que repetir la migración no duplica nada.
 */
class MigradorDatosLegacy(
    private val db: AppDatabase,
    private val preferencias: DataStore<Preferences>,
    private val fuentes: FuentesLegacy
) {
    private val mutex = Mutex()

    @Volatile
    private var comprobada = false

    suspend fun asegurarMigracion() {
        if (comprobada) return
        mutex.withLock {
            if (comprobada) return
            try {
                migrarSiHaceFalta()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // No bloqueamos la app: se reintentará en el próximo arranque con los datos antiguos intactos.
                registrarError(TAG, "No se pudieron migrar los datos antiguos a Room", e)
            }
            comprobada = true
        }
    }

    /** Devuelve un Flow que espera a que termine la migración antes de empezar a emitir. */
    internal fun <T> despuesDeMigrar(origen: () -> Flow<T>): Flow<T> = flow {
        asegurarMigracion()
        emitAll(origen())
    }

    private suspend fun migrarSiHaceFalta() {
        if (preferencias.data.first()[ClavesPreferencias.DATOS_MIGRADOS_A_ROOM] == true) return

        // Si cualquier JSON está corrupto, se lanza aquí, antes de escribir nada.
        val datos = withContext(Dispatchers.Default) { LegacyJsonParser.combinar(fuentes.leer()) }

        db.useWriterConnection { conexion ->
            conexion.immediateTransaction {
                db.categoriaPersonalizadaDao().insertarSiNoExisten(datos.categorias)
                db.gastoDao().insertarSiNoExisten(datos.gastos)
                db.ingresoDao().insertarSiNoExisten(datos.ingresos)
                // La app antigua no reasignaba nada al borrar una categoría: puede haber huérfanos.
                db.gastoDao().reasignarHuerfanos(CATEGORIA_PARA_HUERFANOS.name)
                db.ingresoDao().reasignarHuerfanos(CATEGORIA_PARA_HUERFANOS.name)
            }
        }

        preferencias.edit { it[ClavesPreferencias.DATOS_MIGRADOS_A_ROOM] = true }
        registrarInfo(
            TAG,
            "Migrados a Room: ${datos.gastos.size} gastos, ${datos.ingresos.size} ingresos, " +
                "${datos.categorias.size} categorías"
        )
    }

    private companion object {
        const val TAG = "MigradorDatosLegacy"
    }
}
