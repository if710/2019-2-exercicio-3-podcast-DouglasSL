package br.ufpe.cin.android.podcast

import android.app.*
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import java.io.FileInputStream
import android.content.Context.NOTIFICATION_SERVICE
import androidx.core.content.ContextCompat.getSystemService
import android.app.NotificationManager
import android.content.Context
import org.jetbrains.anko.ctx


class PodcastPlayerService : Service() {

    private var mPlayer: MediaPlayer? = null
    private val mBinder = MusicBinder()
    private val NOTIFICATION_ID = 2

    override fun onCreate() {
        super.onCreate()

        mPlayer = MediaPlayer()
        mPlayer?.isLooping = true

        createChannel()
        startForeground(NOTIFICATION_ID, getNotification(""))
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        mPlayer?.release()
        super.onDestroy()
    }

    fun playPodcast(podcastPath: String, title: String) {
        if (!mPlayer!!.isPlaying) {
            updateNotification(title)
            val fis = FileInputStream(podcastPath)
            mPlayer?.reset()
            mPlayer?.setDataSource(fis.fd)
            mPlayer?.prepare()
            fis.close()
            mPlayer?.start()
        } else {
            mPlayer?.pause()
        }
    }

    fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "1",
                "Podcast Player Notification",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun updateNotification(text: String) {
        val notification = getNotification(text)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun getNotification(text: String) : Notification {
        val notificationIntent = Intent(applicationContext, PodcastPlayerService::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        return NotificationCompat.Builder(applicationContext, "1")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .setContentTitle("Podcast Player")
            .setContentText(text)
            .setContentIntent(pendingIntent).build()
    }

    inner class MusicBinder : Binder() {
        internal val playerService: PodcastPlayerService
            get() = this@PodcastPlayerService
    }

    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }
}
