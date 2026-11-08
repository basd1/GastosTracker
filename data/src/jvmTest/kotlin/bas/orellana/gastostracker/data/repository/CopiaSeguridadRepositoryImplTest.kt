package bas.orellana.gastostracker.data.repository

import bas.orellana.gastostracker.data.EntornoDatos
import bas.orellana.gastostracker.domain.model.Aportacion
import bas.orellana.gastostracker.domain.model.Categoria
import bas.orellana.gastostracker.domain.model.GastoModel
import bas.orellana.gastostracker.domain.model.IngresoModel
import bas.orellana.gastostracker.domain.model.MetaAhorro
import bas.orellana.gastostracker.domain.model.Presupuesto
import bas.orellana.gastostracker.domain.model.Recurrente
import bas.orellana.gastostracker.domain.model.TipoMovimiento
import bas.orellana.gastostracker.domain.repository.CopiaSeguridadInvalida
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CopiaSeguridadRepositoryImplTest {

    private val origen = EntornoDatos()
    private val destino = EntornoDatos()
    private val fecha = LocalDate(2026, 11, 8)

    @AfterTest
    fun cerrar() {
        origen.close()
        destino.close()
    }

    private suspend fun rellenar(entorno: EntornoDatos) {
        entorno.categorias.addCategoriaPersonalizada("Mascotas", 0xFFEF9A9A)
        val idMascotas = entorno.categorias.getCategoriasPersonalizadas().first().single().id
        entorno.gastos.addGasto(GastoModel("g1", "Súper", 40.5, fecha, Categoria.COMIDA))
        entorno.gastos.addGasto(GastoModel("g2", "Pienso", 12.0, fecha, categoriaPersonalizadaId = idMascotas))
        entorno.ingresos.addIngreso(IngresoModel("i1", "Nómina", 1800.0, fecha))
        entorno.presupuestos.guardarPresupuesto(Presupuesto(Categoria.COMIDA, null, 300.0))
        entorno.recurrentes.guardarRecurrente(
            Recurrente("r1", TipoMovimiento.GASTO, "Alquiler", 700.0, diaDelMes = 1, fechaInicio = fecha)
        )
        entorno.ahorro.guardarMeta(MetaAhorro("m1", "Japón", 3000.0, LocalDate(2027, 4, 30), 0xFF81D4FA, fecha))
        entorno.ahorro.guardarAportacion(Aportacion("a1", "m1", 250.0, fecha, "Paga extra"))
    }

    @Test
    fun `exportar e importar en otra instalacion deja exactamente los mismos datos`() = runTest {
        rellenar(origen)
        destino.gastos.addGasto(GastoModel("viejo", "Se borrará", 1.0, fecha))

        val json = origen.copias.exportar()
        val resumen = destino.copias.importar(json)

        assertEquals(2, resumen.gastos)
        assertEquals(1, resumen.aportaciones)
        assertEquals(origen.gastos.getGastos().first(), destino.gastos.getGastos().first())
        assertEquals(origen.ingresos.getIngresos().first(), destino.ingresos.getIngresos().first())
        assertEquals(origen.categorias.getCategoriasPersonalizadas().first(), destino.categorias.getCategoriasPersonalizadas().first())
        assertEquals(origen.presupuestos.getPresupuestos().first(), destino.presupuestos.getPresupuestos().first())
        assertEquals(origen.recurrentes.getRecurrentes().first(), destino.recurrentes.getRecurrentes().first())
        assertEquals(origen.ahorro.getProgresoMetas().first(), destino.ahorro.getProgresoMetas().first())
        assertEquals(origen.ahorro.getTodasLasAportaciones().first(), destino.ahorro.getTodasLasAportaciones().first())
    }

    @Test
    fun `una copia invalida no toca los datos actuales`() = runTest {
        rellenar(destino)
        val antes = destino.gastos.getGastos().first()

        assertFailsWith<CopiaSeguridadInvalida> { destino.copias.importar("esto no es json") }
        assertFailsWith<CopiaSeguridadInvalida> { destino.copias.importar("""{"formato":"otra-app"}""") }
        assertFailsWith<CopiaSeguridadInvalida> {
            destino.copias.importar(
                """{"formato":"gastostracker-copia","aportaciones":[{"id":"a","metaId":"no-existe","monto":1,"fecha":"2026-11-01"}]}"""
            )
        }

        assertEquals(antes, destino.gastos.getGastos().first())
    }

    @Test
    fun `una copia de una version nueva de la app se rechaza`() = runTest {
        assertFailsWith<CopiaSeguridadInvalida> {
            destino.copias.importar("""{"formato":"gastostracker-copia","version":99}""")
        }
    }

    @Test
    fun `una copia vacia deja la app vacia y los campos que faltan se leen como null`() = runTest {
        rellenar(destino)

        destino.copias.importar(
            """{"formato":"gastostracker-copia","gastos":[{"id":"g","nombre":"Pan","monto":1.2,"fecha":"2026-11-01"}]}"""
        )

        val gasto = destino.gastos.getGastos().first().single()
        assertEquals(null, gasto.categoria)
        assertTrue(destino.ahorro.getProgresoMetas().first().isEmpty())
        assertTrue(destino.recurrentes.getRecurrentes().first().isEmpty())
    }
}
