package bas.orellana.gastostracker.data.local

import bas.orellana.gastostracker.data.local.entity.CategoriaPersonalizadaEntity
import bas.orellana.gastostracker.data.local.entity.GastoEntity
import bas.orellana.gastostracker.data.local.entity.IngresoEntity
import bas.orellana.gastostracker.data.local.entity.TotalPorCategoriaRow
import bas.orellana.gastostracker.domain.model.Categoria
import bas.orellana.gastostracker.domain.model.CategoriaPersonalizada
import bas.orellana.gastostracker.domain.model.GastoModel
import bas.orellana.gastostracker.domain.model.IngresoModel
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
