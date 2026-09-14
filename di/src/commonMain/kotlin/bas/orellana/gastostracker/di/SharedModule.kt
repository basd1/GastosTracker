package bas.orellana.gastostracker.di

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
import bas.orellana.gastostracker.domain.usecase.DeleteAllGastosUseCase
import bas.orellana.gastostracker.domain.usecase.DeleteCategoriaPersonalizadaUseCase
import bas.orellana.gastostracker.domain.usecase.DeleteGastoUseCase
import bas.orellana.gastostracker.domain.usecase.DeleteIngresoUseCase
import bas.orellana.gastostracker.domain.usecase.GetCategoriasPersonalizadasUseCase
import bas.orellana.gastostracker.domain.usecase.GetGastosUseCase
import bas.orellana.gastostracker.domain.usecase.GetIngresosUseCase
import bas.orellana.gastostracker.domain.usecase.UpdateCategoriaPersonalizadaUseCase
import bas.orellana.gastostracker.domain.usecase.UpdateGastoUseCase
import bas.orellana.gastostracker.domain.usecase.UpdateIngresoUseCase
import org.koin.dsl.module

/**
 * Bindings de dominio/datos que no dependen de ninguna plataforma concreta.
 * El DataStore que consumen los repos se registra por plataforma (ver [platformModule]).
 */
val sharedModule = module {
    single<CategoriasRepository> { CategoriasRepositoryImpl(get()) }
    single<GastoRepository> { GastoRepositoryImpl(get()) }
    single<IngresoRepository> { IngresoRepositoryImpl(get()) }
    single { PreferencesRepository(get()) }

    factory { GetGastosUseCase(get()) }
    factory { AddGastoUseCase(get()) }
    factory { DeleteGastoUseCase(get()) }
    factory { DeleteAllGastosUseCase(get()) }
    factory { UpdateGastoUseCase(get()) }
    factory { GetCategoriasPersonalizadasUseCase(get()) }
    factory { AddCategoriaPersonalizadaUseCase(get()) }
    factory { UpdateCategoriaPersonalizadaUseCase(get()) }
    factory { DeleteCategoriaPersonalizadaUseCase(get()) }

    factory { GetIngresosUseCase(get()) }
    factory { AddIngresoUseCase(get()) }
    factory { UpdateIngresoUseCase(get()) }
    factory { DeleteIngresoUseCase(get()) }
}
