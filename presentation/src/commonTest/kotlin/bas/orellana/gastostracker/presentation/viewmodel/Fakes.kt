package bas.orellana.gastostracker.presentation.viewmodel

import bas.orellana.gastostracker.domain.model.CategoriaDestino
import bas.orellana.gastostracker.domain.model.CategoriaIngreso
import bas.orellana.gastostracker.domain.model.CategoriaPersonalizada
import bas.orellana.gastostracker.domain.model.GastoModel
import bas.orellana.gastostracker.domain.model.IngresoModel
import bas.orellana.gastostracker.domain.model.TotalPorCategoria
import bas.orellana.gastostracker.domain.repository.CategoriasRepository
import bas.orellana.gastostracker.domain.repository.GastoRepository
import bas.orellana.gastostracker.domain.repository.IngresoRepository
import bas.orellana.gastostracker.domain.repository.PresupuestoRepository
import bas.orellana.gastostracker.domain.repository.RecurrenteRepository
import bas.orellana.gastostracker.domain.repository.AhorroRepository
import bas.orellana.gastostracker.domain.repository.CuentaRepository
import bas.orellana.gastostracker.domain.repository.AjustesRepository
import bas.orellana.gastostracker.domain.model.Ajustes
import bas.orellana.gastostracker.domain.model.Cuenta
import bas.orellana.gastostracker.domain.model.SaldoCuenta
import bas.orellana.gastostracker.domain.model.Aportacion
import bas.orellana.gastostracker.domain.model.MetaAhorro
import bas.orellana.gastostracker.domain.model.ProgresoMeta
import kotlinx.coroutines.flow.combine
import bas.orellana.gastostracker.domain.model.Recurrente
import bas.orellana.gastostracker.domain.model.TipoMovimiento
import bas.orellana.gastostracker.domain.model.gastoEn
import bas.orellana.gastostracker.domain.model.ingresoEn
import bas.orellana.gastostracker.domain.model.Presupuesto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

/** Repositorios en memoria que se comportan como los de Room (mismo orden y mismas reglas). */
class FakeGastoRepository : GastoRepository {
    val datos = MutableStateFlow<List<GastoModel>>(emptyList())

    // Como Room: más reciente primero y, en el mismo día, el último añadido primero.
    override fun getGastos(): Flow<List<GastoModel>> =
        datos.map { gastos -> gastos.asReversed().sortedByDescending { it.fecha } }

    override fun getGastosEntre(desde: LocalDate, hasta: LocalDate): Flow<List<GastoModel>> =
        getGastos().map { gastos -> gastos.filter { it.fecha in desde..hasta } }

    override fun getTotalesPorCategoria(desde: LocalDate, hasta: LocalDate): Flow<List<TotalPorCategoria>> =
        getGastosEntre(desde, hasta).map { gastos ->
            gastos.groupBy { it.categoria to it.categoriaPersonalizadaId }
                .map { (clave, grupo) -> TotalPorCategoria(clave.first, clave.second, grupo.sumOf { it.monto }) }
        }

    override suspend fun addGasto(gasto: GastoModel) {
        datos.value = datos.value.filterNot { it.id == gasto.id } + gasto
    }

    override suspend fun updateGasto(gasto: GastoModel) {
        datos.value = datos.value.map { if (it.id == gasto.id) gasto else it }
    }

    override suspend fun deleteGasto(id: String) {
        datos.value = datos.value.filterNot { it.id == id }
    }

    override suspend fun deleteAllGastos() {
        datos.value = emptyList()
    }
}

class FakeIngresoRepository : IngresoRepository {
    val datos = MutableStateFlow<List<IngresoModel>>(emptyList())

    override fun getIngresos(): Flow<List<IngresoModel>> =
        datos.map { ingresos -> ingresos.asReversed().sortedByDescending { it.fecha } }

    override fun getIngresosEntre(desde: LocalDate, hasta: LocalDate): Flow<List<IngresoModel>> =
        getIngresos().map { ingresos -> ingresos.filter { it.fecha in desde..hasta } }

    override fun getTotalIngresos(desde: LocalDate, hasta: LocalDate): Flow<Double> =
        getIngresosEntre(desde, hasta).map { ingresos -> ingresos.sumOf { it.monto } }

