package bas.orellana.gastostracker

import bas.orellana.gastostracker.data.repository.CategoriasRepositoryImpl
import bas.orellana.gastostracker.data.repository.GastoRepositoryImpl
import bas.orellana.gastostracker.data.repository.PreferencesRepository
import bas.orellana.gastostracker.data.repository.dataStore
import bas.orellana.gastostracker.domain.repository.CategoriasRepository
import bas.orellana.gastostracker.domain.repository.GastoRepository
import bas.orellana.gastostracker.domain.usecase.AddCategoriaPersonalizadaUseCase
import bas.orellana.gastostracker.domain.usecase.AddGastoUseCase
import bas.orellana.gastostracker.domain.usecase.DeleteCategoriaPersonalizadaUseCase
import bas.orellana.gastostracker.domain.usecase.DeleteGastoUseCase
import bas.orellana.gastostracker.domain.usecase.GetCategoriasPersonalizadasUseCase
import bas.orellana.gastostracker.domain.usecase.GetGastosUseCase
import bas.orellana.gastostracker.domain.usecase.UpdateGastoUseCase
import bas.orellana.gastostracker.presentation.viewmodel.HomeViewModel
import bas.orellana.gastostracker.presentation.viewmodel.SettingsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single<CategoriasRepository> { CategoriasRepositoryImpl(androidContext()) }
    single<GastoRepository> { GastoRepositoryImpl(androidContext()) }
    single { PreferencesRepository(androidContext()) }

    factory { GetGastosUseCase(get()) }
    factory { AddGastoUseCase(get()) }
    factory { DeleteGastoUseCase(get()) }
    factory { UpdateGastoUseCase(get()) }
    factory { GetCategoriasPersonalizadasUseCase(get()) }
    factory { AddCategoriaPersonalizadaUseCase(get()) }
    factory { DeleteCategoriaPersonalizadaUseCase(get()) }

    viewModel { HomeViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { SettingsViewModel(get()) }
}