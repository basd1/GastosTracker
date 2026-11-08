package bas.orellana.gastostracker.presentation.di

import bas.orellana.gastostracker.presentation.viewmodel.BalanceViewModel
import bas.orellana.gastostracker.presentation.viewmodel.GraphViewModel
import bas.orellana.gastostracker.presentation.viewmodel.PresupuestosViewModel
import bas.orellana.gastostracker.presentation.viewmodel.HomeViewModel
import bas.orellana.gastostracker.presentation.viewmodel.SettingsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** ViewModels de la app. Común a Android e iOS. */
val presentationModule = module {
    viewModelOf(::HomeViewModel)
    viewModelOf(::BalanceViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::GraphViewModel)
    viewModelOf(::PresupuestosViewModel)
}
