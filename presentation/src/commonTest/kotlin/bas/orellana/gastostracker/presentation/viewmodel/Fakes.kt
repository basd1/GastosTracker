package bas.orellana.gastostracker.presentation.viewmodel

import bas.orellana.gastostracker.domain.model.CategoriaDestino
import bas.orellana.gastostracker.domain.model.CategoriaPersonalizada
import bas.orellana.gastostracker.domain.model.GastoModel
import bas.orellana.gastostracker.domain.model.IngresoModel
import bas.orellana.gastostracker.domain.model.TotalPorCategoria
import bas.orellana.gastostracker.domain.repository.CategoriasRepository
import bas.orellana.gastostracker.domain.repository.GastoRepository
import bas.orellana.gastostracker.domain.repository.IngresoRepository
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
            if (it.categoriaPersonalizadaId == id) it.copy(categoria = categoria, categoriaPersonalizadaId = personalizada) else it
        }
        datos.value = datos.value.filterNot { it.id == id }
    }
}
