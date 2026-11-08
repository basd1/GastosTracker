package bas.orellana.gastostracker.domain.repository

import bas.orellana.gastostracker.domain.model.Aportacion
import bas.orellana.gastostracker.domain.model.MetaAhorro
import bas.orellana.gastostracker.domain.model.ProgresoMeta
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface AhorroRepository {
    /** Metas con lo ahorrado en cada una (sumado en SQL); las archivadas al final. */
    fun getProgresoMetas(): Flow<List<ProgresoMeta>>

    fun getAportaciones(metaId: String): Flow<List<Aportacion>>

    /** Todas las aportaciones, de la más antigua a la más reciente (para la evolución del ahorro). */
    fun getTodasLasAportaciones(): Flow<List<Aportacion>>

    /** Lo apartado (aportaciones menos retiradas) entre dos fechas, ambas incluidas. */
    fun getTotalAhorrado(desde: LocalDate, hasta: LocalDate): Flow<Double>

    suspend fun guardarMeta(meta: MetaAhorro)

    /** Elimina la meta y todas sus aportaciones. */
    suspend fun eliminarMeta(id: String)

    suspend fun guardarAportacion(aportacion: Aportacion)

    suspend fun eliminarAportacion(id: String)
}
