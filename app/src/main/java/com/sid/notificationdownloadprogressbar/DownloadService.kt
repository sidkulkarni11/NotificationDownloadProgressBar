package com.sid.notificationdownloadprogressbar

import android.R
import android.app.*
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.Builder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.random.Random


class DownloadService : Service() {
    var ACTION_STOP_SERVICE = "STOP"

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onCreate() {
        super.onCreate()
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {


        if (ACTION_STOP_SERVICE.equals(intent?.getAction())) {
            stopSelf();
        }

        val manager = getSystemService(
            NotificationManager::class.java
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                "CHANNEL_ID",
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            manager.createNotificationChannel(serviceChannel)
        }

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val progressMax = 100

        val stopSelf = Intent(this, DownloadService::class.java)
        stopSelf.action = this.ACTION_STOP_SERVICE
        stopSelf.setAction(ACTION_STOP_SERVICE)
        val pStopSelf = PendingIntent.getService(
            this,
            0,
            stopSelf,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification =
            NotificationCompat.Builder(this, "CHANNEL_ID")
                .setSmallIcon(R.drawable.ic_delete)
                .addAction(R.drawable.ic_lock_idle_alarm, "Stop", pStopSelf)
                .setContentTitle("GeeksforGeeks")
                .setContentText("Downloading")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setProgress(progressMax, 0, true)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)


//        manager.notify(1, notification.build())


        startForeground(1, notification.build())

        if (!ACTION_STOP_SERVICE.equals(intent?.getAction())) {
            downloadFileUsingRetrofit(notification, manager)
        }


        /*Thread(Runnable {
            SystemClock.sleep(2000)
            var progress = 0
            while (progress <= progressMax) {
                SystemClock.sleep(
                    1500
                )
                progress += 10
                //Use this to make it a Fixed-duration progress indicator notification

                notification.setContentText(progress.toString() + "%")
                    .setProgress(progressMax, progress, false)

                manager.notify(1, notification.build())
            }

            notification.setContentText("Download complete")
                .setProgress(0, 0, false)
                .setOngoing(false)
            manager.notify(1, notification.build())

//            stopSelf()

        }).start()*/
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun createNotificationChannel() {

    }

    fun downloadFileUsingRetrofit(notification: Builder, manager: NotificationManager) {


        val downloadApi = RetrofitHelper.getInstance().create(DownloadInterface::class.java)
        val coroutineScope = CoroutineScope(Dispatchers.IO)
        coroutineScope.launch {
            try {
                /*val response =
                    downloadApi.downloadFile("https://www.google.com/images/branding/googlelogo/1x/googlelogo_color_272x92dp.png")
                */
                var apkTestUrl  = "https://sample-videos.com/img/Sample-jpg-image-2mb.jpg"
//                var apkTestUrl  = "https://www.google.com/images/branding/googlelogo/1x/googlelogo_color_272x92dp.png"
//                var apkTestUrl  = "https://images.pexels.com/photos/268533/pexels-photo-268533.jpeg?cs=srgb&dl=pexels-pixabay-268533.jpg&fm=jpg"
                val response =
                    downloadApi.downloadFile(apkTestUrl)

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        val bytes = response.body()?.bytes()
                        if (bytes != null) {
                            // Save the image to disk or display it in an ImageView
                            // For example:
                            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            var img = bitmap

                            saveFileToDisk(bytes, notification, manager)
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

    private fun saveFileToDisk(
        bytes: ByteArray,
        notification: Builder,
        manager: NotificationManager
    ) {
        try {
            var filename = "bigrattle_test"

            var bytesWritten = 0
            var progress = 0

            while (fileExists(filename))
                filename = filename + Random.nextInt(10000).toString()


            val downloadsDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, filename)
            file.createNewFile()
            val outputStream = FileOutputStream(file)

            var totalBytes = bytes.size

            while (bytesWritten < totalBytes) {
                val remainingBytes = totalBytes - bytesWritten
                val bufferSize = if (remainingBytes > 1024) 1024 else remainingBytes
                outputStream.write(bytes, bytesWritten, bufferSize)
                bytesWritten += bufferSize
                progress = (bytesWritten.toDouble() / totalBytes.toDouble() * 100).toInt()
                // Update progress
                publishProgress(progress, notification, manager)
            }
            outputStream.close()

            notification.setContentText("Download complete")
                .setProgress(0, 0, false)
                .setOngoing(false)
            manager.notify(1, notification.build())
            stopSelf()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun publishProgress(
        progValue: Int,
        notification: Builder,
        manager: NotificationManager
    ) {
        var pV = progValue

        var progressMax = 100

        notification.setContentText(progValue.toString() + "%")
            .setProgress(progressMax, progValue, false)

        manager.notify(1, notification.build())
        /*   while (progress <= progressMax) {
               SystemClock.sleep(
                   1500
               )
               progress += 10
               notification.setContentText(progress.toString() + "%")
                   .setProgress(progressMax, progress, false)

               manager.notify(1, notification.build())
           }*/

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