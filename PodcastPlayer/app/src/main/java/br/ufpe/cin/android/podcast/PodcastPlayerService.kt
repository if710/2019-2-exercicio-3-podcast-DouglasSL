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
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import org.jetbrains.anko.ctx
import org.jetbrains.anko.doAsync
import java.io.File


class PodcastPlayerService : Service() {

    private val PLAY_PAUSE_ACTION = "br.ufpe.cin.android.podcast.services.action.PLAY_PAUSE_ACTION"
    private val NOTIFICATION_ID = 2
    private val mBinder = MusicBinder()

    private var mPlayer: MediaPlayer? = null
    private var currentEpisode: ItemFeed? = null
    private var currentHolder: ItemFeedAdapter.ViewHolder? = null

    override fun onCreate() {
        super.onCreate()

        mPlayer = MediaPlayer()
        mPlayer?.setOnCompletionListener {
            var intent = Intent(ACTION_DELETE_FILE).apply {
                putExtra(DownloadService.PODCAST_ID, currentEpisode?.title)
            }
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
            currentEpisode = null
            currentHolder = null
        }

        createChannel()
        startForeground(NOTIFICATION_ID, getNotification(""))
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        var intentFilter = IntentFilter(PLAY_PAUSE_ACTION)
        registerReceiver(broadcastReceiver, intentFilter)
        return START_STICKY
    }

    override fun onDestroy() {
        mPlayer?.release()
        super.onDestroy()
    }

    fun playPodcast(podcast: ItemFeed, holder: ItemFeedAdapter.ViewHolder ) {
        if (currentHolder == null) {
            currentHolder = holder
        }

        if (currentEpisode != podcast) {
            currentHolder!!.player.setImageResource(android.R.drawable.ic_media_play)
            saveAndPlay(podcast.title, podcast.path)
            currentHolder = holder
            currentEpisode = podcast
        } else {
            if (!mPlayer!!.isPlaying) {
                mPlayer?.start()
                currentHolder!!.player.setImageResource(android.R.drawable.ic_media_pause)
            } else {
                updatePosition(currentEpisode!!.title, mPlayer!!.currentPosition)
                mPlayer?.pause()
                currentHolder!!.player.setImageResource(android.R.drawable.ic_media_play)
            }
        }

        updateNotification(currentEpisode!!.title)
    }

    private fun saveAndPlay(title: String, podcastPath: String){
        if (currentEpisode != null) {
            updatePosition(currentEpisode!!.title, mPlayer!!.currentPosition)
            playFomPosition(title, podcastPath)
        } else {
            play(podcastPath, 0)
        }
    }

    private fun updatePosition(title: String, position: Int) {
        var db = ItemFeedDatabase.getDatabase(this)
        doAsync {
            db.itemFeedDao().updatePosition(title, position)
        }
    }

    private fun playFomPosition(title: String, podcastPath: String) {
        var db = ItemFeedDatabase.getDatabase(this)
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
        currentHolder!!.player.setImageResource(android.R.drawable.ic_media_pause)
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

        val playIntent = Intent(PLAY_PAUSE_ACTION)
        val playPendingIntent = PendingIntent.getBroadcast(applicationContext, 0, playIntent, 0)

        var actionName = if(!mPlayer!!.isPlaying) "Play" else "Pause"

        return NotificationCompat.Builder(applicationContext, "1")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .setContentTitle("Podcast Player")
            .setContentText(text)
            .setContentIntent(pendingIntent)
            .addAction(NotificationCompat.Action(android.R.drawable.ic_media_play, actionName, playPendingIntent))
            .build()
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (currentEpisode != null) {
                updateNotification(currentEpisode!!.title)
                playPodcast(currentEpisode!!, currentHolder!!)
            }
        }
    }

    inner class MusicBinder : Binder() {
        internal val playerService: PodcastPlayerService
            get() = this@PodcastPlayerService
    }

    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }
}
