package bas.orellana.gastostracker.domain.repository

import bas.orellana.gastostracker.domain.model.Ajustes
import kotlinx.coroutines.flow.Flow

interface AjustesRepository {
    val ajustes: Flow<Ajustes>

    suspend fun setTemaOscuro(activo: Boolean)
    suspend fun setMoneda(codigo: String)
    suspend fun setDiaInicioMes(dia: Int)
    suspend fun setRecordatorio(activo: Boolean, hora: Int, minuto: Int)
    suspend fun setBloqueo(activo: Boolean)
    suspend fun setUltimoResumenVisto(mes: String)
}
