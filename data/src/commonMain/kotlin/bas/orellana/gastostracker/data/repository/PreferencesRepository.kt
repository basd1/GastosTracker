package bas.orellana.gastostracker.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import bas.orellana.gastostracker.data.preferences.ClavesPreferencias
import bas.orellana.gastostracker.domain.model.Ajustes
import bas.orellana.gastostracker.domain.repository.AjustesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Recibe el DataStore de preferencias (solo ajustes); los datos viven en Room. */
class PreferencesRepository(private val dataStore: DataStore<Preferences>) : AjustesRepository {

    override val ajustes: Flow<Ajustes> = dataStore.data.map { p ->
        val porDefecto = Ajustes()
        Ajustes(
            temaOscuro = p[ClavesPreferencias.DARK_THEME] ?: porDefecto.temaOscuro,
            moneda = p[ClavesPreferencias.MONEDA]?.takeIf { it in Ajustes.MONEDAS } ?: porDefecto.moneda,
            diaInicioMes = p[ClavesPreferencias.DIA_INICIO_MES]?.coerceIn(Ajustes.DIAS_INICIO_VALIDOS) ?: porDefecto.diaInicioMes,
            recordatorioActivo = p[ClavesPreferencias.RECORDATORIO_ACTIVO] ?: porDefecto.recordatorioActivo,
            recordatorioHora = p[ClavesPreferencias.RECORDATORIO_HORA]?.coerceIn(0, 23) ?: porDefecto.recordatorioHora,
            recordatorioMinuto = p[ClavesPreferencias.RECORDATORIO_MINUTO]?.coerceIn(0, 59) ?: porDefecto.recordatorioMinuto,
            bloqueoActivo = p[ClavesPreferencias.BLOQUEO_ACTIVO] ?: porDefecto.bloqueoActivo,
            ultimoResumenVisto = p[ClavesPreferencias.ULTIMO_RESUMEN_VISTO]
        )
    }

    val isDarkTheme: Flow<Boolean> = ajustes.map { it.temaOscuro }

    suspend fun setDarkTheme(isDark: Boolean) = setTemaOscuro(isDark)

    override suspend fun setTemaOscuro(activo: Boolean) {
        dataStore.edit { it[ClavesPreferencias.DARK_THEME] = activo }
    }

    override suspend fun setMoneda(codigo: String) {
        require(codigo in Ajustes.MONEDAS) { "Moneda no soportada: $codigo" }
        dataStore.edit { it[ClavesPreferencias.MONEDA] = codigo }
    }

    override suspend fun setDiaInicioMes(dia: Int) {
        dataStore.edit { it[ClavesPreferencias.DIA_INICIO_MES] = dia.coerceIn(Ajustes.DIAS_INICIO_VALIDOS) }
    }

    override suspend fun setRecordatorio(activo: Boolean, hora: Int, minuto: Int) {
        dataStore.edit {
            it[ClavesPreferencias.RECORDATORIO_ACTIVO] = activo
            it[ClavesPreferencias.RECORDATORIO_HORA] = hora.coerceIn(0, 23)
            it[ClavesPreferencias.RECORDATORIO_MINUTO] = minuto.coerceIn(0, 59)
        }
    }

    override suspend fun setBloqueo(activo: Boolean) {
        dataStore.edit { it[ClavesPreferencias.BLOQUEO_ACTIVO] = activo }
    }

    override suspend fun setUltimoResumenVisto(mes: String) {
        dataStore.edit { it[ClavesPreferencias.ULTIMO_RESUMEN_VISTO] = mes }
    }
}
