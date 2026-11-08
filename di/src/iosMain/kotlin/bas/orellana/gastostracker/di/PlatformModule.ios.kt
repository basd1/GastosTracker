package bas.orellana.gastostracker.di

import bas.orellana.gastostracker.data.crearPreferenciasDataStore
import bas.orellana.gastostracker.data.fuentesLegacy
import bas.orellana.gastostracker.data.local.crearAppDatabase
import bas.orellana.gastostracker.presentation.util.AlmacenTickets
import bas.orellana.gastostracker.presentation.util.AlmacenTicketsIos
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single { crearAppDatabase() }
    single { crearPreferenciasDataStore() }
    single { fuentesLegacy() }
    single<AlmacenTickets> { AlmacenTicketsIos() }
}
