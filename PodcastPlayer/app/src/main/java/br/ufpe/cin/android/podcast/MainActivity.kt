package br.ufpe.cin.android.podcast

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.net.URL

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        list.layoutManager = LinearLayoutManager(this)
        list.adapter = ItemFeedAdapter(listOf<ItemFeed>(), this)

        doAsync {
            val db = ItemFeedDatabase.getDatabase(applicationContext)
            var itemFeedList = listOf<ItemFeed>()

            try {
                var xml = URL("https://s3-us-west-1.amazonaws.com/podcasts.thepolyglotdeveloper.com/podcast.xml").readText()
                itemFeedList = Parser.parse(xml)
                db.itemFeedDao().addAll(itemFeedList)
            } catch (e: Throwable) {
                Log.e("ERROR", e.message.toString())
                itemFeedList = db.itemFeedDao().all()
            }

            uiThread {
                list.adapter = ItemFeedAdapter(itemFeedList, applicationContext)
            }
        }
    }
}
