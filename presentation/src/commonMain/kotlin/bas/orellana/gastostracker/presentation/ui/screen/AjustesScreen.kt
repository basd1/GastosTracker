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
import androidx.compose.material.icons.filled.AccountBalance
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
import androidx.compose.material.icons.filled.Fingerprint
import bas.orellana.gastostracker.presentation.util.bloqueoDisponible
import bas.orellana.gastostracker.presentation.resources.ajustes_seccion_seguridad
import bas.orellana.gastostracker.presentation.resources.ajustes_bloqueo
import bas.orellana.gastostracker.presentation.resources.ajustes_bloqueo_detalle
import bas.orellana.gastostracker.presentation.resources.ajustes_bloqueo_no_disponible
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import bas.orellana.gastostracker.presentation.ui.dialogs.HoraDialog
import bas.orellana.gastostracker.presentation.ui.dialogs.formatoHora
import bas.orellana.gastostracker.presentation.util.rememberPedirPermisoNotificaciones
import bas.orellana.gastostracker.presentation.resources.ajustes_recordatorio
import bas.orellana.gastostracker.presentation.resources.ajustes_recordatorio_detalle
import bas.orellana.gastostracker.presentation.resources.ajustes_recordatorio_hora
import bas.orellana.gastostracker.presentation.resources.aviso_permiso_notificaciones
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.filled.Euro
import androidx.compose.material.icons.filled.CalendarMonth
import bas.orellana.gastostracker.presentation.ui.dialogs.MonedaDialog
import bas.orellana.gastostracker.presentation.ui.dialogs.DiaInicioMesDialog
import bas.orellana.gastostracker.presentation.util.formatMoney
import bas.orellana.gastostracker.presentation.resources.ajustes_seccion_preferencias
import bas.orellana.gastostracker.presentation.resources.ajustes_moneda
import bas.orellana.gastostracker.presentation.resources.ajustes_dia_inicio
import bas.orellana.gastostracker.presentation.resources.ajustes_dia_inicio_detalle
import bas.orellana.gastostracker.presentation.resources.ajustes_dia_inicio_detalle_1
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
import bas.orellana.gastostracker.presentation.resources.titulo_cuentas
import bas.orellana.gastostracker.presentation.resources.ajustes_cuentas_detalle
import bas.orellana.gastostracker.presentation.ui.components.AjusteItem
import bas.orellana.gastostracker.presentation.ui.components.GradientTopAppBar
import bas.orellana.gastostracker.presentation.ui.components.SeccionAjustes
import bas.orellana.gastostracker.presentation.ui.dialogs.ManageCategoriasDialog
import bas.orellana.gastostracker.presentation.util.rememberAbrirArchivo
import bas.orellana.gastostracker.presentation.util.rememberGuardarArchivo
import bas.orellana.gastostracker.presentation.viewmodel.AvisoDatos
import bas.orellana.gastostracker.presentation.viewmodel.DatosViewModel
import bas.orellana.gastostracker.presentation.viewmodel.CategoriasViewModel
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
    categoriasViewModel: CategoriasViewModel = koinViewModel(),
    datosViewModel: DatosViewModel = koinViewModel()
) {
    val isDarkTheme by settingsViewModel.isDarkTheme.collectAsState()
    val ajustes by settingsViewModel.ajustes.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var mostrarMoneda by remember { mutableStateOf(false) }
    var mostrarDiaInicio by remember { mutableStateOf(false) }
    var mostrarHora by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val pedirPermisoNotificaciones = rememberPedirPermisoNotificaciones { concedido ->
        if (concedido) settingsViewModel.setRecordatorio(true)
        else scope.launch { snackbar.showSnackbar(getString(Res.string.aviso_permiso_notificaciones)) }
    }
    val categoriasState by categoriasViewModel.state.collectAsState()
    val datos by datosViewModel.state.collectAsState()


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

                SeccionAjustes(stringResource(Res.string.ajustes_seccion_preferencias)) {
                    AjusteItem(
                        icono = Icons.Default.Euro,
                        titulo = stringResource(Res.string.ajustes_moneda),
                        detalle = "${ajustes.moneda} · ${formatMoney(1234.5)}",
                        onClick = { mostrarMoneda = true }
                    )
                    HorizontalDivider()
                    AjusteItem(
                        icono = Icons.Default.CalendarMonth,
                        titulo = stringResource(Res.string.ajustes_dia_inicio),
                        detalle = if (ajustes.diaInicioMes == 1) {
                            stringResource(Res.string.ajustes_dia_inicio_detalle_1)
                        } else {
                            stringResource(Res.string.ajustes_dia_inicio_detalle, ajustes.diaInicioMes, ajustes.diaInicioMes - 1)
                        },
                        onClick = { mostrarDiaInicio = true }
                    )
                    HorizontalDivider()
                    AjusteItem(
                        icono = Icons.Default.NotificationsActive,
                        titulo = stringResource(Res.string.ajustes_recordatorio),
                        detalle = stringResource(Res.string.ajustes_recordatorio_detalle),
                        onClick = { if (ajustes.recordatorioActivo) settingsViewModel.setRecordatorio(false) else pedirPermisoNotificaciones() }
                    ) {
                        Switch(
                            checked = ajustes.recordatorioActivo,
                            onCheckedChange = { activar -> if (activar) pedirPermisoNotificaciones() else settingsViewModel.setRecordatorio(false) }
                        )
                    }
                    if (ajustes.recordatorioActivo) {
                        HorizontalDivider()
                        AjusteItem(
                            icono = Icons.Default.Schedule,
                            titulo = stringResource(Res.string.ajustes_recordatorio_hora),
                            detalle = formatoHora(ajustes.recordatorioHora, ajustes.recordatorioMinuto),
                            onClick = { mostrarHora = true }
                        )
                    }
                }

                SeccionAjustes(stringResource(Res.string.ajustes_seccion_organizacion)) {
                    AjusteItem(
                        icono = Icons.Default.Category,
                        titulo = stringResource(Res.string.administrar_categorias),
                        detalle = stringResource(Res.string.ajustes_categorias_detalle),
                        onClick = { categoriasViewModel.mostrarDialogo() }
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
                    HorizontalDivider()
                    AjusteItem(
                        icono = Icons.Default.AccountBalance,
                        titulo = stringResource(Res.string.titulo_cuentas),
                        detalle = stringResource(Res.string.ajustes_cuentas_detalle),
                        onClick = { navController.navigate(NavRoutes.CUENTAS) }
                    )
                }

                val puedeBloquear = bloqueoDisponible()
                SeccionAjustes(stringResource(Res.string.ajustes_seccion_seguridad)) {
                    AjusteItem(
                        icono = Icons.Default.Fingerprint,
                        titulo = stringResource(Res.string.ajustes_bloqueo),
                        detalle = stringResource(
                            if (puedeBloquear || ajustes.bloqueoActivo) Res.string.ajustes_bloqueo_detalle
                            else Res.string.ajustes_bloqueo_no_disponible
                        ),
                        habilitado = puedeBloquear || ajustes.bloqueoActivo,
                        onClick = { settingsViewModel.setBloqueo(!ajustes.bloqueoActivo) }
                    ) {
                        Switch(
                            checked = ajustes.bloqueoActivo,
                            enabled = puedeBloquear || ajustes.bloqueoActivo,
                            onCheckedChange = { settingsViewModel.setBloqueo(it) }
                        )
                    }
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

        if (categoriasState.visible) {
            ManageCategoriasDialog(categoriasViewModel)
        }

        if (mostrarMoneda) {
            MonedaDialog(
                actual = ajustes.moneda,
                onElegir = { settingsViewModel.setMoneda(it); mostrarMoneda = false },
                onCancelar = { mostrarMoneda = false }
            )
        }
        if (mostrarHora) {
            HoraDialog(
                hora = ajustes.recordatorioHora,
                minuto = ajustes.recordatorioMinuto,
                onElegir = { hora, minuto -> settingsViewModel.setRecordatorio(true, hora, minuto); mostrarHora = false },
                onCancelar = { mostrarHora = false }
            )
        }
        if (mostrarDiaInicio) {
            DiaInicioMesDialog(
                actual = ajustes.diaInicioMes,
                onElegir = { settingsViewModel.setDiaInicioMes(it); mostrarDiaInicio = false },
                onCancelar = { mostrarDiaInicio = false }
            )
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
