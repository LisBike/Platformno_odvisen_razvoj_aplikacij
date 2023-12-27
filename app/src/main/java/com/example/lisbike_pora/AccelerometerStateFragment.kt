package com.example.lisbike_pora

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.Navigation
import com.example.lisbike_pora.databinding.FragmentAccelerometerStateBinding
import com.example.lisbike_pora.services.APIUtil
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AccelerometerStateFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AccelerometerStateFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentAccelerometerStateBinding
    private lateinit var sensorManager: SensorManager
    private var accelerometerSensor: Sensor? = null
    private var sensorEventListener: SensorEventListener? = null

    var currentTime: Date = Calendar.getInstance().getTime()
    var df: SimpleDateFormat = SimpleDateFormat("dd-MMM-yyyy hh:mm:ss", Locale.getDefault())
    var accDataString = ""

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
        // return inflater.inflate(R.layout.fragment_accelerometer_state, container, false)
        binding = FragmentAccelerometerStateBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        var formattedDate: String = df.format(currentTime)
        binding.txtTime.text = formattedDate

        binding.buttonLocation.setOnClickListener {
            getLocation()
        }

        setFragmentResultListener("requestKey") { key, bundle ->
            latitude = bundle.getString("latitude")
            longitude = bundle.getString("longitude")
            binding.txtViewLocation.text = "$latitude $longitude"
        }

        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        binding.switchBtn.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                // Switch is ON
                Toast.makeText(requireContext(), "Accelerometer is ON", Toast.LENGTH_SHORT).show()
                registerAccelerometerSensor()

            } else {
                // Switch is OFF
                Toast.makeText(requireContext(), "Accelerometer is OFF", Toast.LENGTH_SHORT).show()
                unregisterAccelerometerSensor()

            }
        }
        binding.btnSave.setOnClickListener {
            APIUtil.uploadData(accDataString, APIUtil.BASE_URL + "sensor", latitude!!, longitude!!, formattedDate, APIUtil.MIME_TEXT)
            Toast.makeText(requireActivity(), "Data uploading..", Toast.LENGTH_SHORT).show()
        }

    }
    private fun registerAccelerometerSensor() {
        if (accelerometerSensor != null && sensorEventListener == null) {
            sensorEventListener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    if (event != null) {
                        val x = event.values[0]
                        val y = event.values[1]
                        val z = event.values[2]
                        binding.txtViewAcc.text = "X: $x \n Y: $y \n Z: $z"
                        accDataString = "X: $x  Y: $y   Z: $z"
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                    //
                }
            }

            sensorManager.registerListener(
                sensorEventListener,
                accelerometerSensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    private fun unregisterAccelerometerSensor() {
        if (sensorEventListener != null) {
            sensorManager.unregisterListener(sensorEventListener)
            sensorEventListener = null
        }
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
         * @return A new instance of fragment AccelerometerStateFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AccelerometerStateFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}