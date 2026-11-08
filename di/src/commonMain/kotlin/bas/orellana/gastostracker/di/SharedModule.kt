package bas.orellana.gastostracker.di

import bas.orellana.gastostracker.data.legacy.MigradorDatosLegacy
import bas.orellana.gastostracker.data.local.AppDatabase
import bas.orellana.gastostracker.data.repository.CategoriasRepositoryImpl
import bas.orellana.gastostracker.data.repository.GastoRepositoryImpl
import bas.orellana.gastostracker.data.repository.IngresoRepositoryImpl
import bas.orellana.gastostracker.data.repository.PreferencesRepository
import bas.orellana.gastostracker.domain.repository.CategoriasRepository
import bas.orellana.gastostracker.domain.repository.GastoRepository
import bas.orellana.gastostracker.domain.repository.IngresoRepository
import bas.orellana.gastostracker.domain.usecase.AddCategoriaPersonalizadaUseCase
import bas.orellana.gastostracker.domain.usecase.AddGastoUseCase
import bas.orellana.gastostracker.domain.usecase.AddIngresoUseCase
import bas.orellana.gastostracker.domain.usecase.ContarMovimientosCategoriaUseCase
import bas.orellana.gastostracker.domain.usecase.DeleteAllGastosUseCase
import bas.orellana.gastostracker.domain.usecase.DeleteCategoriaPersonalizadaUseCase
import bas.orellana.gastostracker.domain.usecase.DeleteGastoUseCase
import bas.orellana.gastostracker.domain.usecase.DeleteIngresoUseCase
import bas.orellana.gastostracker.domain.usecase.GetCategoriasPersonalizadasUseCase
import bas.orellana.gastostracker.domain.usecase.GetGastosUseCase
import bas.orellana.gastostracker.domain.usecase.GetGastosEntreUseCase
import bas.orellana.gastostracker.domain.usecase.GetIngresosUseCase
import bas.orellana.gastostracker.domain.usecase.GetIngresosEntreUseCase
import bas.orellana.gastostracker.domain.usecase.GetTotalIngresosUseCase
import bas.orellana.gastostracker.domain.usecase.GetTotalesGastosPorCategoriaUseCase
import bas.orellana.gastostracker.domain.usecase.UpdateCategoriaPersonalizadaUseCase
import bas.orellana.gastostracker.domain.usecase.UpdateGastoUseCase
import bas.orellana.gastostracker.domain.usecase.UpdateIngresoUseCase
import org.koin.dsl.module

/**
 * Bindings de dominio/datos que no dependen de ninguna plataforma concreta.
 * La base de datos, el DataStore de preferencias y los ficheros antiguos a migrar se
 * registran por plataforma (ver [platformModule]).
 */
val sharedModule = module {
    single { MigradorDatosLegacy(get(), get(), get()) }
    single { get<AppDatabase>().gastoDao() }
    single { get<AppDatabase>().ingresoDao() }

    single<CategoriasRepository> { CategoriasRepositoryImpl(get(), get()) }
    single<GastoRepository> { GastoRepositoryImpl(get(), get()) }
    single<IngresoRepository> { IngresoRepositoryImpl(get(), get()) }
    single { PreferencesRepository(get()) }

    factory { GetGastosUseCase(get()) }
    factory { GetGastosEntreUseCase(get()) }
    factory { AddGastoUseCase(get()) }
    factory { DeleteGastoUseCase(get()) }
    factory { DeleteAllGastosUseCase(get()) }
    factory { UpdateGastoUseCase(get()) }
    factory { GetTotalesGastosPorCategoriaUseCase(get()) }
    factory { GetCategoriasPersonalizadasUseCase(get()) }
    factory { AddCategoriaPersonalizadaUseCase(get()) }
    factory { UpdateCategoriaPersonalizadaUseCase(get()) }
    factory { DeleteCategoriaPersonalizadaUseCase(get()) }
    factory { ContarMovimientosCategoriaUseCase(get()) }

    factory { GetIngresosUseCase(get()) }
    factory { GetIngresosEntreUseCase(get()) }
    factory { AddIngresoUseCase(get()) }
    factory { UpdateIngresoUseCase(get()) }
    factory { DeleteIngresoUseCase(get()) }
    factory { GetTotalIngresosUseCase(get()) }
}
