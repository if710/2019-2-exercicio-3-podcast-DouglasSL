package br.ufpe.cin.android.podcast

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ItemFeedDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(itemFeed: ItemFeed)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addAll(itemFeeds: List<ItemFeed>)

    @Query("SELECT * FROM itemfeed")
    fun all() : List<ItemFeed>

    @Query("SELECT * FROM itemfeed WHERE title LIKE :t")
    fun search(t : String) : ItemFeed
}