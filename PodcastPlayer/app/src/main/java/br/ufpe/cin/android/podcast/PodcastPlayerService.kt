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
import org.jetbrains.anko.doAsync


class PodcastPlayerService : Service() {

    private val NOTIFICATION_ID = 2
    private val mBinder = MusicBinder()

    private var mPlayer: MediaPlayer? = null
    private var currentEpisode = ""

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
        if (!currentEpisode.equals(title)) {
            saveAndPlay(title, podcastPath)
            currentEpisode = title
            updateNotification(title)
        } else {
            if (!mPlayer!!.isPlaying) {
                mPlayer?.start()
            } else {
                updatePosition(currentEpisode, mPlayer!!.currentPosition)
                mPlayer?.pause()
            }
        }
    }

    private fun saveAndPlay(title: String, podcastPath: String){
        if (!currentEpisode.equals("")) {
            updatePosition(currentEpisode, mPlayer!!.currentPosition)
            playFomPosition(title, podcastPath)
        } else {
            play(podcastPath, 0)
        }
    }

    private fun updatePosition(title: String, position: Int) {
        val db = ItemFeedDatabase.getDatabase(this)
        doAsync {
            db.itemFeedDao().updatePosition(title, position)
        }
    }

    private fun playFomPosition(title: String, podcastPath: String) {
        val db = ItemFeedDatabase.getDatabase(this)
        doAsync {
            var episode = db.itemFeedDao().search(title)
            play(podcastPath, episode.lastPosition)
        }
    }

    private fun play(podcastPath: String, position: Int) {
        val fis = FileInputStream(podcastPath)
        mPlayer?.reset()
        mPlayer?.setDataSource(fis.fd)
        mPlayer?.prepare()
        mPlayer?.seekTo(position)
        fis.close()
        mPlayer?.start()
    }

    private fun createChannel() {
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
