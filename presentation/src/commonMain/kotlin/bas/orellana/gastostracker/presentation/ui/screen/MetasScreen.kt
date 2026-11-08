package bas.orellana.gastostracker.presentation.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import bas.orellana.gastostracker.domain.model.ProgresoMeta
import bas.orellana.gastostracker.presentation.navigation.NavRoutes
import bas.orellana.gastostracker.presentation.resources.Res
import bas.orellana.gastostracker.presentation.resources.ahorrado_este_mes
import bas.orellana.gastostracker.presentation.resources.aportar
import bas.orellana.gastostracker.presentation.resources.archivada
import bas.orellana.gastostracker.presentation.resources.nueva_meta
import bas.orellana.gastostracker.presentation.resources.sin_metas_mensaje
import bas.orellana.gastostracker.presentation.resources.sin_metas_titulo
import bas.orellana.gastostracker.presentation.resources.titulo_ahorro
import bas.orellana.gastostracker.presentation.resources.total_ahorrado
import bas.orellana.gastostracker.presentation.ui.components.BottomNavBar
import bas.orellana.gastostracker.presentation.ui.components.GradientTopAppBar
import bas.orellana.gastostracker.presentation.ui.components.ProgresoMetaInfo
import bas.orellana.gastostracker.presentation.ui.dialogs.EditorAportacionDialog
import bas.orellana.gastostracker.presentation.ui.dialogs.EditorMetaDialog
import bas.orellana.gastostracker.presentation.util.formatMoney
import bas.orellana.gastostracker.presentation.viewmodel.MetasViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MetasScreen(
    navController: NavController,
    viewModel: MetasViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                GradientTopAppBar(
                    emoji = "🐷",
                    title = stringResource(Res.string.titulo_ahorro),
                    onSettingsClick = { navController.navigate(NavRoutes.AJUSTES) }
                )
            },
            bottomBar = { BottomNavBar(navController = navController) },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.nuevaMeta() },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(Res.string.nueva_meta))
                }
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(stringResource(Res.string.total_ahorrado), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = formatMoney(state.totalAhorrado),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                            Text(
                                text = stringResource(Res.string.ahorrado_este_mes, formatMoney(state.ahorradoEsteMes)),
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                if (state.metas.isEmpty()) {
                    item { SinMetas() }
                }
                items(state.activas, key = { it.meta.id }) { progreso ->
                    TarjetaMeta(
                        progreso = progreso,
                        onClick = { navController.navigate(NavRoutes.metaDetalle(progreso.meta.id)) },
                        onAportar = { viewModel.aportar(progreso.meta) }
                    )
                }
                if (state.archivadas.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(Res.string.archivada),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    items(state.archivadas, key = { it.meta.id }) { progreso ->
                        TarjetaMeta(
                            progreso = progreso,
                            onClick = { navController.navigate(NavRoutes.metaDetalle(progreso.meta.id)) },
                            onAportar = null
                        )
                    }
                }
                item { Spacer(modifier = Modifier.height(88.dp)) }
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
    }
}

@Composable
private fun TarjetaMeta(
    progreso: ProgresoMeta,
    onClick: () -> Unit,
    onAportar: (() -> Unit)?
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = MaterialTheme.shapes.small
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color(progreso.meta.color))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = progreso.meta.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (onAportar != null) {
                    FilledTonalButton(onClick = onAportar) {
                        Text(stringResource(Res.string.aportar), fontSize = 13.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            ProgresoMetaInfo(progreso)
        }
    }
}

@Composable
private fun SinMetas() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Savings,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(stringResource(Res.string.sin_metas_titulo), style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(Res.string.sin_metas_mensaje),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
