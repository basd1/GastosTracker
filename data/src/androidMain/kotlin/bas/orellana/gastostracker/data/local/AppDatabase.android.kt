package bas.orellana.gastostracker.data.local

import android.content.Context
import androidx.room.Room

fun crearAppDatabase(context: Context): AppDatabase {
    val appContext = context.applicationContext
    val ruta = appContext.getDatabasePath(NOMBRE_BASE_DATOS).absolutePath
    return construirAppDatabase(Room.databaseBuilder<AppDatabase>(context = appContext, name = ruta))
}
