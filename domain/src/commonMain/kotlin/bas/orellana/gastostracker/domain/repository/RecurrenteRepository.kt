package bas.orellana.gastostracker.domain.repository

import bas.orellana.gastostracker.domain.model.Recurrente
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface RecurrenteRepository {
    fun getRecurrentes(): Flow<List<Recurrente>>

    /** Crea o sustituye (por id) un recurrente. */
    suspend fun guardarRecurrente(recurrente: Recurrente)

    /** Elimina el recurrente; los movimientos que ya creó se quedan. */
    suspend fun eliminarRecurrente(id: String)

    /**
     * Crea los gastos e ingresos pendientes hasta [hoy] y avanza `ultimoMesGenerado`, todo en una
     * transacción. Devuelve cuántos movimientos se han creado.
     */
    suspend fun generarPendientes(hoy: LocalDate): Int
}
