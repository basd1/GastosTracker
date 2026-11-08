package bas.orellana.gastostracker.data.local

import bas.orellana.gastostracker.data.local.entity.CategoriaPersonalizadaEntity
import bas.orellana.gastostracker.data.local.entity.GastoEntity
import bas.orellana.gastostracker.data.local.entity.IngresoEntity
import bas.orellana.gastostracker.data.local.entity.PresupuestoEntity
import bas.orellana.gastostracker.data.local.entity.RecurrenteEntity
import bas.orellana.gastostracker.data.local.entity.MetaAhorroEntity
import bas.orellana.gastostracker.data.local.entity.AportacionEntity
import bas.orellana.gastostracker.data.local.entity.TotalPorCategoriaRow
import bas.orellana.gastostracker.domain.model.Categoria
import bas.orellana.gastostracker.domain.model.CategoriaPersonalizada
import bas.orellana.gastostracker.domain.model.GastoModel
import bas.orellana.gastostracker.domain.model.IngresoModel
import bas.orellana.gastostracker.domain.model.Presupuesto
import bas.orellana.gastostracker.domain.model.Mes
import bas.orellana.gastostracker.domain.model.Recurrente
import bas.orellana.gastostracker.domain.model.MetaAhorro
import bas.orellana.gastostracker.domain.model.Aportacion
import bas.orellana.gastostracker.domain.model.TipoMovimiento
import bas.orellana.gastostracker.domain.model.TotalPorCategoria

internal fun categoriaDesdeNombre(nombre: String?): Categoria? =
    nombre?.let { n -> Categoria.entries.find { it.name == n } }

internal fun GastoEntity.toModel() = GastoModel(
    id = id,
    nombre = nombre,
    monto = monto,
    fecha = fecha,
    categoria = categoriaDesdeNombre(categoria),
    categoriaPersonalizadaId = categoriaPersonalizadaId
)

internal fun GastoModel.toEntity() = GastoEntity(
    id = id,
    nombre = nombre,
    monto = monto,
    fecha = fecha,
    categoria = categoria?.name,
    categoriaPersonalizadaId = categoriaPersonalizadaId
)

internal fun IngresoEntity.toModel() = IngresoModel(
    id = id,
    nombre = nombre,
    monto = monto,
    fecha = fecha,
    categoria = categoriaDesdeNombre(categoria),
    categoriaPersonalizadaId = categoriaPersonalizadaId
)

internal fun IngresoModel.toEntity() = IngresoEntity(
    id = id,
    nombre = nombre,
    monto = monto,
    fecha = fecha,
    categoria = categoria?.name,
    categoriaPersonalizadaId = categoriaPersonalizadaId
)

internal fun CategoriaPersonalizadaEntity.toModel() = CategoriaPersonalizada(
    id = id,
    nombre = nombre,
    color = color
)

internal fun CategoriaPersonalizada.toEntity() = CategoriaPersonalizadaEntity(
    id = id,
    nombre = nombre,
    color = color
)

internal fun TotalPorCategoriaRow.toModel() = TotalPorCategoria(
    categoria = categoriaDesdeNombre(categoria),
    categoriaPersonalizadaId = categoriaPersonalizadaId,
    total = total
)

/** null si la fila no se puede convertir (p. ej. una categoría predefinida que ya no existe). */
internal fun PresupuestoEntity.toModel(): Presupuesto? {
    val categoriaModelo = categoriaDesdeNombre(categoria)
    if (categoria != null && categoriaModelo == null) return null
    return runCatching { Presupuesto(categoriaModelo, categoriaPersonalizadaId, monto) }.getOrNull()
}

internal fun Presupuesto.toEntity() = PresupuestoEntity(
    id = clave,
    categoria = categoria?.name,
    categoriaPersonalizadaId = categoriaPersonalizadaId,
    monto = monto
)

internal fun RecurrenteEntity.toModel(): Recurrente? {
    val tipoModelo = TipoMovimiento.entries.find { it.name == tipo } ?: return null
    return runCatching {
        Recurrente(
            id = id,
            tipo = tipoModelo,
            nombre = nombre,
            monto = monto,
            categoria = categoriaDesdeNombre(categoria),
            categoriaPersonalizadaId = categoriaPersonalizadaId,
            diaDelMes = diaDelMes,
            fechaInicio = fechaInicio,
            ultimoMesGenerado = ultimoMesGenerado?.let { Mes.parse(it) },
            activo = activo
        )
    }.getOrNull()
}

internal fun Recurrente.toEntity() = RecurrenteEntity(
    id = id,
    tipo = tipo.name,
    nombre = nombre,
    monto = monto,
    categoria = categoria?.name,
    categoriaPersonalizadaId = categoriaPersonalizadaId,
    diaDelMes = diaDelMes,
    fechaInicio = fechaInicio,
    ultimoMesGenerado = ultimoMesGenerado?.toString(),
    activo = activo
)

internal fun MetaAhorroEntity.toModel(): MetaAhorro? = runCatching {
    MetaAhorro(
        id = id,
        nombre = nombre,
        objetivo = objetivo,
        fechaObjetivo = fechaObjetivo,
        color = color,
        creada = creada,
        archivada = archivada
    )
}.getOrNull()

internal fun MetaAhorro.toEntity() = MetaAhorroEntity(
    id = id,
    nombre = nombre,
    objetivo = objetivo,
    fechaObjetivo = fechaObjetivo,
    color = color,
    creada = creada,
    archivada = archivada
)

internal fun AportacionEntity.toModel() = Aportacion(id = id, metaId = metaId, monto = monto, fecha = fecha, nota = nota)

internal fun Aportacion.toEntity() = AportacionEntity(id = id, metaId = metaId, monto = monto, fecha = fecha, nota = nota)
