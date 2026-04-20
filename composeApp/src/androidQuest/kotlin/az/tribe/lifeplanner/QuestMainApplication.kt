package az.tribe.lifeplanner

import co.touchlab.kermit.Logger

class QuestMainApplication : MainApplication() {
    override fun onCreate() {
        super.onCreate()
        Logger.i("QuestMainApplication") { "Meta Horizon OS detected" }
        // TODO: Initialize Meta Horizon Platform SDK
        //   1. Create your app at https://developer.oculus.com/manage/
        //   2. Get your App ID and add to
        com.oculus.platform.Core.initializeAsync(this, BuildConfig.HORIZON_APP_ID)
    }
}
