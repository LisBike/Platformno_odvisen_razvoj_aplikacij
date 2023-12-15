package com.example.lisbike_pora

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.lisbike_pora.databinding.FragmentAccelerometerStateBinding
import com.example.lisbike_pora.services.APIUtil
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

        var formattedDate: String = df.format(currentTime)
        binding.txtTime.text = formattedDate

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
            APIUtil.uploadData(accDataString, APIUtil.BASE_URL + "image", latitude!!, longitude!!, formattedDate, APIUtil.MIME_TEXT)
            Toast.makeText(requireActivity(), "Image uploading..", Toast.LENGTH_SHORT).show()
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
                        accDataString = "X: $x \\n Y: $y \\n Z: $z"
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