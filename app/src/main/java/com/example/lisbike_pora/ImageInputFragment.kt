package com.example.lisbike_pora

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.setFragmentResultListener
import com.example.lisbike_pora.databinding.FragmentImageInputBinding
import com.example.lisbike_pora.services.APIUtil
import java.text.SimpleDateFormat
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ImageInputFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ImageInputFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentImageInputBinding
    private var image_url: Uri? = null
    private var CAPTURE_CODE:Int = 1001
    private val REQUEST_CAMERA_AND_STORAGE_PERMISSIONS = 1002 // Choose any unique value
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
        // return inflater.inflate(R.layout.fragment_image_input, container, false)
        binding = FragmentImageInputBinding.inflate(inflater,container,false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.imageButtonCamera.setOnClickListener {
                openCamera()
        }
        var formattedDate: String = df.format(currentTime)
        binding.txtTime.text = formattedDate
//        setFragmentResultListener("requestKey") { key, bundle ->
//            latitude = bundle.getString("latitude")
//            longitude = bundle.getString("longitude")
//            binding.txtViewLocation.text = "$latitude $longitude"
//
//        }
        binding.btnSave.setOnClickListener {
            APIUtil.uploadStream(image_url!!, APIUtil.BASE_URL + "image", latitude!!, longitude!!, formattedDate, APIUtil.MIME_JPEG, requireActivity())
            Toast.makeText(requireActivity(), "Image uploading..", Toast.LENGTH_SHORT).show()
            Log.d("ImageInputFragment", "Image uploading..")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK) {
            binding.txtViewUrl.text = image_url.toString()
        }
    }

    fun openCamera() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request the necessary permissions.
            requestCameraAndStoragePermissions()
        } else {
            // Permissions are granted. Proceed with opening the camera.
            launchCameraIntent()
        }
    }
    private fun requestCameraAndStoragePermissions() {
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        ActivityCompat.requestPermissions(
            requireActivity(),
            permissions,
            REQUEST_CAMERA_AND_STORAGE_PERMISSIONS
        )
    }

    private fun launchCameraIntent() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "new image")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From camera")
        image_url =
            requireContext().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_url)
        startActivityForResult(cameraIntent, CAPTURE_CODE)
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CAMERA_AND_STORAGE_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // All required permissions granted. Proceed with opening the camera.
                launchCameraIntent()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Camera and storage permissions are required",
                    Toast.LENGTH_SHORT
                ).show()
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
         * @return A new instance of fragment ImageInputFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ImageInputFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}