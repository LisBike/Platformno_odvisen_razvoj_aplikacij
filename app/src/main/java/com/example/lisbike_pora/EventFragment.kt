package com.example.lisbike_pora

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.lisbike.mqtt.MqttHelper
import com.example.lisbike.mqtt.MyEvent
import com.example.lisbike_pora.databinding.FragmentEventBinding
import com.example.lisbike_pora.mqtt.MyLocation
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.text.SimpleDateFormat
import java.util.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [EventFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EventFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentEventBinding
    private lateinit var mqttHelper: MqttHelper
    private var event: MyEvent = MyEvent("", MyLocation("",""),"")
    var currentTime: Date = Calendar.getInstance().getTime()
    var df: SimpleDateFormat = SimpleDateFormat("dd-MMM-yyyy hh:mm:ss", Locale.getDefault())

    private var latitude: String? = "0"
    private var longitude: String? = "0"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        // return inflater.inflate(R.layout.fragment_event, container, false)
        binding = FragmentEventBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var formattedDate: String = df.format(currentTime)
        binding.txtTime.text = formattedDate
        mqttHelper = MqttHelper(requireContext().applicationContext)
        startMqtt()
        binding.buttonLocation.setOnClickListener {

        }

        // extreme event
        binding.editTextAvailableBikes.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //
            }

            override fun afterTextChanged(s: Editable?) {
                triggerExtremeEvent()
            }
        })


        val spinner = binding.spinnerStationType

        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.station_types,
            R.layout.spinner_item
        )

        adapter.setDropDownViewResource(R.layout.dropdown_list)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                // Get the selected item from the spinner
                val selectedOption = spinner.selectedItem.toString()

                binding.btnSave.setOnClickListener {
                    // Generate the topic based on the selected option
                    var generatedTopic = when (selectedOption) {
                        "Bike Station-Start" -> "${MqttHelper.SUBSCRIPTION_TOPIC}/start"
                        "Bike Station-Destination" -> "${MqttHelper.SUBSCRIPTION_TOPIC}/destination"
                        // Add more cases if needed
                        else -> "${MqttHelper.SUBSCRIPTION_TOPIC}/default"  // Default topic if none of the above cases match
                    }
                    event.bike_availabilty = binding.editTextAvailableBikes.text.toString()
                    event.time = formattedDate
                    event.location.latitude = latitude
                    event.location.longitude = longitude
                    mqttHelper.publish(generatedTopic, event)

                    Navigation.findNavController(view!!)
                        .navigate(R.id.action_eventFragment_to_homeFragment)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
    }
    private fun startMqtt() {
        mqttHelper.mqttClient.setCallback(object : MqttCallback {
            override fun messageArrived(topic: String?, message: MqttMessage?) {
                Log.d(MqttHelper.TAG, "Receive message: ${message.toString()} from topic: $topic")
            }

            override fun connectionLost(cause: Throwable?) {
                Log.d(MqttHelper.TAG, "Connection lost ${cause.toString()}")
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {

            }
        })
    }
    private fun triggerExtremeEvent() {
        val availableBikes = binding.editTextAvailableBikes.text.toString()

        // Check if the station is full (0 available bikes)
        if (availableBikes == "0") {
            // If the station is full, trigger the extreme event
            val extremeTopic = "${MqttHelper.SUBSCRIPTION_TOPIC}/extreme"
            event.bike_availabilty = "0"
            event.time = df.format(currentTime)
            event.location.latitude = latitude
            event.location.longitude = longitude
            mqttHelper.publish(extremeTopic, event)

            Toast.makeText(
                requireContext(),
                "Station is full! Extreme event triggered.",
                Toast.LENGTH_SHORT
            ).show()

            Navigation.findNavController(requireView())
                .navigate(R.id.action_eventFragment_to_homeFragment)
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        mqttHelper.disconnect()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment EventFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            EventFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}