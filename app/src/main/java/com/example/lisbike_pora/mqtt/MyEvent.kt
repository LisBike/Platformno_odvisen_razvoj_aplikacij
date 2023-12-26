package com.example.lisbike.mqtt

import com.example.lisbike_pora.mqtt.MyLocation

class MyEvent (
    var bike_availabilty:String,
    val location: MyLocation,
    var time: String
) {
    override fun toString(): String {
        return "bike_availabilty='$bike_availabilty', location=${location.toString()}, time='$time'"
    }
}