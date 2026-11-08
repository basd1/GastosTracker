package bas.orellana.gastostracker.data.repository

import androidx.room.immediateTransaction
import androidx.room.useWriterConnection
import bas.orellana.gastostracker.data.legacy.MigradorDatosLegacy
import bas.orellana.gastostracker.data.local.AppDatabase
import bas.orellana.gastostracker.data.local.CATEGORIA_PARA_HUERFANOS
import bas.orellana.gastostracker.data.local.VERSION_ACTUAL
import bas.orellana.gastostracker.data.local.SQL_NORMALIZAR_CATEGORIAS_INGRESO
import bas.orellana.gastostracker.data.local.entity.AportacionEntity
import bas.orellana.gastostracker.data.local.entity.CategoriaPersonalizadaEntity
import bas.orellana.gastostracker.data.local.entity.GastoEntity
import bas.orellana.gastostracker.data.local.entity.IngresoEntity
import bas.orellana.gastostracker.data.local.entity.MetaAhorroEntity
import bas.orellana.gastostracker.data.local.entity.PresupuestoEntity
import bas.orellana.gastostracker.data.local.entity.RecurrenteEntity
import bas.orellana.gastostracker.data.local.toModel
import bas.orellana.gastostracker.domain.repository.CopiaSeguridadInvalida
import bas.orellana.gastostracker.domain.repository.CopiaSeguridadRepository
import bas.orellana.gastostracker.domain.repository.ResumenCopia
import bas.orellana.gastostracker.domain.util.todayLocalDate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Formato de la copia de seguridad. Las filas son las entidades de Room tal cual: al añadir columnas
 * nuevas, ponles valor por defecto para poder seguir leyendo copias antiguas.
 */
@Serializable
internal data class CopiaSeguridadDto(
    val formato: String = FORMATO,
    val version: Int = VERSION_FORMATO,
    val versionBaseDatos: Int = VERSION_ACTUAL,
    val creada: String = "",
    val gastos: List<GastoEntity> = emptyList(),
    val ingresos: List<IngresoEntity> = emptyList(),
    val categorias: List<CategoriaPersonalizadaEntity> = emptyList(),
    val presupuestos: List<PresupuestoEntity> = emptyList(),
    val recurrentes: List<RecurrenteEntity> = emptyList(),
    val metas: List<MetaAhorroEntity> = emptyList(),
    val aportaciones: List<AportacionEntity> = emptyList()
) {
    companion object {
        const val FORMATO = "gastostracker-copia"
        const val VERSION_FORMATO = 1
    }
}

internal val jsonCopia = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
    // Las columnas nuevas que falten en copias antiguas se leen como null.
    explicitNulls = false
    encodeDefaults = true
}

class CopiaSeguridadRepositoryImpl(
    private val db: AppDatabase,
    private val migrador: MigradorDatosLegacy
) : CopiaSeguridadRepository {

    private val dao = db.copiaSeguridadDao()

    override suspend fun exportar(): String {
        migrador.asegurarMigracion()
        val copia = CopiaSeguridadDto(
            creada = todayLocalDate().toString(),
            gastos = dao.gastos(),
            ingresos = dao.ingresos(),
            categorias = dao.categorias(),
            presupuestos = dao.presupuestos(),
            recurrentes = dao.recurrentes(),
            metas = dao.metas(),
            aportaciones = dao.aportaciones()
        )
        return withContext(Dispatchers.Default) { jsonCopia.encodeToString(CopiaSeguridadDto.serializer(), copia) }
    }

    override suspend fun importar(contenido: String): ResumenCopia {
        migrador.asegurarMigracion()
        val copia = withContext(Dispatchers.Default) { leerYValidar(contenido) }
        db.useWriterConnection { conexion ->
            conexion.immediateTransaction {
                dao.borrarAportaciones()
                dao.borrarMetas()
                dao.borrarRecurrentes()
                dao.borrarPresupuestos()
                dao.borrarGastos()
                dao.borrarIngresos()
                dao.borrarCategorias()

                dao.insertarCategorias(copia.categorias)
                dao.insertarGastos(copia.gastos)
                dao.insertarIngresos(copia.ingresos)
                dao.insertarPresupuestos(copia.presupuestos)
                dao.insertarRecurrentes(copia.recurrentes)
                dao.insertarMetas(copia.metas)
                dao.insertarAportaciones(copia.aportaciones)
                // Por si la copia viene de una versión que dejaba movimientos sin categoría existente.
                db.gastoDao().reasignarHuerfanos(CATEGORIA_PARA_HUERFANOS.name)
                db.ingresoDao().reasignarHuerfanos(CATEGORIA_PARA_HUERFANOS.name)
                SQL_NORMALIZAR_CATEGORIAS_INGRESO.forEach { sql -> conexion.usePrepared(sql) { it.step() } }
            }
        }
        return copia.resumen()
    }

    private fun leerYValidar(contenido: String): CopiaSeguridadDto {
        val copia = try {
            jsonCopia.decodeFromString(CopiaSeguridadDto.serializer(), contenido)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw CopiaSeguridadInvalida("El archivo no es una copia de seguridad válida", e)
        }
        if (copia.formato != CopiaSeguridadDto.FORMATO) {
            throw CopiaSeguridadInvalida("El archivo no es una copia de seguridad de GastosTracker")
        }
        if (copia.version > CopiaSeguridadDto.VERSION_FORMATO) {
            throw CopiaSeguridadInvalida("La copia es de una versión más nueva de la app: actualízala para restaurarla")
        }
        fun comprobar(condicion: Boolean, mensaje: () -> String) {
            if (!condicion) throw CopiaSeguridadInvalida(mensaje())
        }
        fun <T> sinIdsRepetidos(filas: List<T>, tabla: String, id: (T) -> String) {
            val repetido = filas.groupingBy(id).eachCount().entries.firstOrNull { it.value > 1 }
            comprobar(repetido == null) { "Id repetido en $tabla: ${repetido?.key}" }
        }
        sinIdsRepetidos(copia.gastos, "gastos") { it.id }
        sinIdsRepetidos(copia.ingresos, "ingresos") { it.id }
        sinIdsRepetidos(copia.categorias, "categorías") { it.id }
        sinIdsRepetidos(copia.presupuestos, "presupuestos") { it.id }
        sinIdsRepetidos(copia.recurrentes, "recurrentes") { it.id }
        sinIdsRepetidos(copia.metas, "metas") { it.id }
        sinIdsRepetidos(copia.aportaciones, "aportaciones") { it.id }
        (copia.gastos.map { it.monto } + copia.ingresos.map { it.monto }).forEach { monto ->
            comprobar(monto.isFinite()) { "Importe no válido: $monto" }
        }
        comprobar(copia.recurrentes.all { it.toModel() != null }) { "Hay un gasto o ingreso fijo que no es válido" }
        comprobar(copia.metas.all { it.toModel() != null }) { "Hay una meta de ahorro que no es válida" }
        val idsMetas = copia.metas.map { it.id }.toSet()
        comprobar(copia.aportaciones.all { it.metaId in idsMetas }) { "Hay movimientos de ahorro de una meta que no está en la copia" }
        return copia
    }

    private fun CopiaSeguridadDto.resumen() = ResumenCopia(
        gastos = gastos.size,
        ingresos = ingresos.size,
        categorias = categorias.size,
        presupuestos = presupuestos.size,
        recurrentes = recurrentes.size,
        metas = metas.size,
        aportaciones = aportaciones.size
    )
}
