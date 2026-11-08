package bas.orellana.gastostracker.domain.repository

/** Copia de seguridad completa de los datos de la app (no incluye los ajustes). */
interface CopiaSeguridadRepository {

    /** Todos los datos en el formato de copia de seguridad (JSON). */
    suspend fun exportar(): String

    /**
     * Sustituye TODOS los datos por los de la copia. Primero se lee y se valida la copia entera;
     * si algo falla se lanza [CopiaSeguridadInvalida] sin haber tocado nada. La sustitución va en
     * una sola transacción.
     */
    suspend fun importar(contenido: String): ResumenCopia
}

/** Cuántos elementos tiene una copia. */
data class ResumenCopia(
    val gastos: Int,
    val ingresos: Int,
    val categorias: Int,
    val presupuestos: Int,
    val recurrentes: Int,
    val metas: Int,
    val aportaciones: Int,
    val cuentas: Int = 0
) {
    val movimientos: Int get() = gastos + ingresos + aportaciones
}

class CopiaSeguridadInvalida(mensaje: String, causa: Throwable? = null) : Exception(mensaje, causa)
