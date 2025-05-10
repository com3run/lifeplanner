package az.tribe.lifeplanner

import android.app.Application
import android.content.Context
import az.tribe.lifeplanner.di.initKoin
import org.koin.android.ext.koin.androidContext

class MainApplication : Application() {
    companion object {
        lateinit var appContext: Context
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext

        initKoin {
            androidContext(this@MainApplication)
        }
    }
}