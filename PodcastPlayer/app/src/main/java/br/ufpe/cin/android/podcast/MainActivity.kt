package br.ufpe.cin.android.podcast

import android.Manifest
import android.app.NotificationManager
import android.content.*
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.net.URL
import android.os.Environment
import android.os.IBinder
import org.jetbrains.anko.ctx
import java.io.File

class MainActivity : AppCompatActivity() {

    internal var podcastPlayerService: PodcastPlayerService? = null
    internal var isBound = false
    private lateinit var itemFeedAdapter: ItemFeedAdapter

    private val sConn = object : ServiceConnection {
        override fun onServiceDisconnected(p0: ComponentName?) {
            podcastPlayerService = null
            isBound = false
        }

        override fun onServiceConnected(p0: ComponentName?, b: IBinder?) {
            val binder = b as PodcastPlayerService.MusicBinder
            podcastPlayerService = binder.playerService
            isBound = true
            itemFeedAdapter.podcastPlayerService = podcastPlayerService
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        itemFeedAdapter = ItemFeedAdapter(applicationContext)
        list.layoutManager = LinearLayoutManager(this)
        list.adapter = itemFeedAdapter

        val musicServiceIntent = Intent(this, PodcastPlayerService::class.java)
        startService(musicServiceIntent)

        checkPermissions()

        doAsync {
            val db = ItemFeedDatabase.getDatabase(applicationContext)
            var itemFeedList = listOf<ItemFeed>().toMutableList()

            try {
                var xml = URL("https://s3-us-west-1.amazonaws.com/podcasts.thepolyglotdeveloper.com/podcast.xml").readText()
                itemFeedList = Parser.parse(xml).toMutableList()

                for (i in 0 until itemFeedList.size) {
                    val path = ctx.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                    val file = File(path, "${itemFeedList[i].title}.mp3")
                    if (file.exists()) {
                        itemFeedList[i] = itemFeedList[i].copy(path = file.absolutePath)
                    }
                }
                db.itemFeedDao().addAll(itemFeedList)

            } catch (e: Throwable) {
                Log.e("ERROR", e.message.toString())
                itemFeedList = db.itemFeedDao().all().toMutableList()
            }

            uiThread {
                itemFeedAdapter.itemFeeds = itemFeedList
                list.adapter = itemFeedAdapter
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (!isBound) {
            val bindIntent = Intent(this, PodcastPlayerService::class.java)
            isBound = bindService(bindIntent, sConn, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        isBound = false
        unbindService(sConn)
        super.onStop()
    }

    override fun onDestroy() {
        podcastPlayerService!!.stopSelf()
        super.onDestroy()
    }

    fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 0
            )
        }
    }
}
