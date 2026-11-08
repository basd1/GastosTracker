package bas.orellana.gastostracker

import android.app.Application
import bas.orellana.gastostracker.di.iniciarKoin
import bas.orellana.gastostracker.domain.repository.AjustesRepository
import bas.orellana.gastostracker.recordatorio.ProgramadorRecordatorio
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext

class BaseApplication : Application() {

    /** Vive lo mismo que el proceso: para tareas de fondo ligadas a los ajustes. */
    private val ambitoApp = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        iniciarKoin {
            androidContext(this@BaseApplication)
        }
        programarRecordatorioSegunAjustes()
    }

    /** Programa o cancela el aviso diario cada vez que cambia en Ajustes (y al arrancar). */
    private fun programarRecordatorioSegunAjustes() {
        val ajustes: AjustesRepository = get()
        ambitoApp.launch {
            ajustes.ajustes
                .map { Triple(it.recordatorioActivo, it.recordatorioHora, it.recordatorioMinuto) }
                .distinctUntilChanged()
                .catch { }
                .collect { (activo, hora, minuto) ->
                    if (activo) ProgramadorRecordatorio.programar(this@BaseApplication, hora, minuto)
                    else ProgramadorRecordatorio.cancelar(this@BaseApplication)
                }
        }
    }
}
