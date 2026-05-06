package bas.orellana.apppair

import android.app.Application
import bas.orellana.apppair.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@BaseApplication)
            modules(appModule)
        }
    }
}