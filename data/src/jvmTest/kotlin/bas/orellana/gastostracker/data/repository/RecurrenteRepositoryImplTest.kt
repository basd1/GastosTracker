package bas.orellana.gastostracker.data.repository

import bas.orellana.gastostracker.data.EntornoDatos
import bas.orellana.gastostracker.domain.model.Categoria
import bas.orellana.gastostracker.domain.model.CategoriaDestino
import bas.orellana.gastostracker.domain.model.Mes
import bas.orellana.gastostracker.domain.model.Recurrente
import bas.orellana.gastostracker.domain.model.TipoMovimiento
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RecurrenteRepositoryImplTest {

    private val entorno = EntornoDatos()
    private val repo = entorno.recurrentes

    @AfterTest
    fun cerrar() = entorno.close()

    private fun alquiler(id: String = "alquiler", personalizada: String? = null) = Recurrente(
        id = id,
        tipo = TipoMovimiento.GASTO,
        nombre = "Alquiler",
        monto = 700.0,
        categoria = if (personalizada == null) Categoria.HOGAR else null,
        categoriaPersonalizadaId = personalizada,
        diaDelMes = 1,
        fechaInicio = LocalDate(2026, 9, 1)
    )

    @Test
    fun `genera los gastos e ingresos pendientes y no los repite`() = runTest {
        repo.guardarRecurrente(alquiler())
        repo.guardarRecurrente(
            Recurrente("nomina", TipoMovimiento.INGRESO, "Nómina", 1800.0, diaDelMes = 28, fechaInicio = LocalDate(2026, 10, 28))
        )

        assertEquals(4, repo.generarPendientes(LocalDate(2026, 11, 8)))
        assertEquals(0, repo.generarPendientes(LocalDate(2026, 11, 8)))

        val gastos = entorno.gastos.getGastos().first()
        assertEquals(listOf("rec-alquiler-2026-11", "rec-alquiler-2026-10", "rec-alquiler-2026-09"), gastos.map { it.id })
        assertTrue(gastos.all { it.categoria == Categoria.HOGAR && it.monto == 700.0 })
        assertEquals(listOf(LocalDate(2026, 10, 28)), entorno.ingresos.getIngresos().first().map { it.fecha })
        assertEquals(Mes(2026, 11), repo.getRecurrentes().first().single { it.id == "alquiler" }.ultimoMesGenerado)
    }

    @Test
    fun `un gasto generado que se borra no vuelve a aparecer`() = runTest {
        repo.guardarRecurrente(alquiler())
        repo.generarPendientes(LocalDate(2026, 9, 2))
        entorno.gastos.deleteGasto("rec-alquiler-2026-09")

        repo.generarPendientes(LocalDate(2026, 9, 30))

        assertTrue(entorno.gastos.getGastos().first().isEmpty())
    }

    @Test
    fun `eliminar el recurrente conserva los movimientos ya creados`() = runTest {
        repo.guardarRecurrente(alquiler())
        repo.generarPendientes(LocalDate(2026, 9, 2))
        repo.eliminarRecurrente("alquiler")

        assertTrue(repo.getRecurrentes().first().isEmpty())
        assertEquals(1, entorno.gastos.getGastos().first().size)
    }

    @Test
    fun `al borrar su categoria personalizada el recurrente pasa al destino`() = runTest {
        entorno.categorias.addCategoriaPersonalizada("Casa", 0xFFEF9A9A)
        val id = entorno.categorias.getCategoriasPersonalizadas().first().single().id
        repo.guardarRecurrente(alquiler(personalizada = id))

        entorno.categorias.deleteCategoriaPersonalizada(id, CategoriaDestino.Predefinida(Categoria.HOGAR))

        val recurrente = repo.getRecurrentes().first().single()
        assertEquals(Categoria.HOGAR, recurrente.categoria)
        assertEquals(null, recurrente.categoriaPersonalizadaId)
    }
}
