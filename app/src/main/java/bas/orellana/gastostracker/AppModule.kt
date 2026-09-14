package bas.orellana.gastostracker

import bas.orellana.gastostracker.presentation.viewmodel.BalanceViewModel
import bas.orellana.gastostracker.presentation.viewmodel.HomeViewModel
import bas.orellana.gastostracker.presentation.viewmodel.SettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { HomeViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { SettingsViewModel(get()) }
    viewModel { BalanceViewModel(get(), get(), get(), get(), get()) }
}
