package br.ufpe.cin.android.podcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.jetbrains.anko.doAsync
import java.io.File

const val ACTION_DELETE_FILE = "br.ufpe.cin.android.podcast.services.action.DELETE_FILE"

class DeleteFileReceiver(holder: ItemFeedAdapter.ViewHolder) : BroadcastReceiver() {

    private val playerButton = holder.player

    override fun onReceive(context: Context, intent: Intent) {
        playerButton.isEnabled = false

        var db = ItemFeedDatabase.getDatabase(context)
        var title = intent.getStringExtra(DownloadService.PODCAST_ID)

        doAsync {
            var item = db.itemFeedDao().search(title)
            db.itemFeedDao().updatePosition(title, 0)
            db.itemFeedDao().addPath(title, "")

            var file = File(item.path, "${title}.mp3")
            if (file.exists()) {
                file.delete()
            }
        }
    }
}
