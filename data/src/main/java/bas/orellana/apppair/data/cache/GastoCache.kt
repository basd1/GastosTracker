package bas.orellana.apppair.data.cache

import bas.orellana.apppair.domain.model.GastoModel
import java.time.LocalDate

object GastoCache {
    private val gastos = mutableListOf(
        GastoModel(
            id = "1",
            nombre = "Cena",
            monto = 45.50,
            fecha = LocalDate.now()
        ),
        GastoModel(
            id = "2",
            nombre = "Uber",
            monto = 12.00,
            fecha = LocalDate.now().minusDays(1)
        ),
        GastoModel(
            id = "3",
            nombre = "Supermercado",
            monto = 85.30,
            fecha = LocalDate.now().minusDays(2)
        )
    )

    fun getAll(): List<GastoModel> = gastos.toList()

    fun add(gasto: GastoModel) {
        gastos.add(gasto)
    }

    fun delete(id: String) {
        gastos.removeAll { it.id == id }
    }
}