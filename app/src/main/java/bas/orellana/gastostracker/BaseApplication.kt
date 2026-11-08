package bas.orellana.gastostracker

import android.app.Application
import bas.orellana.gastostracker.di.iniciarKoin
import org.koin.android.ext.koin.androidContext

class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        iniciarKoin {
            androidContext(this@BaseApplication)
        }
    }
}
