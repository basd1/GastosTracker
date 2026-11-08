package bas.orellana.gastostracker.data.legacy

import androidx.datastore.preferences.core.Preferences
import bas.orellana.gastostracker.data.local.categoriaDesdeNombre
import bas.orellana.gastostracker.data.local.entity.CategoriaPersonalizadaEntity
import bas.orellana.gastostracker.data.local.entity.GastoEntity
import bas.orellana.gastostracker.data.local.entity.IngresoEntity
import bas.orellana.gastostracker.data.repository.CategoriaPersonalizadaDto
import bas.orellana.gastostracker.data.repository.GastoDto
import bas.orellana.gastostracker.data.repository.IngresoDto
import bas.orellana.gastostracker.data.repository.repositoryJson
import bas.orellana.gastostracker.domain.model.Categoria
import kotlinx.datetime.LocalDate

/** Datos antiguos ya convertidos, en el orden en que se deben insertar (del más antiguo al más reciente). */
internal class DatosLegacy(
    val categorias: List<CategoriaPersonalizadaEntity>,
    val gastos: List<GastoEntity>,
    val ingresos: List<IngresoEntity>
)

/**
 * Lee el JSON que guardaban las versiones anteriores en DataStore.
 *
 * A diferencia de los repositorios antiguos, NO devuelve una lista vacía si algo falla: lanza la
 * excepción, para que la migración se aborte sin escribir nada y los datos antiguos sigan intactos.
 */
internal object LegacyJsonParser {

    /**
     * Combina el contenido de todos los ficheros antiguos ([fuentes], del más antiguo al más reciente).
     *
     * Las categorías personalizadas antiguas usaban ids "1", "2"... así que dos ficheros distintos
     * pueden reutilizar el mismo id para categorías diferentes. En ese caso la del fichero más
     * reciente recibe un id nuevo (determinista, para que reintentar la migración no duplique nada)
     * y se actualizan los gastos e ingresos de ese mismo fichero que la usaban.
     */
    fun combinar(fuentes: List<Preferences>): DatosLegacy {
        val categorias = mutableListOf<CategoriaPersonalizadaEntity>()
        val gastos = mutableListOf<GastoEntity>()
        val ingresos = mutableListOf<IngresoEntity>()
        val nombrePorId = mutableMapOf<String, String>()

        fuentes.forEachIndexed { indiceFuente, preferencias ->
            val idsCambiados = mutableMapOf<String, String>()
            preferencias[ClavesLegacy.CATEGORIAS_PERSONALIZADAS]?.let(::parseCategorias).orEmpty().forEach { categoria ->
                val nombreExistente = nombrePorId[categoria.id]
                when {
                    nombreExistente == null -> {
                        nombrePorId[categoria.id] = categoria.nombre
                        categorias += categoria
                    }
                    nombreExistente == categoria.nombre -> Unit // la misma categoría en los dos ficheros
                    else -> {
                        val nuevoId = "${categoria.id}_v$indiceFuente"
                        idsCambiados[categoria.id] = nuevoId
                        nombrePorId[nuevoId] = categoria.nombre
                        categorias += categoria.copy(id = nuevoId)
                    }
                }
            }
            fun String?.conIdActualizado() = this?.let { idsCambiados[it] ?: it }

            // El JSON guardaba el más reciente primero; se dan la vuelta para que el rowid
            // conserve el orden en que se añadieron.
            gastos += preferencias[ClavesLegacy.GASTOS]?.let(::parseGastos).orEmpty().asReversed()
                .map { it.copy(categoriaPersonalizadaId = it.categoriaPersonalizadaId.conIdActualizado()) }
            ingresos += preferencias[ClavesLegacy.INGRESOS]?.let(::parseIngresos).orEmpty().asReversed()
                .map { it.copy(categoriaPersonalizadaId = it.categoriaPersonalizadaId.conIdActualizado()) }
        }
        return DatosLegacy(categorias, gastos, ingresos)
    }

    fun parseGastos(json: String): List<GastoEntity> =
        repositoryJson.decodeFromString<List<GastoDto>>(json).map {
            GastoEntity(
                id = it.id,
                nombre = it.nombre,
                monto = it.monto,
                fecha = LocalDate.parse(it.fecha),
                categoria = normalizarCategoria(it.categoria),
                categoriaPersonalizadaId = normalizarTexto(it.categoriaPersonalizadaId)
            )
        }

    fun parseIngresos(json: String): List<IngresoEntity> =
        repositoryJson.decodeFromString<List<IngresoDto>>(json).map {
            IngresoEntity(
                id = it.id,
                nombre = it.nombre,
                monto = it.monto,
                fecha = LocalDate.parse(it.fecha),
                categoria = normalizarCategoria(it.categoria),
                categoriaPersonalizadaId = normalizarTexto(it.categoriaPersonalizadaId)
            )
        }

    fun parseCategorias(json: String): List<CategoriaPersonalizadaEntity> =
        repositoryJson.decodeFromString<List<CategoriaPersonalizadaDto>>(json).map {
            CategoriaPersonalizadaEntity(
                id = it.id,
                nombre = it.nombre,
                // Igual que antes: si una categoría antigua no tenía color, se le asigna uno de la paleta.
                color = it.color ?: Categoria.PALETA_COLORES_PERSONALIZADOS.random()
            )
        }

    /**
     * La app Android original leía los null con `optString`, que devuelve el texto "null", y al
     * volver a guardar la lista lo escribía así. Aquí se normaliza todo eso a un null de verdad.
     */
    private fun normalizarTexto(valor: String?): String? = valor?.takeIf { it.isNotBlank() && it != "null" }

    /** Una categoría desconocida se guarda como "sin categoría", como hacía la app antes. */
    private fun normalizarCategoria(nombre: String?): String? =
        normalizarTexto(nombre)?.takeIf { categoriaDesdeNombre(it) != null }
}
