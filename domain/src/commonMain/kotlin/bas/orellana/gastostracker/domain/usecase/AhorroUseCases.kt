package bas.orellana.gastostracker.domain.usecase

import bas.orellana.gastostracker.domain.model.Aportacion
import bas.orellana.gastostracker.domain.model.MetaAhorro
import bas.orellana.gastostracker.domain.model.ProgresoMeta
import bas.orellana.gastostracker.domain.repository.AhorroRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

class GetProgresoMetasUseCase(private val repository: AhorroRepository) {
    operator fun invoke(): Flow<List<ProgresoMeta>> = repository.getProgresoMetas()
}

class GetAportacionesUseCase(private val repository: AhorroRepository) {
    operator fun invoke(metaId: String): Flow<List<Aportacion>> = repository.getAportaciones(metaId)
}

class GetTodasLasAportacionesUseCase(private val repository: AhorroRepository) {
    operator fun invoke(): Flow<List<Aportacion>> = repository.getTodasLasAportaciones()
}

class GetTotalAhorradoUseCase(private val repository: AhorroRepository) {
    operator fun invoke(desde: LocalDate, hasta: LocalDate): Flow<Double> = repository.getTotalAhorrado(desde, hasta)
}

class GuardarMetaUseCase(private val repository: AhorroRepository) {
    suspend operator fun invoke(meta: MetaAhorro) {
        require(meta.nombre.isNotBlank()) { "La meta necesita un nombre" }
        repository.guardarMeta(meta)
    }
}

class EliminarMetaUseCase(private val repository: AhorroRepository) {
    suspend operator fun invoke(id: String) = repository.eliminarMeta(id)
}

class GuardarAportacionUseCase(private val repository: AhorroRepository) {
    suspend operator fun invoke(aportacion: Aportacion) {
        require(aportacion.monto != 0.0) { "La aportación no puede ser 0" }
        repository.guardarAportacion(aportacion)
    }
}

class EliminarAportacionUseCase(private val repository: AhorroRepository) {
    suspend operator fun invoke(id: String) = repository.eliminarAportacion(id)
}
