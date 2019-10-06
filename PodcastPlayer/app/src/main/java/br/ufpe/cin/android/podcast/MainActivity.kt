package br.ufpe.cin.android.podcast

import android.Manifest
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
import org.jetbrains.anko.ctx
import java.io.File


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        list.layoutManager = LinearLayoutManager(this)
        list.adapter = ItemFeedAdapter(listOf<ItemFeed>(), this)

        checkPermissions()

        doAsync {
            val db = ItemFeedDatabase.getDatabase(applicationContext)
            var itemFeedList = listOf<ItemFeed>().toMutableList()

            try {
                var xml =
                    URL("https://s3-us-west-1.amazonaws.com/podcasts.thepolyglotdeveloper.com/podcast.xml").readText()
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
                list.adapter = ItemFeedAdapter(itemFeedList, applicationContext)
            }
        }
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
