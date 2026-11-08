package bas.orellana.gastostracker.recordatorio

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.Manifest
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import bas.orellana.gastostracker.MainActivity
import bas.orellana.gastostracker.R
import bas.orellana.gastostracker.domain.repository.GastoRepository
import bas.orellana.gastostracker.domain.util.todayLocalDate
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.TimeUnit

/** Programa (o cancela) el aviso diario con WorkManager. */
object ProgramadorRecordatorio {
    private const val TRABAJO = "recordatorio_diario"

    fun programar(context: Context, hora: Int, minuto: Int) {
        val peticion = PeriodicWorkRequestBuilder<RecordatorioWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(milisHastaLaProxima(hora, minuto), TimeUnit.MILLISECONDS)
            .build()
        // Se reprograma en cada arranque y al cambiar la hora: CANCEL_AND_REENQUEUE recalcula el primer aviso.
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(TRABAJO, ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE, peticion)
    }

    fun cancelar(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(TRABAJO)
    }

    /** Milisegundos hasta la próxima vez que el reloj marque [hora]:[minuto] (hoy o mañana). */
    internal fun milisHastaLaProxima(
        hora: Int,
        minuto: Int,
        zona: TimeZone = TimeZone.currentSystemDefault(),
        ahora: LocalDateTime = Clock.System.now().toLocalDateTime(zona)
    ): Long {
        var objetivo = ahora.date.atTime(LocalTime(hora, minuto))
        if (objetivo <= ahora) objetivo = ahora.date.plus(1, DateTimeUnit.DAY).atTime(LocalTime(hora, minuto))
        return objetivo.toInstant(zona).toEpochMilliseconds() - ahora.toInstant(zona).toEpochMilliseconds()
    }
}

/** Avisa de apuntar los gastos del día, salvo que hoy ya se haya apuntado alguno. */
class RecordatorioWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params), KoinComponent {

    private val gastos: GastoRepository by inject()

    override suspend fun doWork(): Result {
        val hoy = todayLocalDate()
        val yaHayGastos = runCatching { gastos.getGastosEntre(hoy, hoy).first().isNotEmpty() }.getOrDefault(false)
        if (!yaHayGastos) mostrarNotificacion(applicationContext)
        return Result.success()
    }

    private fun mostrarNotificacion(context: Context) {
        if (context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        crearCanal(context)
        val abrirApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).setAction(MainActivity.ACCION_ANADIR_GASTO)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notificacion = NotificationCompat.Builder(context, CANAL)
            .setSmallIcon(R.drawable.ic_launcher_monochrome)
            .setContentTitle(context.getString(R.string.recordatorio_titulo))
            .setContentText(context.getString(R.string.recordatorio_texto))
            .setContentIntent(abrirApp)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat.from(context).notify(ID_NOTIFICACION, notificacion)
    }

    private fun crearCanal(context: Context) {
        val canal = NotificationChannel(CANAL, context.getString(R.string.canal_recordatorio), NotificationManager.IMPORTANCE_DEFAULT)
            .apply { description = context.getString(R.string.canal_recordatorio_descripcion) }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
    }

    private companion object {
        const val CANAL = "recordatorio"
        const val ID_NOTIFICACION = 1
    }
}
