package bas.orellana.apppair

import bas.orellana.apppair.data.repository.GastoRepositoryImpl
import bas.orellana.apppair.domain.repository.GastoRepository
import bas.orellana.apppair.domain.usecase.GetGastosUseCase
import bas.orellana.apppair.presentation.viewmodel.HomeViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single<GastoRepository> { GastoRepositoryImpl() }
    factory { GetGastosUseCase(get()) }
    viewModel { HomeViewModel(get()) }
}