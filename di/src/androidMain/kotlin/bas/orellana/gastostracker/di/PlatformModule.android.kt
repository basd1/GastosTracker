package bas.orellana.gastostracker.di

import bas.orellana.gastostracker.data.crearPreferenciasDataStore
import bas.orellana.gastostracker.data.fuentesLegacy
import bas.orellana.gastostracker.data.local.crearAppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val platformModule = module {
    single { crearAppDatabase(androidContext()) }
    single { crearPreferenciasDataStore(androidContext()) }
    single { fuentesLegacy(androidContext()) }
}
