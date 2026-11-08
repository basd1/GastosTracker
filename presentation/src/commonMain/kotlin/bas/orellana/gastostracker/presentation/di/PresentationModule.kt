package bas.orellana.gastostracker.presentation.di

import bas.orellana.gastostracker.presentation.viewmodel.BalanceViewModel
import bas.orellana.gastostracker.presentation.viewmodel.GraphViewModel
import bas.orellana.gastostracker.presentation.viewmodel.PresupuestosViewModel
import bas.orellana.gastostracker.presentation.viewmodel.RecurrentesViewModel
import bas.orellana.gastostracker.presentation.viewmodel.MetasViewModel
import bas.orellana.gastostracker.presentation.viewmodel.DatosViewModel
import bas.orellana.gastostracker.presentation.viewmodel.CuentasViewModel
import bas.orellana.gastostracker.presentation.viewmodel.CategoriasViewModel
import bas.orellana.gastostracker.presentation.viewmodel.MetaDetalleViewModel
import org.koin.core.module.dsl.viewModel
import bas.orellana.gastostracker.presentation.TareasAlAbrir
import bas.orellana.gastostracker.presentation.navigation.PeticionesFiltro
import bas.orellana.gastostracker.presentation.navigation.AccionesPendientes
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
    viewModelOf(::RecurrentesViewModel)
    viewModelOf(::MetasViewModel)
    viewModelOf(::DatosViewModel)
    viewModelOf(::CuentasViewModel)
    viewModelOf(::CategoriasViewModel)
    viewModel { (metaId: String) -> MetaDetalleViewModel(metaId, get(), get(), get(), get(), get(), get()) }
    single { TareasAlAbrir(get(), get(), get()) }
    single { PeticionesFiltro() }
    single { AccionesPendientes() }
}
