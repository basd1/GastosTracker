package bas.orellana.gastostracker.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import bas.orellana.gastostracker.MainActivity
import bas.orellana.gastostracker.R
import bas.orellana.gastostracker.domain.model.Mes
import bas.orellana.gastostracker.domain.repository.AjustesRepository
import bas.orellana.gastostracker.domain.repository.GastoRepository
import bas.orellana.gastostracker.presentation.util.formatMoney
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** Widget de inicio: lo gastado en el periodo actual y un botón para añadir un gasto. */
class GastosWidget : GlanceAppWidget(), KoinComponent {

    private val gastos: GastoRepository by inject()
    private val ajustesRepository: AjustesRepository by inject()

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val ajustes = runCatching { ajustesRepository.ajustes.first() }.getOrNull()
        val dia = ajustes?.diaInicioMes ?: 1
        val periodo = Mes.actual(dia).periodo(dia)
        val total = runCatching {
            gastos.getTotalesPorCategoria(periodo.start, periodo.endInclusive).first().sumOf { it.total }
        }.getOrDefault(0.0)
        val texto = formatMoney(total, ajustes?.moneda ?: "EUR")

        provideContent {
            GlanceTheme {
                Contenido(context, texto)
            }
        }
    }

    @Composable
    private fun Contenido(context: Context, total: String) {
        val abrirApp = actionStartActivity(Intent(context, MainActivity::class.java))
        val anadirGasto = actionStartActivity(
            Intent(context, MainActivity::class.java).setAction(MainActivity.ACCION_ANADIR_GASTO)
        )
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.primaryContainer)
                .cornerRadius(16.dp)
                .padding(12.dp)
                .clickable(abrirApp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = context.getString(R.string.widget_gastado_mes),
                style = TextStyle(color = GlanceTheme.colors.onPrimaryContainer, fontSize = 13.sp)
            )
            Text(
                text = total,
                style = TextStyle(color = GlanceTheme.colors.onPrimaryContainer, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = GlanceModifier.height(8.dp))
            Button(text = context.getString(R.string.widget_anadir), onClick = anadirGasto)
        }
    }
}

class GastosWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = GastosWidget()
}
