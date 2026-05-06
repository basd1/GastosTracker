package bas.orellana.gastostracker

import bas.orellana.gastostracker.data.repository.GastoRepositoryImpl
import bas.orellana.gastostracker.domain.repository.GastoRepository
import bas.orellana.gastostracker.domain.usecase.GetGastosUseCase
import bas.orellana.gastostracker.presentation.viewmodel.HomeViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single<GastoRepository> { GastoRepositoryImpl() }
    factory { GetGastosUseCase(get()) }
    viewModel { HomeViewModel(get()) }
}