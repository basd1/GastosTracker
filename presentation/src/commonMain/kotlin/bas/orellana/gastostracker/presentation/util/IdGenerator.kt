package bas.orellana.gastostracker.presentation.util

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Reemplaza java.util.UUID.randomUUID(), que no existe en Kotlin/Native.
 */
@OptIn(ExperimentalUuidApi::class)
fun randomId(): String = Uuid.random().toString()