    override suspend fun addIngreso(ingreso: IngresoModel) {
        datos.value = datos.value.filterNot { it.id == ingreso.id } + ingreso
    }

    override suspend fun updateIngreso(ingreso: IngresoModel) {
        datos.value = datos.value.map { if (it.id == ingreso.id) ingreso else it }
    }

    override suspend fun deleteIngreso(id: String) {
        datos.value = datos.value.filterNot { it.id == id }
    }
}

class FakeCategoriasRepository(
    private val gastos: FakeGastoRepository,
    private val ingresos: FakeIngresoRepository
) : CategoriasRepository {
    val datos = MutableStateFlow<List<CategoriaPersonalizada>>(emptyList())
    private var siguienteId = 0

    override fun getCategoriasPersonalizadas(): Flow<List<CategoriaPersonalizada>> = datos

    override suspend fun addCategoriaPersonalizada(nombre: String, color: Long) {
        datos.value = datos.value + CategoriaPersonalizada("cat-${siguienteId++}", nombre, color)
    }

    override suspend fun updateCategoriaPersonalizada(categoria: CategoriaPersonalizada) {
        datos.value = datos.value.map { if (it.id == categoria.id) categoria else it }
    }

    override suspend fun contarMovimientos(id: String): Int =
        gastos.datos.value.count { it.categoriaPersonalizadaId == id } +
            ingresos.datos.value.count { it.categoriaPersonalizadaId == id }

    override suspend fun deleteCategoriaPersonalizada(id: String, destino: CategoriaDestino) {
        val (categoria, personalizada) = when (destino) {
            is CategoriaDestino.Predefinida -> destino.categoria to null
            is CategoriaDestino.Personalizada -> null to destino.id
        }
        gastos.datos.value = gastos.datos.value.map {
            if (it.categoriaPersonalizadaId == id) it.copy(categoria = categoria, categoriaPersonalizadaId = personalizada) else it
        }
        ingresos.datos.value = ingresos.datos.value.map {
            if (it.categoriaPersonalizadaId == id) it.copy(categoria = categoria?.let { c -> CategoriaIngreso.equivalente(c) }, categoriaPersonalizadaId = personalizada) else it
        }
        datos.value = datos.value.filterNot { it.id == id }
    }
}

class FakePresupuestoRepository : PresupuestoRepository {
    val datos = MutableStateFlow<List<Presupuesto>>(emptyList())

    override fun getPresupuestos(): Flow<List<Presupuesto>> = datos

    override suspend fun guardarPresupuesto(presupuesto: Presupuesto) {
        val existe = datos.value.any { it.clave == presupuesto.clave }
        datos.value = if (existe) datos.value.map { if (it.clave == presupuesto.clave) presupuesto else it }
        else datos.value + presupuesto
    }

    override suspend fun eliminarPresupuesto(clave: String) {
        datos.value = datos.value.filterNot { it.clave == clave }
    }
}

/** Genera como el repositorio de Room, escribiendo en los repositorios falsos de gastos e ingresos. */
class FakeRecurrenteRepository(
    private val gastos: FakeGastoRepository,
    private val ingresos: FakeIngresoRepository
) : RecurrenteRepository {
    val datos = MutableStateFlow<List<Recurrente>>(emptyList())

    override fun getRecurrentes(): Flow<List<Recurrente>> = datos

    override suspend fun guardarRecurrente(recurrente: Recurrente) {
        datos.value = datos.value.filterNot { it.id == recurrente.id } + recurrente
    }

    override suspend fun eliminarRecurrente(id: String) {
        datos.value = datos.value.filterNot { it.id == id }
    }

    override suspend fun generarPendientes(hoy: LocalDate): Int {
        var creados = 0
        datos.value = datos.value.map { recurrente ->
            val generacion = recurrente.ocurrenciasPendientes(hoy)
            generacion.meses.forEach { mes ->
                when (recurrente.tipo) {
                    TipoMovimiento.GASTO -> if (gastos.datos.value.none { it.id == recurrente.idMovimiento(mes) }) gastos.addGasto(recurrente.gastoEn(mes))
                    TipoMovimiento.INGRESO -> if (ingresos.datos.value.none { it.id == recurrente.idMovimiento(mes) }) ingresos.addIngreso(recurrente.ingresoEn(mes))
                }
                creados++
            }
            recurrente.copy(ultimoMesGenerado = generacion.ultimoMesRevisado)
        }
        return creados
    }
}

