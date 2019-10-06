package br.ufpe.cin.android.podcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import org.jetbrains.anko.doAsync

const val ACTION_DOWNLOAD = "br.ufpe.cin.android.podcast.services.action.DOWNLOAD_COMPLETE"

class DownloaderReceiver(holder: ItemFeedAdapter.ViewHolder) : BroadcastReceiver() {

    private val downloadButton = holder.download
    private val playerButton = holder.player

    override fun onReceive(context: Context, intent: Intent) {
        downloadButton.isEnabled = true
        Toast.makeText(context,"Download complete",Toast.LENGTH_SHORT).show()

        val title = intent.getStringExtra(DownloadService.PODCAST_ID)
        val path = intent.getStringExtra(DownloadService.PODCAST_PATH)
        val db = ItemFeedDatabase.getDatabase(context)

        doAsync {
            db.itemFeedDao().addPath(title, path)
            playerButton.isEnabled = true
        }
    }
}
