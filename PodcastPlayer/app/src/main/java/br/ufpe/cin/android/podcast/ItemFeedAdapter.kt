package br.ufpe.cin.android.podcast

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.itemlista.view.*
import org.jetbrains.anko.doAsync


class ItemFeedAdapter (private val ctx : Context) : RecyclerView.Adapter<ItemFeedAdapter.ViewHolder>() {

    var podcastPlayerService: PodcastPlayerService? = null
    var itemFeeds = listOf<ItemFeed>()

    override fun getItemCount(): Int = itemFeeds.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(ctx).inflate(R.layout.itemlista, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val itemFeed = itemFeeds[position]
        holder.title.text = itemFeed.title
        holder.date.text = itemFeed.pubDate
        holder.player.isEnabled = false

        doAsync {
            val db = ItemFeedDatabase.getDatabase(ctx)
            val item = db.itemFeedDao().search(itemFeed.title)
            if (!item.path.equals("")) {
                holder.player.isEnabled = true
            }
        }

        holder.title.setOnClickListener{
            val intent = Intent(ctx, EpisodeDetailActivity::class.java)
            intent.putExtra(EpisodeDetailActivity.TITLE, itemFeed.title)
            intent.putExtra(EpisodeDetailActivity.DESCRIPTION, itemFeed.description)
            intent.putExtra(EpisodeDetailActivity.LINK, itemFeed.link)
            intent.putExtra(EpisodeDetailActivity.IMAGE_URL, itemFeed.imageUrl)
            ctx.startActivity(intent)
        }

        holder.download.setOnClickListener {
            DownloadService.startDownload(ctx, itemFeed.title, itemFeed.downloadLink)
        }

        holder.player.setOnClickListener {
            podcastPlayerService!!.playPodcast(itemFeed.path, itemFeed.title)
        }
    }

    class ViewHolder (item : View) : RecyclerView.ViewHolder(item) {
        val title = item.item_title
        val date = item.item_date
        val download = item.item_action
        val player = item.item_play
    }
}