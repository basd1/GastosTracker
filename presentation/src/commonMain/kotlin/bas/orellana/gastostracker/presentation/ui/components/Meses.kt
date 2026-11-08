package bas.orellana.gastostracker.presentation.ui.components

import androidx.compose.runtime.Composable
import bas.orellana.gastostracker.presentation.resources.Res
import bas.orellana.gastostracker.presentation.resources.mes_y_anio
import bas.orellana.gastostracker.presentation.resources.meses
import bas.orellana.gastostracker.presentation.resources.meses_cortos
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource

/** Nombre completo del mes ([mes] de 1 a 12). */
@Composable
fun nombreMes(mes: Int): String = stringArrayResource(Res.array.meses)[mes - 1]

/** Nombre abreviado del mes ([mes] de 1 a 12). */
@Composable
fun nombreMesCorto(mes: Int): String = stringArrayResource(Res.array.meses_cortos)[mes - 1]

/** "Noviembre 2026". */
@Composable
fun mesYAnio(mes: Int, anio: Int): String = stringResource(Res.string.mes_y_anio, nombreMes(mes), anio)
