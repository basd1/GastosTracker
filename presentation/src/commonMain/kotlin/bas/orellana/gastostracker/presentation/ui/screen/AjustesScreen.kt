package bas.orellana.gastostracker.presentation.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import bas.orellana.gastostracker.presentation.navigation.NavRoutes
import bas.orellana.gastostracker.presentation.resources.Res
import bas.orellana.gastostracker.presentation.resources.administrar_categorias
import bas.orellana.gastostracker.presentation.resources.ajustes_categorias_detalle
import bas.orellana.gastostracker.presentation.resources.ajustes_presupuestos_detalle
import bas.orellana.gastostracker.presentation.resources.ajustes_recurrentes
import bas.orellana.gastostracker.presentation.resources.ajustes_recurrentes_detalle
import bas.orellana.gastostracker.presentation.resources.ajustes_seccion_apariencia
import bas.orellana.gastostracker.presentation.resources.ajustes_seccion_organizacion
import bas.orellana.gastostracker.presentation.resources.modo_oscuro
import bas.orellana.gastostracker.presentation.resources.modo_oscuro_detalle
import bas.orellana.gastostracker.presentation.resources.titulo_ajustes
import bas.orellana.gastostracker.presentation.resources.titulo_presupuestos
import bas.orellana.gastostracker.presentation.ui.components.GradientTopAppBar
import bas.orellana.gastostracker.presentation.ui.dialogs.ManageCategoriasDialog
import bas.orellana.gastostracker.presentation.viewmodel.HomeViewModel
import bas.orellana.gastostracker.presentation.viewmodel.SettingsViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Pantalla de ajustes: sustituye al antiguo diálogo y es la puerta a las pantallas secundarias. */
@Composable
fun AjustesScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel = koinViewModel(),
    homeViewModel: HomeViewModel = koinViewModel()
) {
    val isDarkTheme by settingsViewModel.isDarkTheme.collectAsState()
    val homeState by homeViewModel.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                GradientTopAppBar(
                    emoji = "⚙️",
                    title = stringResource(Res.string.titulo_ajustes),
                    onBack = { navController.popBackStack() }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SeccionAjustes(stringResource(Res.string.ajustes_seccion_apariencia)) {
                    AjusteItem(
                        icono = Icons.Default.DarkMode,
                        titulo = stringResource(Res.string.modo_oscuro),
                        detalle = stringResource(Res.string.modo_oscuro_detalle),
                        onClick = { settingsViewModel.toggleDarkTheme() }
                    ) {
                        Switch(checked = isDarkTheme, onCheckedChange = { settingsViewModel.toggleDarkTheme() })
                    }
                }

                SeccionAjustes(stringResource(Res.string.ajustes_seccion_organizacion)) {
                    AjusteItem(
                        icono = Icons.Default.Category,
                        titulo = stringResource(Res.string.administrar_categorias),
                        detalle = stringResource(Res.string.ajustes_categorias_detalle),
                        onClick = { homeViewModel.showManageCategoriasDialog() }
                    )
                    HorizontalDivider()
                    AjusteItem(
                        icono = Icons.Default.TrackChanges,
                        titulo = stringResource(Res.string.titulo_presupuestos),
                        detalle = stringResource(Res.string.ajustes_presupuestos_detalle),
                        onClick = { navController.navigate(NavRoutes.PRESUPUESTOS) }
                    )
                    HorizontalDivider()
                    AjusteItem(
                        icono = Icons.Default.EventRepeat,
                        titulo = stringResource(Res.string.ajustes_recurrentes),
                        detalle = stringResource(Res.string.ajustes_recurrentes_detalle),
                        onClick = { navController.navigate(NavRoutes.RECURRENTES) }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        if (homeState.showManageCategoriasDialog) {
            ManageCategoriasDialog(homeViewModel)
        }
    }
}

@Composable
fun SeccionAjustes(titulo: String, contenido: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            text = titulo.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 8.dp, bottom = 6.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = MaterialTheme.shapes.small,
            content = contenido
        )
    }
}

/** Fila de ajustes: icono, título, detalle y a la derecha un control o una flecha. */
@Composable
fun AjusteItem(
    icono: ImageVector,
    titulo: String,
    detalle: String? = null,
    onClick: () -> Unit,
    habilitado: Boolean = true,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = habilitado, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.bodyLarge,
                color = if (habilitado) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (detalle != null) {
                Text(
                    text = detalle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (trailing != null) {
            trailing()
        } else {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
