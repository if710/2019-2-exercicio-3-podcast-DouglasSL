package br.ufpe.cin.android.podcast

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.itemlista.view.*

class ItemFeedAdapter (private val itemFeeds: List<ItemFeed>, private val ctx : Context) : RecyclerView.Adapter<ItemFeedAdapter.ViewHolder>() {

    override fun getItemCount(): Int = itemFeeds.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(ctx).inflate(R.layout.itemlista, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val itemFeed = itemFeeds[position]
        holder.title.text = itemFeed.title
        holder.date.text = itemFeed.pubDate

        holder.title.setOnClickListener{
            val intent = Intent(ctx, EpisodeDetailActivity::class.java)
            intent.putExtra(EpisodeDetailActivity.TITLE, itemFeed.title)
            intent.putExtra(EpisodeDetailActivity.DESCRIPTION, itemFeed.description)
            intent.putExtra(EpisodeDetailActivity.LINK, itemFeed.link)
            intent.putExtra(EpisodeDetailActivity.IMAGE_URL, itemFeed.imageUrl)
            ctx.startActivity(intent)
        }
    }

    class ViewHolder (item : View) : RecyclerView.ViewHolder(item) {
        val title = item.item_title
        val date = item.item_date
        val download = item.item_action

        init {
            download.setOnClickListener {
                Toast.makeText(
                    itemView.context,
                    "Downloading ${title.text}..",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }
}