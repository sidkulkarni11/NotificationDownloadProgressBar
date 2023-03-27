package com.sid.notificationdownloadprogressbar

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.*
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import kotlin.random.Random


class MainActivity : AppCompatActivity() {

    private lateinit var notificationManager: NotificationManagerCompat
    val channelId = "Progress Notification" as String
    lateinit var notifClickButton: Button
    lateinit var downloadFileButton: Button
    var clickAllowed = true

    var progressBar: ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Create a Notification Manager
//        notificationManager = NotificationManagerCompat.from(this)


        notifClickButton = findViewById(R.id.showNotif)
        downloadFileButton = findViewById(R.id.downloadFile)

        notifClickButton.setOnClickListener {
            if (clickAllowed) {
//
                startService()
                clickAllowed = false
            }
        }

        downloadFileButton.setOnClickListener {
            clickAllowed = true
//            downloadFileUsingRetrofit()
            /*val executor: ExecutorService = Executors.newSingleThreadScheduledExecutor()
            val handler = Handler(Looper.getMainLooper())

            executor.execute(object : Runnable {
                var count = 0
                override fun run() {

                    //Background work here
                    try {

                        // put your url.this is sample url.
                        val url = URL("https://images.unsplash.com/photo-1578135568951-0291665c42b3?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=764&q=80")
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
            })*/
        }
    }

    //Start() is called when the buttons is pressed.
    public fun start(view: View) {

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this, 0, intent, 0
        )

        //Sets the maximum progress as 100
        val progressMax = 100
        //Creating a notification and setting its various attributes
        val notification =
            NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_sharp_arrow_downward_24)
                .setContentTitle("GeeksforGeeks")
                .setContentText("Downloading")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setProgress(progressMax, 0, true)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

        //Initial Alert
        notificationManager.notify(1, notification.build())

        Thread(Runnable {
            SystemClock.sleep(2000)
            var progress = 0
            while (progress <= progressMax) {
                SystemClock.sleep(
                    1000
                )
                progress += 20
                //Use this to make it a Fixed-duration progress indicator notification

                notification.setContentText(progress.toString() + "%")
                    .setProgress(progressMax, progress, false)

                notificationManager.notify(1, notification.build())
            }

            notification.setContentText("Download complete")
                .setProgress(0, 0, false)
                .setOngoing(false)
            notificationManager.notify(1, notification.build())
        }).start()
    }

    fun startService() {
        val serviceIntent = Intent(this, DownloadService::class.java)
        serviceIntent.putExtra("inputExtra", "Foreground Service Example in Android")
        ContextCompat.startForegroundService(this, serviceIntent)
    }

    private fun publishProgress(vararg progress: Int) {
        progressBar?.setProgress(progress[0])
    }

    fun startDownloadFileService() {
        val serviceIntent = Intent(this, DownloadFileService::class.java)
        serviceIntent.putExtra("inputExtra", "Foreground Service Example in Android")
        ContextCompat.startForegroundService(this, serviceIntent)
    }

    fun downloadFileUsingRetrofit() {


        val downloadApi = RetrofitHelper.getInstance().create(DownloadInterface::class.java)
        val coroutineScope = CoroutineScope(Dispatchers.IO)
        coroutineScope.launch {
            try {
                val response =
                    downloadApi.downloadFile("https://www.google.com/images/branding/googlelogo/1x/googlelogo_color_272x92dp.png")

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        val bytes = response.body()?.bytes()
                        if (bytes != null) {
                            // Save the image to disk or display it in an ImageView
                            // For example:
                            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            var img = bitmap

                            saveFileToDisk( this@MainActivity,bytes)
                        } else {
                            // Handle error
                        }
                    } else {
                        // Handle error
                    }
                } else {
                    // Handle error
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        /*   downloadFileCall.enqueue(object : Callback<ResponseBody> {
               override fun onResponse(
                   call: Call<ResponseBody>,
                   response: Response<ResponseBody>
               ) {
                   if (response.isSuccessful) {
                       // Get the file as a byte array
                       val bytes = response.body()?.bytes()
                       if (bytes != null) {
                           // Save the file to disk
                           saveFileToDisk(bytes)
                       } else {
                           // Handle error
                       }
                   } else {
                       // Handle error
                   }
               }

               override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                   // Handle error
               }
           })*/


    }

    private fun saveFileToDisk( context: Context,bytes: ByteArray) {
        try {
            var filename = "bigrattle_test"


            while (fileExists(filename))
                filename = filename + Random.nextInt(10000).toString()

            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, filename)

            file.createNewFile()
            val outputStream = FileOutputStream(file)
            outputStream.write(bytes)
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun fileExists(filename: String): Boolean {
        try {
            var file =
                File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path + "/" + filename)
            if (file.exists()) {
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }
}