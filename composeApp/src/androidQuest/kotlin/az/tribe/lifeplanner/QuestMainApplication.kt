package az.tribe.lifeplanner

import co.touchlab.kermit.Logger

class QuestMainApplication : MainApplication() {
    override fun onCreate() {
        super.onCreate()
        Logger.i("QuestMainApplication") { "Meta Horizon OS detected" }
        val appId = BuildKonfig.HORIZON_APP_ID
        if (appId.isNotBlank()) {
            com.meta.horizon.platform.ovr.Core.asyncInitialize(appId, this)
        }
    }
}
