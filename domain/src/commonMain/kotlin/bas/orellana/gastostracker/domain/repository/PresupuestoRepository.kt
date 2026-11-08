package bas.orellana.gastostracker.domain.repository

import bas.orellana.gastostracker.domain.model.Presupuesto
import kotlinx.coroutines.flow.Flow

interface PresupuestoRepository {
    fun getPresupuestos(): Flow<List<Presupuesto>>

    /** Crea o sustituye el presupuesto de su categoría (o el global). */
    suspend fun guardarPresupuesto(presupuesto: Presupuesto)

    suspend fun eliminarPresupuesto(clave: String)
}
