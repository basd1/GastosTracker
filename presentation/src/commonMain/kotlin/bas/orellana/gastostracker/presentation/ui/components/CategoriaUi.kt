package bas.orellana.gastostracker.presentation.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import bas.orellana.gastostracker.domain.model.Categoria
import bas.orellana.gastostracker.domain.model.CategoriaIngreso
import bas.orellana.gastostracker.domain.model.CategoriaPersonalizada
import bas.orellana.gastostracker.presentation.resources.Res
import bas.orellana.gastostracker.presentation.resources.categoria_comida
import bas.orellana.gastostracker.presentation.resources.categoria_ingreso_freelance
import bas.orellana.gastostracker.presentation.resources.categoria_ingreso_intereses
import bas.orellana.gastostracker.presentation.resources.categoria_ingreso_nomina
import bas.orellana.gastostracker.presentation.resources.categoria_ingreso_otros
import bas.orellana.gastostracker.presentation.resources.categoria_ingreso_reembolsos
import bas.orellana.gastostracker.presentation.resources.categoria_ingreso_regalos
import bas.orellana.gastostracker.presentation.resources.categoria_ingreso_ventas
import bas.orellana.gastostracker.presentation.resources.categoria_hogar
import bas.orellana.gastostracker.presentation.resources.categoria_ocio
import bas.orellana.gastostracker.presentation.resources.categoria_otros
import bas.orellana.gastostracker.presentation.resources.categoria_regalos
import bas.orellana.gastostracker.presentation.resources.categoria_ropa
import bas.orellana.gastostracker.presentation.resources.categoria_salud
import bas.orellana.gastostracker.presentation.resources.categoria_servicios
import bas.orellana.gastostracker.presentation.resources.categoria_tecnologia
import bas.orellana.gastostracker.presentation.resources.categoria_transporte
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

val Categoria.nombreRes: StringResource
    get() = when (this) {
        Categoria.COMIDA -> Res.string.categoria_comida
        Categoria.HOGAR -> Res.string.categoria_hogar
        Categoria.TRANSPORTE -> Res.string.categoria_transporte
        Categoria.OCIO -> Res.string.categoria_ocio
        Categoria.ROPA -> Res.string.categoria_ropa
        Categoria.SALUD -> Res.string.categoria_salud
        Categoria.TECNOLOGIA -> Res.string.categoria_tecnologia
        Categoria.SERVICIOS -> Res.string.categoria_servicios
        Categoria.REGALOS -> Res.string.categoria_regalos
        Categoria.OTROS -> Res.string.categoria_otros
    }

/** Nombre visible de la categoría, en el idioma del dispositivo. */
@Composable
fun Categoria.nombre(): String = stringResource(nombreRes)

/** Nombre y color con los que se pinta la categoría de un movimiento; null si no tiene. */
data class EtiquetaCategoria(val nombre: String, val color: Color)

@Composable
fun etiquetaCategoria(
    categoria: Categoria?,
    categoriaPersonalizadaId: String?,
    categoriasPersonalizadas: List<CategoriaPersonalizada>
): EtiquetaCategoria? {
    if (categoria != null) return EtiquetaCategoria(categoria.nombre(), Color(categoria.color))
    val personalizada = categoriaPersonalizadaId?.let { id -> categoriasPersonalizadas.find { it.id == id } }
    return personalizada?.let { EtiquetaCategoria(it.nombre, Color(it.color)) }
}

val CategoriaIngreso.nombreRes: StringResource
    get() = when (this) {
        CategoriaIngreso.NOMINA -> Res.string.categoria_ingreso_nomina
        CategoriaIngreso.FREELANCE -> Res.string.categoria_ingreso_freelance
        CategoriaIngreso.VENTAS -> Res.string.categoria_ingreso_ventas
        CategoriaIngreso.REEMBOLSOS -> Res.string.categoria_ingreso_reembolsos
        CategoriaIngreso.INTERESES -> Res.string.categoria_ingreso_intereses
        CategoriaIngreso.REGALOS -> Res.string.categoria_ingreso_regalos
        CategoriaIngreso.OTROS -> Res.string.categoria_ingreso_otros
    }

/** Nombre visible de la categoría de ingreso, en el idioma del dispositivo. */
@Composable
fun CategoriaIngreso.nombre(): String = stringResource(nombreRes)

@Composable
fun etiquetaCategoria(
    categoria: CategoriaIngreso?,
    categoriaPersonalizadaId: String?,
    categoriasPersonalizadas: List<CategoriaPersonalizada>
): EtiquetaCategoria? {
    if (categoria != null) return EtiquetaCategoria(categoria.nombre(), Color(categoria.color))
    val personalizada = categoriaPersonalizadaId?.let { id -> categoriasPersonalizadas.find { it.id == id } }
    return personalizada?.let { EtiquetaCategoria(it.nombre, Color(it.color)) }
}
