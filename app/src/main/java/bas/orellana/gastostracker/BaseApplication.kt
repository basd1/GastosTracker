package bas.orellana.gastostracker

import android.app.Application
import bas.orellana.gastostracker.di.platformModule
import bas.orellana.gastostracker.di.sharedModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@BaseApplication)
            modules(sharedModule, platformModule, appModule)
        }
    }
}
