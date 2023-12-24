package com.example.lisbike_pora.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.example.lisbike_pora.R
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class ImageService : Service(), LifecycleOwner {
    private var interval:Long? = 10
    private lateinit var camera:Camera
    private lateinit var imageCapture: ImageCapture
    private lateinit var lifecycleRegistry: LifecycleRegistry
    private lateinit var cameraExecutor: ScheduledExecutorService
    private var recording = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        interval = intent?.getLongExtra("interval",10)
        val action = intent?.action
        if (action != null) {
            handleAction(action)
        }
      //  startForegroundService()
        return super.onStartCommand(intent, flags, startId)
    }
    private fun handleAction(action: String) {
        when (action) {
            ACTION_START_RECORDING -> startForegroundService()
            ACTION_STOP_RECORDING -> stopRecording()
        }
    }
    private fun startForegroundService() {
        if(!recording) {
            recording = true
            lifecycleRegistry = LifecycleRegistry(this)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                createNotificationChannel(notificationManager)

            val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setAutoCancel(false)
                .setOngoing(true)
                .setSmallIcon(R.drawable.icons8_compact_camera_50)
                .setContentTitle(CHANNEL_NAME)


            val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

            cameraProviderFuture.addListener({
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()

                try {
                    cameraProvider.unbindAll()

                    camera = cameraProvider.bindToLifecycle(
                        this,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        imageCapture
                    )

                    // Start capturing images every interval
                    startImageCapture()

                } catch (exc: Exception) {
                    Log.e(TAG, "Use case binding failed", exc)
                }
            }, ContextCompat.getMainExecutor(this))

            startForeground(NOTIFICATION_ID, notificationBuilder.build())
        }
    }

    private fun startImageCapture() {
        cameraExecutor = Executors.newScheduledThreadPool(1)
        cameraExecutor.scheduleAtFixedRate(
            {
                captureAndUploadImage()
            },
            0, // Initial delay (0 means start immediately)
            interval!!, // Interval between captures in seconds
            TimeUnit.SECONDS
        )
    }

    private fun captureAndUploadImage() {
        val imageFile = createTempFile("image", ".jpg")
        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(imageFile).build()
        imageCapture.takePicture(
            outputFileOptions,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    Log.d("Saved Image", "Image was successfully saved to ${imageFile.absolutePath}")
                    // Send the image file to the API
                    APIUtil.uploadStream(Uri.fromFile(imageFile), APIUtil.BASE_URL + "sensor", "0", "0", "0", APIUtil.MIME_JPEG, baseContext)
                }

                override fun onError(
                    exception: ImageCaptureException
                ) {
                    val errorType = exception.imageCaptureError
                    Log.e(TAG, "Error in takePicture - error type: $errorType")
                    Log.e(TAG, exception.message ?: "")
                }
            })
    }
    private fun stopRecording() {
        if (recording) {
            recording = false
            cameraExecutor.shutdown()
        }
    }

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
    companion object {
        const val CHANNEL_ID = "image_service_channel"
        const val CHANNEL_NAME = "Image Service"
        const val NOTIFICATION_ID = 1

        const val ACTION_START_RECORDING = "com.example.lisbike_pora.START_RECORDING"
        const val ACTION_STOP_RECORDING = "com.example.lisbike_pora.STOP_RECORDING"
    }
}