package bas.orellana.gastostracker.di

import bas.orellana.gastostracker.presentation.di.presentationModule
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration

/** Base de datos, DataStore y ficheros antiguos: dependen de la plataforma (Context en Android, rutas en iOS). */
expect val platformModule: Module

/**
 * Punto de arranque de la inyección de dependencias, igual en Android e iOS.
 * Cada plataforma solo añade lo suyo en [configuracion] (en Android, `androidContext`).
 */
fun iniciarKoin(configuracion: KoinAppDeclaration = {}): KoinApplication = startKoin {
    configuracion()
    modules(sharedModule, platformModule, presentationModule)
}
