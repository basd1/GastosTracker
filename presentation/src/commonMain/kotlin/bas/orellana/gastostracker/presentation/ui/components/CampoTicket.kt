package bas.orellana.gastostracker.presentation.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bas.orellana.gastostracker.presentation.resources.Res
import bas.orellana.gastostracker.presentation.resources.accion_cerrar
import bas.orellana.gastostracker.presentation.resources.ticket
import bas.orellana.gastostracker.presentation.resources.ticket_camara
import bas.orellana.gastostracker.presentation.resources.ticket_galeria
import bas.orellana.gastostracker.presentation.resources.ticket_importe_leido
import bas.orellana.gastostracker.presentation.resources.ticket_leer_importe
import bas.orellana.gastostracker.presentation.resources.ticket_quitar
import bas.orellana.gastostracker.presentation.resources.ticket_sin_importe
import bas.orellana.gastostracker.presentation.util.ImagenTicket
import bas.orellana.gastostracker.presentation.util.extraerImporteTotal
import bas.orellana.gastostracker.presentation.util.formatMoney
import bas.orellana.gastostracker.presentation.util.rememberLectorTicket
import bas.orellana.gastostracker.presentation.util.rememberSelectorFotoTicket
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

/**
 * Foto del ticket de un gasto: hacerla o elegirla, verla y leer el importe con OCR.
 * Al adjuntar una foto se intenta leer el importe automáticamente ([onImporteLeido]).
 * En plataformas sin cámara/OCR implementados no se muestra nada.
 */
@Composable
fun CampoTicket(
    foto: String?,
    onFoto: (String?) -> Unit,
    onImporteLeido: (Double) -> Unit
) {
    val scope = rememberCoroutineScope()
    val lector = rememberLectorTicket()
    var leyendo by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf<String?>(null) }
    var verGrande by remember { mutableStateOf(false) }
    val textoSinImporte = stringResource(Res.string.ticket_sin_importe)

    fun leer(ruta: String) {
        scope.launch {
            leyendo = true
            val importe = lector(ruta)?.let { extraerImporteTotal(it) }
            leyendo = false
            if (importe != null) {
                onImporteLeido(importe)
                mensaje = null
            } else {
                mensaje = textoSinImporte
            }
        }
    }

    val selector = rememberSelectorFotoTicket { ruta ->
        if (ruta != null) {
            onFoto(ruta)
            leer(ruta)
        }
    }
    if (!selector.disponible) return

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = stringResource(Res.string.ticket),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (foto == null) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = selector.camara, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(Res.string.ticket_camara), fontSize = 13.sp)
                }
                OutlinedButton(onClick = selector.galeria, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(Res.string.ticket_galeria), fontSize = 13.sp)
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 140.dp)
                    .clip(MaterialTheme.shapes.small)
                    .clickable { verGrande = true },
                contentAlignment = Alignment.Center
            ) {
                ImagenTicket(ruta = foto, modifier = Modifier.fillMaxWidth().height(140.dp), contentDescription = stringResource(Res.string.ticket))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { leer(foto) }, enabled = !leyendo) {
                    if (leyendo) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.DocumentScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(Res.string.ticket_leer_importe))
                }
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = { onFoto(null); mensaje = null }) {
                    Text(stringResource(Res.string.ticket_quitar), color = MaterialTheme.colorScheme.error)
                }
            }
        }
        mensaje?.let { Text(it, fontSize = 12.sp, color = MaterialTheme.colorScheme.error) }
    }

    if (verGrande && foto != null) {
        AlertDialog(
            onDismissRequest = { verGrande = false },
            text = { ImagenTicket(ruta = foto, modifier = Modifier.fillMaxWidth()) },
            confirmButton = { TextButton(onClick = { verGrande = false }) { Text(stringResource(Res.string.accion_cerrar)) } }
        )
    }
}

/** Texto para avisar del importe leído (por si se quiere enseñar en otro sitio). */
@Composable
fun textoImporteLeido(importe: Double): String = stringResource(Res.string.ticket_importe_leido, formatMoney(importe))
