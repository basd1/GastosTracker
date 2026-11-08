package bas.orellana.gastostracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bas.orellana.gastostracker.domain.repository.CopiaSeguridadInvalida
import bas.orellana.gastostracker.domain.repository.ResumenCopia
import bas.orellana.gastostracker.domain.usecase.ExportarCopiaSeguridadUseCase
import bas.orellana.gastostracker.domain.usecase.GetCategoriasPersonalizadasUseCase
import bas.orellana.gastostracker.domain.usecase.GetGastosUseCase
import bas.orellana.gastostracker.domain.usecase.GetIngresosUseCase
import bas.orellana.gastostracker.domain.usecase.GetProgresoMetasUseCase
import bas.orellana.gastostracker.domain.usecase.GetTodasLasAportacionesUseCase
import bas.orellana.gastostracker.domain.usecase.ImportarCopiaSeguridadUseCase
import bas.orellana.gastostracker.domain.util.todayLocalDate
import bas.orellana.gastostracker.presentation.resources.Res
import bas.orellana.gastostracker.presentation.resources.csv_categoria
import bas.orellana.gastostracker.presentation.resources.csv_concepto
import bas.orellana.gastostracker.presentation.resources.csv_fecha
import bas.orellana.gastostracker.presentation.resources.csv_importe
import bas.orellana.gastostracker.presentation.resources.csv_metodo_pago
import bas.orellana.gastostracker.presentation.resources.csv_nota
import bas.orellana.gastostracker.presentation.resources.csv_tipo
import bas.orellana.gastostracker.presentation.resources.csv_tipo_ahorro
import bas.orellana.gastostracker.presentation.resources.csv_tipo_gasto
import bas.orellana.gastostracker.presentation.resources.csv_tipo_ingreso
import bas.orellana.gastostracker.presentation.resources.sin_categoria
import bas.orellana.gastostracker.presentation.ui.components.nombreRes
import bas.orellana.gastostracker.presentation.util.AlmacenTickets
import bas.orellana.gastostracker.presentation.util.CopiaLeida
import bas.orellana.gastostracker.presentation.util.FilaCsv
import bas.orellana.gastostracker.presentation.util.MIME_ZIP
import bas.orellana.gastostracker.presentation.util.generarCsv
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString

/** Archivo listo para que la pantalla abra el selector de "Guardar como". En la copia, [fotos] son las fotos de tickets que van dentro. */
data class ArchivoParaGuardar(val nombre: String, val mime: String, val contenido: String, val fotos: List<String> = emptyList())

sealed interface AvisoDatos {
    data object CsvGuardado : AvisoDatos
    data object CopiaGuardada : AvisoDatos
    data class Restaurada(val resumen: ResumenCopia) : AvisoDatos
    data object NoGuardado : AvisoDatos
    data class CopiaInvalida(val detalle: String?) : AvisoDatos
    data object ErrorInesperado : AvisoDatos
}

data class DatosState(
    val trabajando: Boolean = false,
    val archivoPendiente: ArchivoParaGuardar? = null,
    /** Contenido de la copia elegida, a la espera de que el usuario confirme que quiere sustituirlo todo. */
    val copiaPorRestaurar: CopiaLeida? = null,
    val aviso: AvisoDatos? = null
)

