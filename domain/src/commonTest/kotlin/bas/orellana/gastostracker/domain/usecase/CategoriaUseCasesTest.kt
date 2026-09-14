package bas.orellana.gastostracker.domain.usecase

import bas.orellana.gastostracker.domain.model.CategoriaPersonalizada
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
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
    fun `DeleteCategoriaPersonalizadaUseCase elimina la categoria indicada`() = runTest {
        val repository = FakeCategoriasRepository()
        repository.addCategoriaPersonalizada("Mascotas", 0xFFEF9A9A)
        val id = repository.getCategoriasPersonalizadas().first().single().id
        val useCase = DeleteCategoriaPersonalizadaUseCase(repository)

        useCase(id)

        assertTrue(repository.getCategoriasPersonalizadas().first().isEmpty())
    }

    @Test
    fun `GetCategoriasPersonalizadasUseCase expone el flujo del repositorio`() = runTest {
        val repository = FakeCategoriasRepository()
        repository.addCategoriaPersonalizada("Mascotas", 0xFFEF9A9A)
        val useCase = GetCategoriasPersonalizadasUseCase(repository)

        assertEquals(repository.getCategoriasPersonalizadas().first(), useCase().first())
    }
}
