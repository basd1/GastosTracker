package bas.orellana.gastostracker.data.util

/** Log mínimo multiplataforma (Logcat en Android, consola en iOS). */
internal expect fun registrarInfo(tag: String, mensaje: String)

internal expect fun registrarError(tag: String, mensaje: String, error: Throwable)
