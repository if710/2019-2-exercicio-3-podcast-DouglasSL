package br.ufpe.cin.android.podcast

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.migration.Migration


@Database(entities = [ItemFeed::class], version = 3, exportSchema = false)
abstract class ItemFeedDatabase : RoomDatabase() {
    abstract fun itemFeedDao(): ItemFeedDAO

    companion object {
        private var INSTANCE: ItemFeedDatabase? = null
        fun getDatabase(ctx: Context): ItemFeedDatabase {
            if (INSTANCE == null) {
                synchronized(ItemFeedDatabase::class) {
                    INSTANCE = Room.databaseBuilder(
                        ctx.applicationContext,
                        ItemFeedDatabase::class.java,
                        "itemFeed.db")
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }
            return INSTANCE!!
        }
    }
}