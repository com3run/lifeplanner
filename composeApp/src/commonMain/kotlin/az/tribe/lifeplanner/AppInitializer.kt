package az.tribe.noesis.app

import com.mmk.kmpnotifier.notification.NotifierManager
import com.mmk.kmpnotifier.notification.PayloadData
import com.mmk.kmpauth.google.GoogleAuthCredentials
import com.mmk.kmpauth.google.GoogleAuthProvider

object AppInitializer {
    fun onApplicationStart() {
        onApplicationStartPlatformSpecific()

        GoogleAuthProvider.create(credentials = GoogleAuthCredentials(serverId = "41057913718-kkemslm4bollp9a9f7g0mrh6bjipnk9j.apps.googleusercontent.com"))

        NotifierManager.addListener(object : NotifierManager.Listener {
            override fun onNewToken(token: String) {
                println("Push Notification onNewToken: $token")
            }

            override fun onPushNotification(title: String?, body: String?) {
                super.onPushNotification(title, body)
                println("Push Notification notification type message is received: Title: $title and Body: $body")
            }

            override fun onPayloadData(data: PayloadData) {
                super.onPayloadData(data)
                println("Push Notification payloadData: $data")
            }

            override fun onNotificationClicked(data: PayloadData) {
                super.onNotificationClicked(data)
                println("Notification clicked, Notification payloadData: $data")
            }
        })
    }
}