package br.ufpe.cin.android.podcast

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_episode_detail.*
import android.graphics.BitmapFactory
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.net.URL


class EpisodeDetailActivity : AppCompatActivity() {

    companion object {
        const val TITLE = "TITLE"
        const val DESCRIPTION = "DESCRIPTION"
        const val LINK = "LINK"
        const val IMAGE_URL = "IMAGE_URL"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_episode_detail)

        ep_title.text = intent.getStringExtra(TITLE)
        ep_description.text = intent.getStringExtra(DESCRIPTION)
        ep_link.text = intent.getStringExtra(LINK)

        doAsync {
            val url = URL(intent.getStringExtra(IMAGE_URL))
            val bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream())

            uiThread { ep_img.setImageBitmap(bmp) }
        }
    }
}
