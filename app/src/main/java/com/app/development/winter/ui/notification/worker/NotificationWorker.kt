package com.app.development.winter.ui.notification.worker

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkerParameters
import com.app.development.winter.BuildConfig
import com.app.development.winter.R
import com.app.development.winter.application.Controller
import com.app.development.winter.localcache.LocalDataHelper
import com.app.development.winter.shared.extension.hasPostNotificationPermissionGranted
import com.app.development.winter.shared.extension.toJson
import com.app.development.winter.shared.network.ApiEndpoints.RESPONSE_OK
import com.app.development.winter.ui.notification.NotificationsActivity
import com.app.development.winter.ui.notification.domain.NotificationRepository
import com.app.development.winter.ui.notification.model.Notification
import com.google.gson.JsonArray
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class NotificationWorker(
    private val context: Context,
    private val params: WorkerParameters,
) : CoroutineWorker(context, params) {

    private var notificationGroupItems: MutableList<NotificationCompat.Builder> = mutableListOf()
    private val notificationStyle = NotificationCompat.InboxStyle()
    private var notificationIdCount = 0

    override suspend fun doWork(): Result = coroutineScope {
        val lastSeenNotificationId = LocalDataHelper.getLastSeenNotificationID() ?: ""
        val result = async {
            return@async getNotificationResult(lastSeenNotificationId)
        }
        val updatedNotificationId = result.await()
        if (updatedNotificationId.isNullOrEmpty().not()) {
            val success: Data =
                Data.Builder().putString(KEY_LAST_NOTIFICATION_Id, updatedNotificationId).build()
            Result.success(success)
        } else {
            Result.failure()
        }
    }

    private suspend fun getNotificationResult(lastSeenNotificationId: String?): String? =
        suspendCoroutine { continuation ->
            getLatestNotification(lastSeenNotificationId, onSuccess = {
                if (it?.isNotEmpty() == true) {
                    processAndPostNotification(it) {
                        continuation.resume(it.first().id)
                    }
                } else {
                    LocalDataHelper.setLastSeenNotificationID(lastSeenNotificationId ?: "")
                    continuation.resume(lastSeenNotificationId)
                }
            }, onFailure = {
                continuation.resume(null)
            })
        }


    companion object {
        private const val TAG = "NetworkWorker"
        const val KEY_LAST_NOTIFICATION_Id = "key_last_notification_id"

        fun buildRequest(lastNotificationId: String): PeriodicWorkRequest {
            val inputData =
                Data.Builder().putString(KEY_LAST_NOTIFICATION_Id, lastNotificationId).build()

            val constraints =
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

            return PeriodicWorkRequest.Builder(NotificationWorker::class.java, 15, TimeUnit.MINUTES)
                .setInputData(inputData).setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.MINUTES).build()
        }
    }

    private fun processAndPostNotification(
        pastNotificationList: List<Notification>, callback: () -> Unit
    ) {
        if (Controller.instance.applicationContext.hasPostNotificationPermissionGranted()) {
            val notificationGroupList = pastNotificationList.take(5)
            val jsonArrayIDs = JsonArray()
            notificationGroupList.onEach { notification ->
                notificationStyle.addLine(notification.title)
                jsonArrayIDs.add(notification.id)
                val notificationGroupItem = NotificationCompat.Builder(
                    Controller.instance.applicationContext, pending_notification_channel
                ).setSmallIcon(R.drawable.ic_push_notification)
                    .setContentTitle(notification.getMessage()).setContentText(notification.title)
                    .setAutoCancel(true).setGroup(pending_notification_group)
                notificationGroupItems.add(notificationGroupItem)
            }

            notificationStyle.setBigContentTitle("You have new notifications")
            notificationStyle.setSummaryText("${notificationGroupItems.size}+ new messages")

            if (notificationGroupItems.isEmpty().not()) {
                LocalDataHelper.setLastSeenNotificationID(jsonArrayIDs.toJson())
                publishGroupNotification()
            }
        }
        callback.invoke()
    }

    @SuppressLint("MissingPermission")
    private fun publishGroupNotification() {
        notificationIdCount = 0
        createNotificationChannel()
        val summaryNotification = NotificationCompat.Builder(
            Controller.instance.applicationContext, pending_notification_channel
        ).setContentTitle("You have new notifications")
            .setContentText(if (notificationGroupItems.size > 1) "${notificationGroupItems.size} + new messages" else "${notificationGroupItems.size} new message")
            .setSmallIcon(R.drawable.ic_push_notification).setStyle(notificationStyle)
            .setGroup(pending_notification_group).setGroupSummary(true).setAutoCancel(true)
            .setContentIntent(setUpPendingIntent()).build()
        if (Controller.instance.applicationContext.hasPostNotificationPermissionGranted()) {
            NotificationManagerCompat.from(Controller.instance.applicationContext).apply {
                notificationGroupItems.onEach {
                    notify(notificationIdCount.plus(1), it.build())
                }
                notify(notificationIdCount.plus(1), summaryNotification)
            }
        }
    }

    private fun getLatestNotification(
        lastSeenNotificationId: String?,
        onSuccess: (list: List<Notification>?) -> Unit,
        onFailure: (message: String) -> Unit
    ) {
        val params: MutableMap<String, Any> = HashMap()
        params["ids"] = lastSeenNotificationId ?: ""
        params["packageName"] = BuildConfig.APPLICATION_ID
        params["userId"] = LocalDataHelper.getUserDetail()?.id.toString()
        CoroutineScope(Dispatchers.IO).launch {
            NotificationRepository.getLatestNotifications(params).catch { exception ->
                exception.message?.let { message ->
                    onFailure.invoke(message)
                }
            }.collect { result ->
                if (result != null && result.code == RESPONSE_OK) {
                    onSuccess.invoke(result.data)
                } else {
                    onFailure.invoke(result?.message ?: "")
                }
            }
        }
    }


    private var pending_notification_channel = "general_notification_channel"
    private var pending_notification_group = "${BuildConfig.APPLICATION_ID}.NOTIFICATION_GROUP"

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = Controller.instance.applicationContext.getString(R.string.app_name)
            val description = "${context.getString(R.string.app_name)} general notification channel"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(pending_notification_channel, name, importance)
            channel.description = description
            // Register the channel with the system; you can't change the importance or other notification behaviors after this
            val notificationManager =
                Controller.instance.applicationContext.getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun setUpPendingIntent(): PendingIntent? {
        // Create an Intent for the activity you want to start.
        val resultIntent = Intent(context, NotificationsActivity::class.java)
        val rootIntent =
            applicationContext.packageManager.getLaunchIntentForPackage(applicationContext.packageName)
        return PendingIntent.getActivities(
            applicationContext,
            0,
            arrayOf(rootIntent, resultIntent),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
}