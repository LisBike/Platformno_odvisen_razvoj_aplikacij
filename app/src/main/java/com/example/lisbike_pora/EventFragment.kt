package com.example.lisbike_pora

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.Navigation
import com.example.lisbike.mqtt.MqttHelper
import com.example.lisbike.mqtt.MyEvent
import com.example.lisbike_pora.databinding.FragmentEventBinding
import com.example.lisbike_pora.mqtt.MyLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
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

    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private val permissionId = 2

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
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        var formattedDate: String = df.format(currentTime)
        binding.txtTime.text = formattedDate
        mqttHelper = MqttHelper(requireContext().applicationContext)
        startMqtt()
        binding.buttonLocation.setOnClickListener {
            getLocation()
        }
        setFragmentResultListener("requestKey") { key, bundle ->
            latitude = bundle.getString("latitude")
            longitude = bundle.getString("longitude")
            binding.txtViewLocation.text = "$latitude $longitude"
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

    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun getLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.lastLocation.addOnCompleteListener(requireActivity()) { task ->
                    val location: Location? = task.result
                    if (location != null) {
                        val geocoder = Geocoder(requireContext(), Locale.getDefault())
                        val list: List<Address> =
                            geocoder.getFromLocation(location.latitude, location.longitude, 1) as List<Address>

                        latitude = location.latitude.toString()
                        longitude = location.longitude.toString()

                        Toast.makeText(requireContext(), "Address\n${list[0].getAddressLine(0)}", Toast.LENGTH_SHORT).show()
                        binding.txtViewLocation.text = "lat: $latitude lng:$longitude"
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Please turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            permissionId
        )
    }
    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == permissionId) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLocation()
            }
        }
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