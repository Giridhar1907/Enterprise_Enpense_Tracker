package com.example.enterpriseenpensetracker.data.remote

import com.example.enterpriseenpensetracker.utils.NotificationHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FCMService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        
        message.notification?.let {
            NotificationHelper.showNotification(
                context = this,
                title = it.title ?: "Expense Update",
                message = it.body ?: ""
            )
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Here you would normally send the token to your backend/Firestore
        // to target this specific device for notifications.
    }
}
