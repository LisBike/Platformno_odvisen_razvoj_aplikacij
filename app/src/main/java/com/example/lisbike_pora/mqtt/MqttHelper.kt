package com.example.lisbike.mqtt

import android.content.Context
import android.util.Log
import android.widget.Toast
import info.mqtt.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.json.JSONObject


class MqttHelper (var context: Context) {

    public var mqttClient: MqttAndroidClient

    companion object {
        val TAG = "AndroidMqttClient"
        const val SERVER_URI = "tcp://broker.emqx.io:1883"
        var SUBSCRIPTION_TOPIC = "lisbike"
        val clientId: String = MqttClient.generateClientId()
    }

    init {
        val options = MqttConnectOptions()
        mqttClient = MqttAndroidClient(context, SERVER_URI, clientId)
        try {
            mqttClient.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "Connection success")
                    subscribe(SUBSCRIPTION_TOPIC)
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG, "Connection failure")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun subscribe(topic: String, qos: Int = 1) {
        try {
            if(mqttClient.isConnected) {
                mqttClient.subscribe(topic, qos, null, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        Log.d(TAG, "Subscribed to $topic")
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        Log.d(TAG, "Failed to subscribe $topic")
                    }
                })
            }
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun unsubscribe(topic: String) {
        try {
            mqttClient.unsubscribe(topic, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "Unsubscribed to $topic")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG, "Failed to unsubscribe $topic")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun publish(topic: String, event: MyEvent, qos: Int = 1, retained: Boolean = false) {
        try {
            val obj = JSONObject()

            obj.put("content", event.bike_availabilty)
            obj.put("latitude", event.location.latitude)
            obj.put("longitude", event.location.longitude)
            obj.put("time", event.time)

            val message = MqttMessage()
            message.payload = obj.toString().toByteArray()
            message.qos = qos
            message.isRetained = retained
            if(mqttClient.isConnected) {
                mqttClient.publish(topic, message, null, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        Toast.makeText(context, "Successfully Added Event!",
                            Toast.LENGTH_SHORT).show()
                        Log.d(TAG, "${event.toString()} published to $topic")
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        Log.d(TAG, "Failed to publish $event to $topic")
                    }
                })
            }
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun disconnect() {
        try {
            mqttClient.disconnect(null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "Disconnected")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG, "Failed to disconnect")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }
}