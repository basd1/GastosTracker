# Reglas de R8 propias de la app.
#
# Room, Koin, kotlinx.serialization, DataStore, SQLite bundled y Compose traen sus propias
# reglas (consumer rules), así que de momento no hace falta ninguna más. Si una versión de
# release falla al arrancar con ClassNotFoundException/NoSuchMethodException, añade aquí el
# -keep de la clase afectada.

# Conserva los números de línea para que los stack traces de release se puedan leer
# (con el mapping.txt que genera el build en app/build/outputs/mapping/release/).
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
