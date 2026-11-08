package bas.orellana.gastostracker.data.util

internal actual fun registrarInfo(tag: String, mensaje: String) {
    println("I/$tag: $mensaje")
}

internal actual fun registrarError(tag: String, mensaje: String, error: Throwable) {
    println("E/$tag: $mensaje")
    println(error.stackTraceToString())
}
