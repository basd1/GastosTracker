package bas.orellana.gastostracker.domain.usecase

import bas.orellana.gastostracker.domain.model.Categoria
import bas.orellana.gastostracker.domain.model.CategoriaDestino
import bas.orellana.gastostracker.domain.model.CategoriaPersonalizada
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CategoriaUseCasesTest {

    @Test
    fun `AddCategoriaPersonalizadaUseCase crea una categoria con el nombre y color dados`() = runTest {
        val repository = FakeCategoriasRepository()
        val useCase = AddCategoriaPersonalizadaUseCase(repository)

        useCase("Mascotas", 0xFFEF9A9A)

        val creada = repository.getCategoriasPersonalizadas().first().single()
        assertEquals("Mascotas", creada.nombre)
        assertEquals(0xFFEF9A9A, creada.color)
    }

    @Test
    fun `UpdateCategoriaPersonalizadaUseCase actualiza la categoria existente`() = runTest {
        val repository = FakeCategoriasRepository()
        repository.addCategoriaPersonalizada("Mascotas", 0xFFEF9A9A)
        val id = repository.getCategoriasPersonalizadas().first().single().id
        val useCase = UpdateCategoriaPersonalizadaUseCase(repository)

        useCase(CategoriaPersonalizada(id = id, nombre = "Veterinario", color = 0xFF80DEEA))

        val actualizada = repository.getCategoriasPersonalizadas().first().single()
        assertEquals("Veterinario", actualizada.nombre)
    }

    @Test
    fun `DeleteCategoriaPersonalizadaUseCase elimina la categoria y reasigna a Otros por defecto`() = runTest {
        val repository = FakeCategoriasRepository()
        repository.addCategoriaPersonalizada("Mascotas", 0xFFEF9A9A)
        val id = repository.getCategoriasPersonalizadas().first().single().id
        val useCase = DeleteCategoriaPersonalizadaUseCase(repository)

        useCase(id)

        assertTrue(repository.getCategoriasPersonalizadas().first().isEmpty())
        assertEquals(id to CategoriaDestino.Predefinida(Categoria.OTROS), repository.ultimaEliminacion)
    }

    @Test
    fun `DeleteCategoriaPersonalizadaUseCase reasigna a la categoria elegida`() = runTest {
        val repository = FakeCategoriasRepository()
        repository.addCategoriaPersonalizada("Mascotas", 0xFFEF9A9A)
        repository.addCategoriaPersonalizada("Animales", 0xFF80DEEA)
        val (mascotas, animales) = repository.getCategoriasPersonalizadas().first()
        val useCase = DeleteCategoriaPersonalizadaUseCase(repository)

        useCase(mascotas.id, CategoriaDestino.Personalizada(animales.id))

        assertEquals(listOf(animales), repository.getCategoriasPersonalizadas().first())
        assertEquals(mascotas.id to CategoriaDestino.Personalizada(animales.id), repository.ultimaEliminacion)
    }

    @Test
    fun `DeleteCategoriaPersonalizadaUseCase no permite reasignar a la misma categoria`() = runTest {
        val repository = FakeCategoriasRepository()
        repository.addCategoriaPersonalizada("Mascotas", 0xFFEF9A9A)
        val id = repository.getCategoriasPersonalizadas().first().single().id
        val useCase = DeleteCategoriaPersonalizadaUseCase(repository)

        assertFailsWith<IllegalArgumentException> { useCase(id, CategoriaDestino.Personalizada(id)) }
        assertEquals(1, repository.getCategoriasPersonalizadas().first().size)
    }

    @Test
    fun `ContarMovimientosCategoriaUseCase devuelve los movimientos del repositorio`() = runTest {
        val repository = FakeCategoriasRepository()
        repository.movimientosPorCategoria["7"] = 3
        val useCase = ContarMovimientosCategoriaUseCase(repository)

        assertEquals(3, useCase("7"))
        assertEquals(0, useCase("otra"))
    }

    @Test
    fun `GetCategoriasPersonalizadasUseCase expone el flujo del repositorio`() = runTest {
        val repository = FakeCategoriasRepository()
        repository.addCategoriaPersonalizada("Mascotas", 0xFFEF9A9A)
        val useCase = GetCategoriasPersonalizadasUseCase(repository)

        assertEquals(repository.getCategoriasPersonalizadas().first(), useCase().first())
    }
}
