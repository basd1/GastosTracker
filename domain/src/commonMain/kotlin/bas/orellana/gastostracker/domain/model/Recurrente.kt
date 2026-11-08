package bas.orellana.gastostracker.domain.model

import kotlinx.datetime.LocalDate
import kotlin.math.min

enum class TipoMovimiento { GASTO, INGRESO }

/**
 * Movimiento que se repite cada mes el día [diaDelMes] (alquiler, nómina, suscripciones...).
 * Si el mes tiene menos días (p. ej. día 31 en febrero), se apunta el último día del mes.
 *
 * La app crea los movimientos pendientes al abrirse ([ocurrenciasPendientes]); [ultimoMesGenerado]
 * evita repetirlos y los ids deterministas ([idMovimiento]) impiden duplicados aunque se reintente.
 */
data class Recurrente(
    val id: String,
    val tipo: TipoMovimiento,
    val nombre: String,
    val monto: Double,
    val categoria: Categoria? = null,
    val categoriaPersonalizadaId: String? = null,
    val diaDelMes: Int,
    /** Primer día en el que puede haber un movimiento. */
    val fechaInicio: LocalDate,
    val ultimoMesGenerado: Mes? = null,
    val activo: Boolean = true
) {
    init {
        require(diaDelMes in 1..31) { "Día del mes fuera de rango: $diaDelMes" }
    }

    /** Fecha del movimiento en [mes]. */
    fun fechaEn(mes: Mes): LocalDate = LocalDate(mes.anio, mes.numero, min(diaDelMes, mes.numeroDeDias))

    fun idMovimiento(mes: Mes): String = "rec-$id-$mes"

    /**
     * Meses con movimiento pendiente de crear hasta [hoy] (incluido), y el mes hasta el que queda
     * todo revisado. Un recurrente pausado no genera nada, pero tampoco "recupera" lo que no generó
     * mientras estaba pausado: al reactivarlo se continúa desde el mes actual.
     */
    fun ocurrenciasPendientes(hoy: LocalDate): Generacion {
        val inicio = ultimoMesGenerado?.siguiente() ?: Mes.de(fechaInicio)
        var mes = inicio
        var ultimoRevisado = ultimoMesGenerado
        val pendientes = mutableListOf<Mes>()
        while (fechaEn(mes) <= hoy) {
            if (activo && fechaEn(mes) >= fechaInicio) pendientes += mes
            ultimoRevisado = mes
            mes = mes.siguiente()
        }
        return Generacion(pendientes, ultimoRevisado)
    }

    /** Lo que cuesta (o aporta) al mes. */
    val importeMensual: Double get() = if (activo) monto else 0.0

    data class Generacion(val meses: List<Mes>, val ultimoMesRevisado: Mes?)
}

fun Recurrente.gastoEn(mes: Mes): GastoModel = GastoModel(
    id = idMovimiento(mes),
    nombre = nombre,
    monto = monto,
    fecha = fechaEn(mes),
    categoria = categoria,
    categoriaPersonalizadaId = categoriaPersonalizadaId
)

fun Recurrente.ingresoEn(mes: Mes): IngresoModel = IngresoModel(
    id = idMovimiento(mes),
    nombre = nombre,
    monto = monto,
    fecha = fechaEn(mes),
    categoria = categoria,
    categoriaPersonalizadaId = categoriaPersonalizadaId
)
