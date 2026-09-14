package bas.orellana.gastostracker.di

import bas.orellana.gastostracker.data.createDataStore
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val platformModule = module {
    single { createDataStore(androidContext()) }
}
