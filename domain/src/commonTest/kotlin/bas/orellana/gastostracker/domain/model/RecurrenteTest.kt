package bas.orellana.gastostracker.domain.model

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RecurrenteTest {

    private fun recurrente(
        dia: Int,
        inicio: LocalDate,
        ultimo: Mes? = null,
        activo: Boolean = true
    ) = Recurrente(
        id = "r",
        tipo = TipoMovimiento.GASTO,
        nombre = "Alquiler",
        monto = 700.0,
        diaDelMes = dia,
        fechaInicio = inicio,
        ultimoMesGenerado = ultimo,
        activo = activo
    )

    @Test
    fun `genera un movimiento por cada mes desde el inicio hasta hoy`() {
        val generacion = recurrente(dia = 5, inicio = LocalDate(2026, 8, 5))
            .ocurrenciasPendientes(hoy = LocalDate(2026, 11, 8))

        assertEquals(listOf(Mes(2026, 8), Mes(2026, 9), Mes(2026, 10), Mes(2026, 11)), generacion.meses)
        assertEquals(Mes(2026, 11), generacion.ultimoMesRevisado)
    }

    @Test
    fun `el mes actual no se genera hasta que llega el dia`() {
        val generacion = recurrente(dia = 25, inicio = LocalDate(2026, 10, 25))
            .ocurrenciasPendientes(hoy = LocalDate(2026, 11, 8))

        assertEquals(listOf(Mes(2026, 10)), generacion.meses)
        assertEquals(Mes(2026, 10), generacion.ultimoMesRevisado)
    }

    @Test
    fun `si se crea despues del dia del mes, el primero es el mes siguiente`() {
        val generacion = recurrente(dia = 1, inicio = LocalDate(2026, 10, 15))
            .ocurrenciasPendientes(hoy = LocalDate(2026, 11, 8))

        assertEquals(listOf(Mes(2026, 11)), generacion.meses)
    }

    @Test
    fun `continua desde el ultimo mes generado sin repetir`() {
        val generacion = recurrente(dia = 1, inicio = LocalDate(2026, 1, 1), ultimo = Mes(2026, 10))
            .ocurrenciasPendientes(hoy = LocalDate(2026, 11, 8))

        assertEquals(listOf(Mes(2026, 11)), generacion.meses)
        assertTrue(
            recurrente(dia = 1, inicio = LocalDate(2026, 1, 1), ultimo = Mes(2026, 11))
                .ocurrenciasPendientes(hoy = LocalDate(2026, 11, 30)).meses.isEmpty()
        )
    }

    @Test
    fun `el dia 31 cae en el ultimo dia de los meses cortos`() {
        val r = recurrente(dia = 31, inicio = LocalDate(2026, 1, 31))

        assertEquals(LocalDate(2026, 2, 28), r.fechaEn(Mes(2026, 2)))
        assertEquals(LocalDate(2028, 2, 29), r.fechaEn(Mes(2028, 2)))
        assertEquals(LocalDate(2026, 4, 30), r.fechaEn(Mes(2026, 4)))
        assertEquals(listOf(Mes(2026, 1), Mes(2026, 2), Mes(2026, 3)), r.ocurrenciasPendientes(LocalDate(2026, 3, 31)).meses)
    }

    @Test
    fun `pausado no genera pero avanza, asi al reactivarlo no recupera los meses pausados`() {
        val generacion = recurrente(dia = 1, inicio = LocalDate(2026, 9, 1), activo = false)
            .ocurrenciasPendientes(hoy = LocalDate(2026, 11, 8))

        assertTrue(generacion.meses.isEmpty())
        assertEquals(Mes(2026, 11), generacion.ultimoMesRevisado)
    }

    @Test
    fun `los movimientos generados tienen id determinista`() {
        val r = recurrente(dia = 5, inicio = LocalDate(2026, 8, 5))
        val gasto = r.gastoEn(Mes(2026, 9))

        assertEquals("rec-r-2026-09", gasto.id)
        assertEquals(LocalDate(2026, 9, 5), gasto.fecha)
        assertEquals(700.0, gasto.monto)
    }
}
