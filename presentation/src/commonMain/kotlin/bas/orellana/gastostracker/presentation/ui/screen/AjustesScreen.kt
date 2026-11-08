package bas.orellana.gastostracker.presentation.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import bas.orellana.gastostracker.presentation.navigation.NavRoutes
import bas.orellana.gastostracker.presentation.resources.Res
import bas.orellana.gastostracker.presentation.resources.accion_cancelar
import bas.orellana.gastostracker.presentation.resources.administrar_categorias
import bas.orellana.gastostracker.presentation.resources.ajustes_categorias_detalle
import bas.orellana.gastostracker.presentation.resources.ajustes_presupuestos_detalle
import bas.orellana.gastostracker.presentation.resources.ajustes_recurrentes
import bas.orellana.gastostracker.presentation.resources.ajustes_recurrentes_detalle
import bas.orellana.gastostracker.presentation.resources.ajustes_seccion_apariencia
import bas.orellana.gastostracker.presentation.resources.ajustes_seccion_datos
import bas.orellana.gastostracker.presentation.resources.ajustes_seccion_organizacion
import bas.orellana.gastostracker.presentation.resources.aviso_copia_guardada
import bas.orellana.gastostracker.presentation.resources.aviso_copia_invalida
import bas.orellana.gastostracker.presentation.resources.aviso_copia_restaurada
import bas.orellana.gastostracker.presentation.resources.aviso_csv_guardado
import bas.orellana.gastostracker.presentation.resources.aviso_error_inesperado
import bas.orellana.gastostracker.presentation.resources.aviso_no_guardado
import bas.orellana.gastostracker.presentation.resources.crear_copia
import bas.orellana.gastostracker.presentation.resources.crear_copia_detalle
import bas.orellana.gastostracker.presentation.resources.exportar_csv
import bas.orellana.gastostracker.presentation.resources.exportar_csv_detalle
import bas.orellana.gastostracker.presentation.resources.modo_oscuro
import bas.orellana.gastostracker.presentation.resources.modo_oscuro_detalle
import bas.orellana.gastostracker.presentation.resources.restaurar_confirmar
import bas.orellana.gastostracker.presentation.resources.restaurar_copia
import bas.orellana.gastostracker.presentation.resources.restaurar_copia_detalle
import bas.orellana.gastostracker.presentation.resources.restaurar_mensaje
import bas.orellana.gastostracker.presentation.resources.restaurar_titulo
import bas.orellana.gastostracker.presentation.resources.titulo_ajustes
import bas.orellana.gastostracker.presentation.resources.titulo_presupuestos
import bas.orellana.gastostracker.presentation.ui.components.AjusteItem
import bas.orellana.gastostracker.presentation.ui.components.GradientTopAppBar
import bas.orellana.gastostracker.presentation.ui.components.SeccionAjustes
import bas.orellana.gastostracker.presentation.ui.dialogs.ManageCategoriasDialog
import bas.orellana.gastostracker.presentation.util.rememberAbrirArchivo
import bas.orellana.gastostracker.presentation.util.rememberGuardarArchivo
import bas.orellana.gastostracker.presentation.viewmodel.AvisoDatos
import bas.orellana.gastostracker.presentation.viewmodel.DatosViewModel
import bas.orellana.gastostracker.presentation.viewmodel.HomeViewModel
import bas.orellana.gastostracker.presentation.viewmodel.SettingsViewModel
import org.jetbrains.compose.resources.getPluralString
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Pantalla de ajustes: sustituye al antiguo diálogo y es la puerta a las pantallas secundarias. */
@Composable
fun AjustesScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel = koinViewModel(),
    homeViewModel: HomeViewModel = koinViewModel(),
    datosViewModel: DatosViewModel = koinViewModel()
) {
    val isDarkTheme by settingsViewModel.isDarkTheme.collectAsState()
    val homeState by homeViewModel.state.collectAsState()
    val datos by datosViewModel.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    val guardarCsv = rememberGuardarArchivo(DatosViewModel.MIME_CSV) {
        datosViewModel.resultadoGuardado(DatosViewModel.MIME_CSV, it)
    }
    val guardarJson = rememberGuardarArchivo(DatosViewModel.MIME_JSON) {
        datosViewModel.resultadoGuardado(DatosViewModel.MIME_JSON, it)
    }
    val abrirCopia = rememberAbrirArchivo { datosViewModel.copiaElegida(it) }

    LaunchedEffect(datos.archivoPendiente) {
        val archivo = datos.archivoPendiente ?: return@LaunchedEffect
        datosViewModel.archivoEntregado()
        if (archivo.mime == DatosViewModel.MIME_CSV) guardarCsv(archivo.nombre, archivo.contenido)
        else guardarJson(archivo.nombre, archivo.contenido)
    }
    LaunchedEffect(datos.aviso) {
        val aviso = datos.aviso ?: return@LaunchedEffect
        datosViewModel.avisoMostrado()
        val texto = when (aviso) {
            AvisoDatos.CsvGuardado -> getString(Res.string.aviso_csv_guardado)
            AvisoDatos.CopiaGuardada -> getString(Res.string.aviso_copia_guardada)
            is AvisoDatos.Restaurada ->
                getPluralString(Res.plurals.aviso_copia_restaurada, aviso.resumen.movimientos, aviso.resumen.movimientos)
            AvisoDatos.NoGuardado -> getString(Res.string.aviso_no_guardado)
            is AvisoDatos.CopiaInvalida -> getString(Res.string.aviso_copia_invalida, aviso.detalle.orEmpty())
            AvisoDatos.ErrorInesperado -> getString(Res.string.aviso_error_inesperado)
        }
        snackbar.showSnackbar(texto)
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
                if (datos.trabajando) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

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

                SeccionAjustes(stringResource(Res.string.ajustes_seccion_datos)) {
                    AjusteItem(
                        icono = Icons.Default.TableChart,
                        titulo = stringResource(Res.string.exportar_csv),
                        detalle = stringResource(Res.string.exportar_csv_detalle),
                        habilitado = !datos.trabajando,
                        onClick = { datosViewModel.exportarCsv() }
                    )
                    HorizontalDivider()
                    AjusteItem(
                        icono = Icons.Default.Backup,
                        titulo = stringResource(Res.string.crear_copia),
                        detalle = stringResource(Res.string.crear_copia_detalle),
                        habilitado = !datos.trabajando,
                        onClick = { datosViewModel.exportarCopia() }
                    )
                    HorizontalDivider()
                    AjusteItem(
                        icono = Icons.Default.Restore,
                        titulo = stringResource(Res.string.restaurar_copia),
                        detalle = stringResource(Res.string.restaurar_copia_detalle),
                        habilitado = !datos.trabajando,
                        onClick = abrirCopia
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        if (homeState.showManageCategoriasDialog) {
            ManageCategoriasDialog(homeViewModel)
        }

        if (datos.copiaPorRestaurar != null) {
            AlertDialog(
                onDismissRequest = { datosViewModel.cancelarRestaurar() },
                title = { Text(stringResource(Res.string.restaurar_titulo)) },
                text = { Text(stringResource(Res.string.restaurar_mensaje)) },
                confirmButton = {
                    TextButton(onClick = { datosViewModel.confirmarRestaurar() }) {
                        Text(stringResource(Res.string.restaurar_confirmar), color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { datosViewModel.cancelarRestaurar() }) {
                        Text(stringResource(Res.string.accion_cancelar))
                    }
                }
            )
        }
    }
}
