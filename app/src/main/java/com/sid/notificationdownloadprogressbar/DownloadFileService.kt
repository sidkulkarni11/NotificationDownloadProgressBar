package com.sid.notificationdownloadprogressbar

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.View
import android.widget.ProgressBar
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.URL
import java.net.URLConnection
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class DownloadFileService : Service(){
    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onCreate() {
        super.onCreate()
    }

    var progressBar: ProgressBar? = null


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val executor: ExecutorService = Executors.newSingleThreadScheduledExecutor()
        val handler = Handler(Looper.getMainLooper())

        executor.execute(object : Runnable {
            var count = 0
            override fun run() {

                //Background work here
                try {

                    // put your url.this is sample url.
                    val url = URL("https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf")
                    val conection: URLConnection = url.openConnection()
                    conection.connect()
                    val lenghtOfFile: Int = conection.getContentLength()

                    // download the file
                    val input: InputStream = conection.getInputStream()

                    //catalogfile is your destenition folder
                    val output: OutputStream = FileOutputStream(System.currentTimeMillis().toString() + "testbigrattle.pdf")
                    val data = ByteArray(1024)
                    var total: Long = 0
                    while (input.read(data).also { count = it } != -1) {
                        total += count.toLong()
                        // publishing the progress....
                        publishProgress(Integer.valueOf("" + (total * 100 / lenghtOfFile).toInt()))

                        // writing data to file
                        output.write(data, 0, count)
                    }

                    // flushing output
                    output.flush()

                    // closing streams
                    output.close()
                    input.close()
                    handler.post(Runnable { //UI Thread work here
                        progressBar?.setVisibility(View.GONE)
                    })

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })
        return START_NOT_STICKY
    }
    private fun publishProgress(vararg progress: Int) {
        progressBar?.setProgress(progress[0])
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}