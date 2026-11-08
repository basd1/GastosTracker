package bas.orellana.gastostracker.data.util

import android.util.Log

internal actual fun registrarInfo(tag: String, mensaje: String) {
    Log.i(tag, mensaje)
}

internal actual fun registrarError(tag: String, mensaje: String, error: Throwable) {
    Log.e(tag, mensaje, error)
}
