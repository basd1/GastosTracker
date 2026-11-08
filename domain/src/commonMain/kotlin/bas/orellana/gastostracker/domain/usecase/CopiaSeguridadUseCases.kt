package bas.orellana.gastostracker.domain.usecase

import bas.orellana.gastostracker.domain.repository.CopiaSeguridadRepository
import bas.orellana.gastostracker.domain.repository.ResumenCopia

class ExportarCopiaSeguridadUseCase(private val repository: CopiaSeguridadRepository) {
    suspend operator fun invoke(): String = repository.exportar()
}

class ImportarCopiaSeguridadUseCase(private val repository: CopiaSeguridadRepository) {
    suspend operator fun invoke(contenido: String): ResumenCopia = repository.importar(contenido)
}