class DatosViewModel(
    private val getGastosUseCase: GetGastosUseCase,
    private val getIngresosUseCase: GetIngresosUseCase,
    private val getCategoriasPersonalizadasUseCase: GetCategoriasPersonalizadasUseCase,
    private val getTodasLasAportacionesUseCase: GetTodasLasAportacionesUseCase,
    private val getProgresoMetasUseCase: GetProgresoMetasUseCase,
    private val exportarCopiaSeguridadUseCase: ExportarCopiaSeguridadUseCase,
    private val importarCopiaSeguridadUseCase: ImportarCopiaSeguridadUseCase,
    private val almacenTickets: AlmacenTickets
) : ViewModel() {

    private val _state = MutableStateFlow(DatosState())
    val state: StateFlow<DatosState> = _state.asStateFlow()

    fun exportarCsv() = trabajar {
        val contenido = construirCsv()
        _state.update {
            it.copy(archivoPendiente = ArchivoParaGuardar("gastostracker-${todayLocalDate()}.csv", MIME_CSV, contenido))
        }
    }

    fun exportarCopia() = trabajar {
        val contenido = exportarCopiaSeguridadUseCase()
        val fotos = getGastosUseCase().first().mapNotNull { it.fotoTicket }
        _state.update {
            it.copy(archivoPendiente = ArchivoParaGuardar("gastostracker-copia-${todayLocalDate()}.zip", MIME_COPIA, contenido, fotos))
        }
    }

    /** La pantalla ya ha abierto el selector con el archivo pendiente. */
    fun archivoEntregado() = _state.update { it.copy(archivoPendiente = null) }

    fun resultadoGuardado(mime: String, guardado: Boolean) = _state.update {
        it.copy(
            aviso = when {
                !guardado -> AvisoDatos.NoGuardado
                mime == MIME_CSV -> AvisoDatos.CsvGuardado
                else -> AvisoDatos.CopiaGuardada
            }
        )
    }

    /** El usuario ha elegido un archivo para restaurar: hay que pedir confirmación antes de sustituir nada. */
    fun copiaElegida(copia: CopiaLeida?) {
        if (copia == null) return
        _state.update { it.copy(copiaPorRestaurar = copia) }
    }

    fun cancelarRestaurar() = _state.update { it.copy(copiaPorRestaurar = null) }

    fun confirmarRestaurar() {
        val copia = _state.value.copiaPorRestaurar ?: return
        _state.update { it.copy(copiaPorRestaurar = null) }
        trabajar {
            val resumen = importarCopiaSeguridadUseCase(copia.json)
            // Las fotos, después de los datos: si la copia no es válida no se toca nada.
            if (copia.fotos > 0) almacenTickets.instalarFotosRestauradas()
            _state.update { it.copy(aviso = AvisoDatos.Restaurada(resumen)) }
        }
    }

    fun avisoMostrado() = _state.update { it.copy(aviso = null) }

    private fun trabajar(accion: suspend () -> Unit) {
        if (_state.value.trabajando) return
        _state.update { it.copy(trabajando = true) }
        viewModelScope.launch {
            try {
                accion()
            } catch (e: CancellationException) {
                throw e
            } catch (e: CopiaSeguridadInvalida) {
                _state.update { it.copy(aviso = AvisoDatos.CopiaInvalida(e.message)) }
            } catch (_: Exception) {
                _state.update { it.copy(aviso = AvisoDatos.ErrorInesperado) }
            } finally {
                _state.update { it.copy(trabajando = false) }
            }
        }
    }

    private suspend fun construirCsv(): String {
        val personalizadas = getCategoriasPersonalizadasUseCase().first().associate { it.id to it.nombre }
        val metas = getProgresoMetasUseCase().first().associate { it.meta.id to it.meta.nombre }
        val sinCategoria = getString(Res.string.sin_categoria)
        val nombresPredefinidas = mutableMapOf<String, String>()
        suspend fun nombreCategoria(categoria: bas.orellana.gastostracker.domain.model.Categoria?, personalizadaId: String?): String =
            when {
                categoria != null -> nombresPredefinidas.getOrPut(categoria.name) { getString(categoria.nombreRes) }
                personalizadaId != null -> personalizadas[personalizadaId] ?: sinCategoria
                else -> sinCategoria
            }

        val tipoGasto = getString(Res.string.csv_tipo_gasto)
        val tipoIngreso = getString(Res.string.csv_tipo_ingreso)
        val tipoAhorro = getString(Res.string.csv_tipo_ahorro)
        val filas = buildList {
            getGastosUseCase().first().forEach {
                add(
                    FilaCsv(
                        it.fecha, tipoGasto, it.nombre, nombreCategoria(it.categoria, it.categoriaPersonalizadaId), -it.monto,
                        nota = it.nota.orEmpty(),
                        metodoPago = it.metodoPago?.let { metodo -> getString(metodo.nombreRes) }.orEmpty()
                    )
                )
            }
            getIngresosUseCase().first().forEach {
                val categoriaIngreso = it.categoria
                val nombre = if (categoriaIngreso != null) getString(categoriaIngreso.nombreRes) else nombreCategoria(null, it.categoriaPersonalizadaId)
                add(FilaCsv(it.fecha, tipoIngreso, it.nombre, nombre, it.monto, nota = it.nota.orEmpty()))
            }
            getTodasLasAportacionesUseCase().first().forEach {
                add(FilaCsv(it.fecha, tipoAhorro, it.nota.orEmpty(), metas[it.metaId].orEmpty(), -it.monto))
            }
        }
        val cabecera = listOf(
            getString(Res.string.csv_fecha),
            getString(Res.string.csv_tipo),
            getString(Res.string.csv_concepto),
            getString(Res.string.csv_categoria),
            getString(Res.string.csv_importe),
            getString(Res.string.csv_nota),
            getString(Res.string.csv_metodo_pago)
        )
        return generarCsv(cabecera, filas)
    }

    companion object {
        const val MIME_CSV = "text/csv"
        const val MIME_COPIA = MIME_ZIP
    }
}
