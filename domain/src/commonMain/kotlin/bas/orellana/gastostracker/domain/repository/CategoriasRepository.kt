package bas.orellana.gastostracker.domain.repository

import bas.orellana.gastostracker.domain.model.CategoriaDestino
import bas.orellana.gastostracker.domain.model.CategoriaPersonalizada
import kotlinx.coroutines.flow.Flow

interface CategoriasRepository {
    fun getCategoriasPersonalizadas(): Flow<List<CategoriaPersonalizada>>
    suspend fun addCategoriaPersonalizada(nombre: String, color: Long)
    suspend fun updateCategoriaPersonalizada(categoria: CategoriaPersonalizada)

    /** Número de gastos más ingresos que usan la categoría personalizada [id]. */
    suspend fun contarMovimientos(id: String): Int

    /**
     * Elimina la categoría personalizada [id] y pasa sus gastos e ingresos a [destino],
     * todo en una sola transacción: nunca quedan movimientos apuntando a una categoría que no existe.
     */
    suspend fun deleteCategoriaPersonalizada(id: String, destino: CategoriaDestino)
}
