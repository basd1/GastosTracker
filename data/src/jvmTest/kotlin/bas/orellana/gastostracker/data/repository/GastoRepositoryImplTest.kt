package bas.orellana.gastostracker.data.repository

import bas.orellana.gastostracker.data.EntornoDatos
import bas.orellana.gastostracker.domain.model.Categoria
import bas.orellana.gastostracker.domain.model.GastoModel
import bas.orellana.gastostracker.domain.model.TotalPorCategoria
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GastoRepositoryImplTest {

    private val entorno = EntornoDatos()
    private val repo = entorno.gastos

    @AfterTest
    fun cerrar() = entorno.close()

    private fun gasto(
        id: String,
        monto: Double,
        fecha: LocalDate,
        categoria: Categoria? = null,
        personalizada: String? = null
    ) = GastoModel(id, "Gasto $id", monto, fecha, categoria, personalizada)

    @Test
    fun `getGastos ordena por fecha descendente y dentro del dia el ultimo anadido primero`() = runTest {
        val dia1 = LocalDate(2026, 11, 1)
        val dia2 = LocalDate(2026, 11, 2)
        repo.addGasto(gasto("a", 10.0, dia1))
        repo.addGasto(gasto("b", 20.0, dia2))
        repo.addGasto(gasto("c", 30.0, dia1))

        assertEquals(listOf("b", "c", "a"), repo.getGastos().first().map { it.id })
    }

    @Test
    fun `addGasto conserva todos los campos`() = runTest {
        val original = gasto("a", 12.5, LocalDate(2026, 11, 8), personalizada = "cat-1")
        repo.addGasto(original)

        assertEquals(listOf(original), repo.getGastos().first())
    }

    @Test
    fun `getGastosEntre incluye los dos extremos del rango`() = runTest {
        repo.addGasto(gasto("antes", 1.0, LocalDate(2026, 10, 31)))
        repo.addGasto(gasto("inicio", 1.0, LocalDate(2026, 11, 1)))
        repo.addGasto(gasto("fin", 1.0, LocalDate(2026, 11, 30)))
        repo.addGasto(gasto("despues", 1.0, LocalDate(2026, 12, 1)))

        val delMes = repo.getGastosEntre(LocalDate(2026, 11, 1), LocalDate(2026, 11, 30)).first()

        assertEquals(setOf("inicio", "fin"), delMes.map { it.id }.toSet())
    }

    @Test
    fun `getTotalesPorCategoria suma por categoria solo dentro del rango`() = runTest {
        val noviembre = LocalDate(2026, 11, 10)
        repo.addGasto(gasto("1", 10.0, noviembre, Categoria.COMIDA))
        repo.addGasto(gasto("2", 15.5, noviembre, Categoria.COMIDA))
        repo.addGasto(gasto("3", 7.0, noviembre, personalizada = "cat-1"))
        repo.addGasto(gasto("4", 3.0, noviembre))
        repo.addGasto(gasto("5", 99.0, LocalDate(2026, 10, 10), Categoria.COMIDA))

        val totales = repo.getTotalesPorCategoria(LocalDate(2026, 11, 1), LocalDate(2026, 11, 30)).first()

        assertEquals(
            setOf(
                TotalPorCategoria(Categoria.COMIDA, null, 25.5),
                TotalPorCategoria(null, "cat-1", 7.0),
                TotalPorCategoria(null, null, 3.0)
            ),
            totales.toSet()
        )
    }

    @Test
    fun `updateGasto modifica el gasto existente`() = runTest {
        val original = gasto("a", 10.0, LocalDate(2026, 11, 1), Categoria.OCIO)
        repo.addGasto(original)

        repo.updateGasto(original.copy(nombre = "Cine", monto = 8.0, categoria = Categoria.SALUD))

        val actualizado = repo.getGastos().first().single()
        assertEquals("Cine", actualizado.nombre)
        assertEquals(8.0, actualizado.monto)
        assertEquals(Categoria.SALUD, actualizado.categoria)
    }

    @Test
    fun `deleteGasto borra solo ese gasto y deleteAllGastos los borra todos`() = runTest {
        repo.addGasto(gasto("a", 1.0, LocalDate(2026, 11, 1)))
        repo.addGasto(gasto("b", 2.0, LocalDate(2026, 11, 2)))

        repo.deleteGasto("a")
        assertEquals(listOf("b"), repo.getGastos().first().map { it.id })

        repo.deleteAllGastos()
        assertTrue(repo.getGastos().first().isEmpty())
    }

    @Test
    fun `restaurar un gasto borrado lo devuelve con el mismo id`() = runTest {
        val original = gasto("a", 5.0, LocalDate(2026, 11, 1))
        repo.addGasto(original)
        repo.deleteGasto("a")

        repo.addGasto(original)

        assertEquals(listOf(original), repo.getGastos().first())
    }
}
