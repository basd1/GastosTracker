package bas.orellana.gastostracker.data.repository

import bas.orellana.gastostracker.data.EntornoDatos
import bas.orellana.gastostracker.domain.model.Categoria
import bas.orellana.gastostracker.domain.model.CategoriaIngreso
import bas.orellana.gastostracker.domain.model.CategoriaDestino
import bas.orellana.gastostracker.domain.model.GastoModel
import bas.orellana.gastostracker.domain.model.IngresoModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CategoriasRepositoryImplTest {

    private val entorno = EntornoDatos()
    private val repo = entorno.categorias
    private val fecha = LocalDate(2026, 11, 8)

    @AfterTest
    fun cerrar() = entorno.close()

    private suspend fun crearCategoria(nombre: String): String {
        repo.addCategoriaPersonalizada(nombre, 0xFFEF9A9A)
        return repo.getCategoriasPersonalizadas().first().single { it.nombre == nombre }.id
    }

    private suspend fun gastosPorId() = entorno.gastos.getGastos().first().associateBy { it.id }
    private suspend fun ingresosPorId() = entorno.ingresos.getIngresos().first().associateBy { it.id }

    @Test
    fun `las categorias se devuelven en orden de creacion`() = runTest {
        crearCategoria("Mascotas")
        crearCategoria("Bebé")
        crearCategoria("Gimnasio")

        assertEquals(listOf("Mascotas", "Bebé", "Gimnasio"), repo.getCategoriasPersonalizadas().first().map { it.nombre })
    }

    @Test
    fun `updateCategoriaPersonalizada cambia nombre y color`() = runTest {
        val id = crearCategoria("Mascotas")
        val categoria = repo.getCategoriasPersonalizadas().first().single()

        repo.updateCategoriaPersonalizada(categoria.copy(nombre = "Animales", color = 0xFF80DEEA))

        val actualizada = repo.getCategoriasPersonalizadas().first().single()
        assertEquals(id, actualizada.id)
        assertEquals("Animales", actualizada.nombre)
        assertEquals(0xFF80DEEA, actualizada.color)
    }

    @Test
    fun `contarMovimientos suma gastos e ingresos de esa categoria`() = runTest {
        val mascotas = crearCategoria("Mascotas")
        val otra = crearCategoria("Otra")
        entorno.gastos.addGasto(GastoModel("g1", "Pienso", 20.0, fecha, categoriaPersonalizadaId = mascotas))
        entorno.gastos.addGasto(GastoModel("g2", "Vete", 50.0, fecha, categoriaPersonalizadaId = mascotas))
        entorno.gastos.addGasto(GastoModel("g3", "Otro", 5.0, fecha, categoriaPersonalizadaId = otra))
        entorno.ingresos.addIngreso(IngresoModel("i1", "Venta camada", 100.0, fecha, categoriaPersonalizadaId = mascotas))

        assertEquals(3, repo.contarMovimientos(mascotas))
        assertEquals(1, repo.contarMovimientos(otra))
        assertEquals(0, repo.contarMovimientos("no-existe"))
    }

    @Test
    fun `al borrar una categoria sus movimientos pasan a la categoria predefinida elegida`() = runTest {
        val mascotas = crearCategoria("Mascotas")
        val otra = crearCategoria("Otra")
        entorno.gastos.addGasto(GastoModel("g1", "Pienso", 20.0, fecha, categoriaPersonalizadaId = mascotas))
        entorno.gastos.addGasto(GastoModel("g2", "Otro", 5.0, fecha, categoriaPersonalizadaId = otra))
        entorno.gastos.addGasto(GastoModel("g3", "Súper", 30.0, fecha, Categoria.COMIDA))
        entorno.ingresos.addIngreso(IngresoModel("i1", "Venta", 100.0, fecha, categoriaPersonalizadaId = mascotas))

        repo.deleteCategoriaPersonalizada(mascotas, CategoriaDestino.Predefinida(Categoria.OTROS))

        assertEquals(listOf("Otra"), repo.getCategoriasPersonalizadas().first().map { it.nombre })
        val gastos = gastosPorId()
        assertEquals(Categoria.OTROS, gastos.getValue("g1").categoria)
        assertEquals(null, gastos.getValue("g1").categoriaPersonalizadaId)
        // Los movimientos de otras categorías no se tocan.
        assertEquals(otra, gastos.getValue("g2").categoriaPersonalizadaId)
        assertEquals(Categoria.COMIDA, gastos.getValue("g3").categoria)
        val ingreso = ingresosPorId().getValue("i1")
        assertEquals(CategoriaIngreso.OTROS, ingreso.categoria)
        assertEquals(null, ingreso.categoriaPersonalizadaId)
    }

    @Test
    fun `al borrar una categoria sus movimientos pueden pasar a otra personalizada`() = runTest {
        val mascotas = crearCategoria("Mascotas")
        val animales = crearCategoria("Animales")
        entorno.gastos.addGasto(GastoModel("g1", "Pienso", 20.0, fecha, categoriaPersonalizadaId = mascotas))
        entorno.ingresos.addIngreso(IngresoModel("i1", "Venta", 100.0, fecha, categoriaPersonalizadaId = mascotas))

        repo.deleteCategoriaPersonalizada(mascotas, CategoriaDestino.Personalizada(animales))

        val gasto = gastosPorId().getValue("g1")
        assertEquals(null, gasto.categoria)
        assertEquals(animales, gasto.categoriaPersonalizadaId)
        assertEquals(animales, ingresosPorId().getValue("i1").categoriaPersonalizadaId)
        assertEquals(2, repo.contarMovimientos(animales))
    }

    @Test
    fun `borrar una categoria sin movimientos solo la elimina`() = runTest {
        val mascotas = crearCategoria("Mascotas")

        repo.deleteCategoriaPersonalizada(mascotas, CategoriaDestino.POR_DEFECTO)

        assertEquals(emptyList(), repo.getCategoriasPersonalizadas().first())
    }
}
