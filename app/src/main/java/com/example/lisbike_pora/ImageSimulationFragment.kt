package com.example.lisbike_pora

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.lisbike_pora.databinding.FragmentImageInputBinding
import com.example.lisbike_pora.databinding.FragmentImageSimulationBinding
import com.example.lisbike_pora.services.ImageService

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ImageSimulationFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ImageSimulationFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding:FragmentImageSimulationBinding

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
        // return inflater.inflate(R.layout.fragment_image_simulation, container, false)
        binding = FragmentImageSimulationBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.switchSimulation.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked) {
                val serviceIntent = Intent(requireActivity(), ImageService::class.java)
                serviceIntent.putExtra("interval", binding.editTextTime.text.toString().toLong())
                serviceIntent.action = ImageService.ACTION_START_RECORDING
                requireActivity().startService(serviceIntent)
                Toast.makeText(requireActivity(), "Service started!", Toast.LENGTH_SHORT).show()
            } else {
                val stopRecordingIntent = Intent(requireActivity(), ImageService::class.java)
                stopRecordingIntent.action = ImageService.ACTION_STOP_RECORDING
                requireActivity().startService(stopRecordingIntent)
                Toast.makeText(requireActivity(), "Service stopped!", Toast.LENGTH_SHORT).show()
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
         * @return A new instance of fragment ImageSimulationFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ImageSimulationFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}