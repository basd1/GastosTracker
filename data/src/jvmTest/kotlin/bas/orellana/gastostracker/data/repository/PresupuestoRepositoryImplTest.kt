package bas.orellana.gastostracker.data.repository

import bas.orellana.gastostracker.data.EntornoDatos
import bas.orellana.gastostracker.domain.model.Categoria
import bas.orellana.gastostracker.domain.model.CategoriaDestino
import bas.orellana.gastostracker.domain.model.Presupuesto
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PresupuestoRepositoryImplTest {

    private val entorno = EntornoDatos()
    private val repo = entorno.presupuestos

    @AfterTest
    fun cerrar() = entorno.close()

    @Test
    fun `guardar dos veces la misma categoria sustituye el importe`() = runTest {
        repo.guardarPresupuesto(Presupuesto(Categoria.COMIDA, null, 300.0))
        repo.guardarPresupuesto(Presupuesto.global(1500.0))
        repo.guardarPresupuesto(Presupuesto(Categoria.COMIDA, null, 250.0))

        assertEquals(
            listOf(Presupuesto(Categoria.COMIDA, null, 250.0), Presupuesto.global(1500.0)),
            repo.getPresupuestos().first()
        )
    }

    @Test
    fun `eliminar por clave`() = runTest {
        repo.guardarPresupuesto(Presupuesto(Categoria.OCIO, null, 100.0))
        repo.eliminarPresupuesto("cat:OCIO")

        assertTrue(repo.getPresupuestos().first().isEmpty())
    }

    @Test
    fun `al borrar una categoria personalizada se borra su presupuesto`() = runTest {
        entorno.categorias.addCategoriaPersonalizada("Mascotas", 0xFFEF9A9A)
        val id = entorno.categorias.getCategoriasPersonalizadas().first().single().id
        repo.guardarPresupuesto(Presupuesto(null, id, 40.0))
        repo.guardarPresupuesto(Presupuesto.global(900.0))

        entorno.categorias.deleteCategoriaPersonalizada(id, CategoriaDestino.POR_DEFECTO)

        assertEquals(listOf(Presupuesto.global(900.0)), repo.getPresupuestos().first())
    }
}
