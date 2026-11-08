package bas.orellana.gastostracker.di

import bas.orellana.gastostracker.data.legacy.MigradorDatosLegacy
import bas.orellana.gastostracker.data.local.AppDatabase
import bas.orellana.gastostracker.data.repository.CategoriasRepositoryImpl
import bas.orellana.gastostracker.data.repository.GastoRepositoryImpl
import bas.orellana.gastostracker.data.repository.IngresoRepositoryImpl
import bas.orellana.gastostracker.data.repository.PreferencesRepository
import bas.orellana.gastostracker.domain.repository.AjustesRepository
import bas.orellana.gastostracker.data.repository.PresupuestoRepositoryImpl
import bas.orellana.gastostracker.data.repository.RecurrenteRepositoryImpl
import bas.orellana.gastostracker.data.repository.AhorroRepositoryImpl
import bas.orellana.gastostracker.data.repository.CopiaSeguridadRepositoryImpl
import bas.orellana.gastostracker.data.repository.CuentaRepositoryImpl
import bas.orellana.gastostracker.domain.repository.CuentaRepository
import bas.orellana.gastostracker.domain.usecase.EliminarCuentaUseCase
import bas.orellana.gastostracker.domain.usecase.GetSaldosCuentasUseCase
import bas.orellana.gastostracker.domain.usecase.GuardarCuentaUseCase
import bas.orellana.gastostracker.domain.repository.CopiaSeguridadRepository
import bas.orellana.gastostracker.domain.usecase.ExportarCopiaSeguridadUseCase
import bas.orellana.gastostracker.domain.usecase.ImportarCopiaSeguridadUseCase
import bas.orellana.gastostracker.domain.repository.AhorroRepository
import bas.orellana.gastostracker.domain.usecase.EliminarAportacionUseCase
import bas.orellana.gastostracker.domain.usecase.EliminarMetaUseCase
import bas.orellana.gastostracker.domain.usecase.GetAportacionesUseCase
import bas.orellana.gastostracker.domain.usecase.GetProgresoMetasUseCase
import bas.orellana.gastostracker.domain.usecase.GetTodasLasAportacionesUseCase
import bas.orellana.gastostracker.domain.usecase.GetTotalAhorradoUseCase
import bas.orellana.gastostracker.domain.usecase.GuardarAportacionUseCase
import bas.orellana.gastostracker.domain.usecase.GuardarMetaUseCase
import bas.orellana.gastostracker.domain.repository.RecurrenteRepository
import bas.orellana.gastostracker.domain.usecase.EliminarRecurrenteUseCase
import bas.orellana.gastostracker.domain.usecase.GenerarRecurrentesUseCase
import bas.orellana.gastostracker.domain.usecase.GetRecurrentesUseCase
import bas.orellana.gastostracker.domain.usecase.GuardarRecurrenteUseCase
import bas.orellana.gastostracker.domain.repository.PresupuestoRepository
import bas.orellana.gastostracker.domain.usecase.EliminarPresupuestoUseCase
import bas.orellana.gastostracker.domain.usecase.GetEstadoPresupuestosUseCase
import bas.orellana.gastostracker.domain.usecase.GetPresupuestosUseCase
import bas.orellana.gastostracker.domain.usecase.GuardarPresupuestoUseCase
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
    single<AjustesRepository> { get<PreferencesRepository>() }
    single<PresupuestoRepository> { PresupuestoRepositoryImpl(get<AppDatabase>().presupuestoDao(), get()) }
    single<RecurrenteRepository> { RecurrenteRepositoryImpl(get(), get()) }
    single<AhorroRepository> { AhorroRepositoryImpl(get(), get()) }
    single<CopiaSeguridadRepository> { CopiaSeguridadRepositoryImpl(get(), get()) }
    single<CuentaRepository> { CuentaRepositoryImpl(get(), get()) }

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

    factory { GetPresupuestosUseCase(get()) }
    factory { GuardarPresupuestoUseCase(get()) }
    factory { EliminarPresupuestoUseCase(get()) }
    factory { GetEstadoPresupuestosUseCase(get(), get()) }

    factory { GetRecurrentesUseCase(get()) }
    factory { GuardarRecurrenteUseCase(get()) }
    factory { EliminarRecurrenteUseCase(get()) }
    factory { GenerarRecurrentesUseCase(get()) }

    factory { GetProgresoMetasUseCase(get()) }
    factory { GetAportacionesUseCase(get()) }
    factory { GetTodasLasAportacionesUseCase(get()) }
    factory { GetTotalAhorradoUseCase(get()) }
    factory { GuardarMetaUseCase(get()) }
    factory { EliminarMetaUseCase(get()) }
    factory { GuardarAportacionUseCase(get()) }
    factory { EliminarAportacionUseCase(get()) }

    factory { ExportarCopiaSeguridadUseCase(get()) }
    factory { ImportarCopiaSeguridadUseCase(get()) }

    factory { GetSaldosCuentasUseCase(get()) }
    factory { GuardarCuentaUseCase(get()) }
    factory { EliminarCuentaUseCase(get()) }
}
