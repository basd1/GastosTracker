package bas.orellana.gastostracker.data.util

// El target JVM solo se usa para los tests en el host.

internal actual fun registrarInfo(tag: String, mensaje: String) {
    println("I/$tag: $mensaje")
}

internal actual fun registrarError(tag: String, mensaje: String, error: Throwable) {
    System.err.println("E/$tag: $mensaje")
    error.printStackTrace()
}
