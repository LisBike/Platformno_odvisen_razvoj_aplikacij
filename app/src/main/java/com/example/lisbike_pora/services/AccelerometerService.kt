package com.example.lisbike_pora.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.lisbike_pora.MainActivity
import com.example.lisbike_pora.R
import java.util.*

class AccelerometerService : Service(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var interval:Long? = 10
    private var from:Float = 0f
    private var to:Float = 10f
    private var latitude:String? = ""
    private var longitude:String? = ""
    private var accDataString = ""
    private var isRecording = false
    private var notificationManager: NotificationManager? = null

    override fun onCreate() {
        super.onCreate()

        // Initialize the SensorManager and accelerometer
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // Initialize NotificationManager
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createNotificationChannel()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        interval = intent?.getLongExtra("interval",10)
        from = intent!!.getFloatExtra("from",0f)
        to = intent.getFloatExtra("to",10f)
        latitude = intent.getStringExtra("latitude")
        longitude = intent.getStringExtra("longitude")
        val action = intent?.action
        if (action != null) {
            handleAction(action)
        }
        return super.onStartCommand(intent, flags, startId)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleAction(action: String) {
        when (action) {
            ACTION_START_RECORDING -> startForegroundService()
            ACTION_STOP_RECORDING -> stopForegroundService()
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun startForegroundService() {
        if (!isRecording) {
            isRecording = true
            // Create a notification and put the service in the foreground
            val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setAutoCancel(false)
                .setOngoing(true)
                .setSmallIcon(R.drawable.icons8_accelerometer_64)
                .setContentTitle(CHANNEL_NAME)
            startForeground(NOTIFICATION_ID, notificationBuilder.build())

            // Schedule the periodic task for collecting accelerometer data
            val handler = android.os.Handler()
            val runnable = object : Runnable {
                override fun run() {
                    if (isRecording) {
                        // Collect accelerometer data
                        getAccelerometerData()
                        // Schedule the next data collection based on the interval
                        handler.postDelayed(this, interval!! * 1000)
                    }
                }
            }
            handler.postDelayed(runnable, interval!! * 1000)
        }
    }

    private fun stopForegroundService() {
        if (isRecording) {
            isRecording = false
            // Stop the foreground service and remove the notification
            stopForeground(true)
            notificationManager?.cancel(NOTIFICATION_ID)
            // Unregister accelerometer listener
            sensorManager.unregisterListener(this)
        }
    }

    private fun getAccelerometerData() {
        // This will call onSensorChanged, and the process will repeat based on the interval
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }


    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            // Generate random float values for x, y, and z within the specified range
            val random = Random()
            val x = random.nextFloat() * (to - from) + from
            val y = random.nextFloat() * (to - from) + from
            val z = random.nextFloat() * (to - from) + from

            accDataString = "X: $x  Y: $y   Z: $z"

            APIUtil.uploadSimulation(accDataString,APIUtil.BASE_URL + "simulation", latitude!!, longitude!!)

            Log.d("AccelerometerService", "X: $x, Y: $y, Z: $z")
            sensorManager.unregisterListener(this)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        //
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager?.createNotificationChannel(channel)
        }
    }


    companion object {
        const val CHANNEL_ID = "accelerometer_service_channel"
        const val CHANNEL_NAME = "Accelerometer Service"
        const val NOTIFICATION_ID = 1

        const val ACTION_START_RECORDING = "com.example.lisbike_pora.START_RECORDING"
        const val ACTION_STOP_RECORDING = "com.example.lisbike_pora.STOP_RECORDING"
    }
}