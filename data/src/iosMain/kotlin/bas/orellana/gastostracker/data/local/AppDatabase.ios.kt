package bas.orellana.gastostracker.data.local

import androidx.room.Room
import bas.orellana.gastostracker.data.directorioDocumentos

fun crearAppDatabase(): AppDatabase =
    construirAppDatabase(Room.databaseBuilder<AppDatabase>(name = "${directorioDocumentos()}/$NOMBRE_BASE_DATOS"))
