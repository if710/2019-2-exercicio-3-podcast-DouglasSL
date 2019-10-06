package br.ufpe.cin.android.podcast

import android.app.IntentService
import android.content.Intent
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL

private const val PODCAST_ID = "PODCAST_ID"

class DownloadService : IntentService("DownloadService") {

    override fun onHandleIntent(intent: Intent?) {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
               return
            }

            val root = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            root?.mkdirs()
            val title = intent!!.getStringExtra(PODCAST_ID)
            val output = File(root, "${title}.mp3")
            if (output.exists()) {
                output.delete()
            }

            val url = URL(intent!!.data!!.toString())
            val con = url.openConnection() as HttpURLConnection
            val fos = FileOutputStream(output.path)
            val out = BufferedOutputStream(fos)
            try {
                val `in` = con.inputStream
                val buffer = ByteArray(8192)
                var len = `in`.read(buffer)
                while (len >= 0) {
                    out.write(buffer, 0, len)
                    len = `in`.read(buffer)
                }
                out.flush()
            } finally {
                Log.e(javaClass.getName(), "Download complete")
                fos.fd.sync()
                out.close()
                con.disconnect()

                val db = ItemFeedDatabase.getDatabase(applicationContext)
                db.itemFeedDao().addPath(intent.getStringExtra(PODCAST_ID), output.path)
            }

        } catch (e: Exception) {
            Log.e(javaClass.getName(),  e.toString())
        }
    }

    companion object {
        fun startDownload(context: Context, id: String, url: String) {
            val intent = Intent(context, DownloadService::class.java).apply {
                putExtra(PODCAST_ID, id)
            }
            intent.data = Uri.parse(url)
            context.startService(intent)
        }
    }
}
