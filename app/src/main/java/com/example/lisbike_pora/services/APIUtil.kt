package com.example.lisbike_pora.services

import android.content.Context
import android.net.Uri
import android.util.Base64
import android.util.Base64OutputStream
import android.util.Log
import android.widget.Toast
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.IOException

object APIUtil {
    private val client = OkHttpClient()

    fun uploadStream(uri: Uri, url: String, lat: String, lng: String, time:String, mime: MediaType, context: Context) {
        val inStream = context.contentResolver.openInputStream(uri)
        val base64 = inStream.let {
            inStream.use {
                ByteArrayOutputStream().use { outputStream ->
                    Base64OutputStream(outputStream, Base64.DEFAULT)!!.use { base64FilterStream ->
                        it!!.copyTo(base64FilterStream)
                        base64FilterStream.flush()
                    }
                    outputStream.toString()
                }
            }
        }
        val cleanBase64 = base64.replace("\\s".toRegex(), "")
        val mode: String = mime.toString()

        val data = """{"mode":"$mode","lat":"$lat","lng":"$lng","time:":"$time","file":"$cleanBase64"}""".toRequestBody(MIME_JSON)
        val request = Request.Builder()
            .url(url)
            .post(data)
            .build()
        Thread {
            try {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                    Log.d("Image",response.body!!.string())
                }
            } catch (e: Exception) {
                Log.d("APIUtil-File", "Error when sending file:\n$e")
            }
        }.start()
    }
    val BASE_URL = "http://164.8.200.230:3000/net/"
    val MIME_JPEG = "image/jpeg".toMediaType()
    val MIME_JSON = "application/json".toMediaType()
}