class FakeAhorroRepository : AhorroRepository {
    val metas = MutableStateFlow<List<MetaAhorro>>(emptyList())
    val aportaciones = MutableStateFlow<List<Aportacion>>(emptyList())

    override fun getProgresoMetas(): Flow<List<ProgresoMeta>> =
        combine(metas, aportaciones) { lista, aportes ->
            lista.map { meta -> ProgresoMeta(meta, aportes.filter { it.metaId == meta.id }.sumOf { it.monto }) }
                .sortedBy { it.meta.archivada }
        }

    override fun getAportaciones(metaId: String): Flow<List<Aportacion>> =
        aportaciones.map { lista -> lista.filter { it.metaId == metaId }.sortedByDescending { it.fecha } }

    override fun getTodasLasAportaciones(): Flow<List<Aportacion>> = aportaciones.map { it.sortedBy { a -> a.fecha } }

    override fun getTotalAhorrado(desde: LocalDate, hasta: LocalDate): Flow<Double> =
        aportaciones.map { lista -> lista.filter { it.fecha in desde..hasta }.sumOf { it.monto } }

    override suspend fun guardarMeta(meta: MetaAhorro) {
        metas.value = if (metas.value.any { it.id == meta.id }) metas.value.map { if (it.id == meta.id) meta else it }
        else metas.value + meta
    }

    override suspend fun eliminarMeta(id: String) {
        aportaciones.value = aportaciones.value.filterNot { it.metaId == id }
        metas.value = metas.value.filterNot { it.id == id }
    }

    override suspend fun guardarAportacion(aportacion: Aportacion) {
        aportaciones.value = aportaciones.value.filterNot { it.id == aportacion.id } + aportacion
    }

    override suspend fun eliminarAportacion(id: String) {
        aportaciones.value = aportaciones.value.filterNot { it.id == id }
    }
}

class FakeCuentaRepository(private val gastos: FakeGastoRepository, private val ingresos: FakeIngresoRepository) : CuentaRepository {
    val cuentas = MutableStateFlow<List<Cuenta>>(emptyList())

    override fun getSaldos(): Flow<List<SaldoCuenta>> = combine(cuentas, gastos.datos, ingresos.datos) { lista, g, i ->
        lista.map { cuenta ->
            SaldoCuenta(
                cuenta,
                cuenta.saldoInicial + i.filter { it.cuentaId == cuenta.id }.sumOf { it.monto } -
                    g.filter { it.cuentaId == cuenta.id }.sumOf { it.monto }
            )
        }
    }

    override suspend fun guardarCuenta(cuenta: Cuenta) {
        cuentas.value = cuentas.value.filterNot { it.id == cuenta.id } + cuenta
    }

    override suspend fun eliminarCuenta(id: String) {
        gastos.datos.value = gastos.datos.value.map { if (it.cuentaId == id) it.copy(cuentaId = null) else it }
        ingresos.datos.value = ingresos.datos.value.map { if (it.cuentaId == id) it.copy(cuentaId = null) else it }
        cuentas.value = cuentas.value.filterNot { it.id == id }
    }
}

class FakeAjustesRepository : AjustesRepository {
    val datos = MutableStateFlow(Ajustes())
    override val ajustes: Flow<Ajustes> = datos

    override suspend fun setTemaOscuro(activo: Boolean) { datos.value = datos.value.copy(temaOscuro = activo) }
    override suspend fun setMoneda(codigo: String) { datos.value = datos.value.copy(moneda = codigo) }
    override suspend fun setDiaInicioMes(dia: Int) { datos.value = datos.value.copy(diaInicioMes = dia) }
    override suspend fun setRecordatorio(activo: Boolean, hora: Int, minuto: Int) {
        datos.value = datos.value.copy(recordatorioActivo = activo, recordatorioHora = hora, recordatorioMinuto = minuto)
    }
    override suspend fun setBloqueo(activo: Boolean) { datos.value = datos.value.copy(bloqueoActivo = activo) }
    override suspend fun setUltimoResumenVisto(mes: String) { datos.value = datos.value.copy(ultimoResumenVisto = mes) }
}
