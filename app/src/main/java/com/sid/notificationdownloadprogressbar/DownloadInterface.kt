package com.sid.notificationdownloadprogressbar

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url

interface DownloadInterface {

    @Streaming
    @GET
    suspend fun downloadFile(@Url fileUrl:String): Response<ResponseBody>
}