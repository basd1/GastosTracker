package bas.orellana.gastostracker.presentation.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import bas.orellana.gastostracker.domain.model.Aportacion
import bas.orellana.gastostracker.presentation.resources.Res
import bas.orellana.gastostracker.presentation.resources.accion_cancelar
import bas.orellana.gastostracker.presentation.resources.accion_deshacer
import bas.orellana.gastostracker.presentation.resources.accion_eliminar
import bas.orellana.gastostracker.presentation.resources.aportacion_eliminada
import bas.orellana.gastostracker.presentation.resources.aportaciones
import bas.orellana.gastostracker.presentation.resources.aportar
import bas.orellana.gastostracker.presentation.resources.archivar
import bas.orellana.gastostracker.presentation.resources.desarchivar
import bas.orellana.gastostracker.presentation.resources.editar_meta
import bas.orellana.gastostracker.presentation.resources.eliminar_meta_mensaje
import bas.orellana.gastostracker.presentation.resources.eliminar_meta_titulo
import bas.orellana.gastostracker.presentation.resources.retirar
import bas.orellana.gastostracker.presentation.resources.sin_aportaciones
import bas.orellana.gastostracker.presentation.ui.components.GradientTopAppBar
import bas.orellana.gastostracker.presentation.ui.components.ProgresoMetaInfo
import bas.orellana.gastostracker.presentation.ui.dialogs.EditorAportacionDialog
import bas.orellana.gastostracker.presentation.ui.dialogs.EditorMetaDialog
import bas.orellana.gastostracker.presentation.util.formatDDMMYYYY
import bas.orellana.gastostracker.presentation.util.formatMoney
import bas.orellana.gastostracker.presentation.viewmodel.MetaDetalleViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun MetaDetalleScreen(
    navController: NavController,
    metaId: String,
    viewModel: MetaDetalleViewModel = koinViewModel(key = "meta-$metaId") { parametersOf(metaId) }
) {
    val state by viewModel.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val textoEliminada = stringResource(Res.string.aportacion_eliminada)
    val textoDeshacer = stringResource(Res.string.accion_deshacer)

    LaunchedEffect(state.eliminada) {
        if (state.eliminada) navController.popBackStack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbar) },
            topBar = {
                GradientTopAppBar(
                    emoji = "🎯",
                    title = state.progreso?.meta?.nombre.orEmpty(),
                    onBack = { navController.popBackStack() }
                )
            }
        ) { padding ->
            val progreso = state.progreso
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (progreso != null) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                ProgresoMetaInfo(progreso)
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(onClick = { viewModel.aportar(retirada = false) }, modifier = Modifier.weight(1f)) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(stringResource(Res.string.aportar))
                                    }
                                    OutlinedButton(onClick = { viewModel.aportar(retirada = true) }, modifier = Modifier.weight(1f)) {
                                        Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(stringResource(Res.string.retirar))
                                    }
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                    TextButton(onClick = { viewModel.editarMeta() }) { Text(stringResource(Res.string.editar_meta)) }
                                    TextButton(onClick = { viewModel.cambiarArchivada() }) {
                                        Text(stringResource(if (progreso.meta.archivada) Res.string.desarchivar else Res.string.archivar))
                                    }
                                    TextButton(onClick = { viewModel.pedirEliminar() }) {
                                        Text(stringResource(Res.string.accion_eliminar), color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
                item {
                    Text(
                        text = stringResource(Res.string.aportaciones),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                if (state.aportaciones.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(Res.string.sin_aportaciones),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                items(state.aportaciones, key = { it.id }) { aportacion ->
                    FilaAportacion(
                        aportacion = aportacion,
                        onEliminar = {
                            viewModel.eliminarAportacion(aportacion)
                            scope.launch {
                                val resultado = snackbar.showSnackbar(textoEliminada, actionLabel = textoDeshacer)
                                if (resultado == SnackbarResult.ActionPerformed) viewModel.restaurarAportacion(aportacion)
                            }
                        }
                    )
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }

        state.editorMeta?.let { editor ->
            EditorMetaDialog(
                editor = editor,
                onCambio = { viewModel.actualizarEditorMeta(it) },
                onGuardar = { viewModel.guardarMeta() },
                onCancelar = { viewModel.cancelarEditorMeta() }
            )
        }
        state.editorAportacion?.let { editor ->
            EditorAportacionDialog(
                editor = editor,
                onCambio = { viewModel.actualizarEditorAportacion(it) },
                onGuardar = { viewModel.guardarAportacion() },
                onCancelar = { viewModel.cancelarAportacion() }
            )
        }
        if (state.confirmarEliminar) {
            AlertDialog(
                onDismissRequest = { viewModel.cancelarEliminar() },
                title = { Text(stringResource(Res.string.eliminar_meta_titulo, state.progreso?.meta?.nombre.orEmpty())) },
                text = { Text(stringResource(Res.string.eliminar_meta_mensaje)) },
                confirmButton = {
                    TextButton(onClick = { viewModel.eliminarMeta() }) {
                        Text(stringResource(Res.string.accion_eliminar), color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.cancelarEliminar() }) { Text(stringResource(Res.string.accion_cancelar)) }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilaAportacion(aportacion: Aportacion, onEliminar: () -> Unit) {
    val estado = rememberSwipeToDismissBoxState(
        confirmValueChange = { valor ->
            if (valor == SwipeToDismissBoxValue.EndToStart) {
                onEliminar()
                true
            } else {
                false
            }
        }
    )
    SwipeToDismissBox(
        state = estado,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(Res.string.accion_eliminar), tint = MaterialTheme.colorScheme.error)
            }
        }
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(aportacion.fecha.formatDDMMYYYY(), style = MaterialTheme.typography.bodyMedium)
                    aportacion.nota?.let {
                        Text(it, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Text(
                    text = (if (aportacion.esRetirada) "−" else "+") + formatMoney(kotlin.math.abs(aportacion.monto)),
                    fontWeight = FontWeight.Bold,
                    color = if (aportacion.esRetirada) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}
