package com.example.storydicobyarafin.ui.addstory

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.storydicobyarafin.R
import com.example.storydicobyarafin.databinding.ActivityAddStoryBinding
import com.example.storydicobyarafin.ui.ViewModelFactory
import com.example.storydicobyarafin.utils.ResultState
import com.example.storydicobyarafin.utils.reduceFileImage
import com.example.storydicobyarafin.utils.uriToFile
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class AddStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddStoryBinding
    private val viewModel by viewModels<AddStoryViewModel> {
        ViewModelFactory.getInstance(this)
    }

    private var currentImageUri: Uri? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d(TAG, "Camera permission granted by user. Launching CameraX Activity...")
            startCameraX()
        } else {
            Log.w(TAG, "Camera permission denied by user.")
            Toast.makeText(this, getString(R.string.error_permission_denied), Toast.LENGTH_SHORT).show()
        }
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            Log.d(TAG, "Image selected from Gallery: $uri")
            showImage()
        } else {
            Log.w(TAG, "No image selected from Gallery.")
            Toast.makeText(this, getString(R.string.error_empty_image), Toast.LENGTH_SHORT).show()
        }
    }

    private val launcherCameraX = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == CameraActivity.CAMERAX_RESULT) {
            val imageUriString = result.data?.getStringExtra(CameraActivity.EXTRA_CAMERAX_IMAGE)
            if (imageUriString != null) {
                currentImageUri = Uri.parse(imageUriString)
                Log.d(TAG, "CameraX image received in AddStoryActivity: $currentImageUri")
                showImage()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(TAG, "AddStoryActivity created.")

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.title_add_story)

        binding.btnGallery.setOnClickListener { startGallery() }
        binding.btnCamera.setOnClickListener { checkCameraPermissionAndStart() }
        binding.buttonAdd.setOnClickListener { uploadImage() }

        observeViewModel()
    }

    private fun checkCameraPermissionAndStart() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission already granted. Launching CameraX...")
            startCameraX()
        } else {
            Log.d(TAG, "Requesting CAMERA runtime permission...")
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startGallery() {
        Log.d(TAG, "Launching Gallery picker...")
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun startCameraX() {
        Log.d(TAG, "Launching CameraActivity with CameraX...")
        val intent = Intent(this, CameraActivity::class.java)
        launcherCameraX.launch(intent)
    }

    private fun showImage() {
        currentImageUri?.let {
            binding.ivPreview.setImageURI(it)
        }
    }

    private fun uploadImage() {
        currentImageUri?.let { uri ->
            val description = binding.edAddDescription.text.toString().trim()
            if (description.isEmpty()) {
                binding.edAddDescription.error = getString(R.string.error_empty_description)
                return
            }

            Log.d(TAG, "Preparing image for upload. Selected URI: $uri")
            val imageFile = uriToFile(uri, this).reduceFileImage()
            Log.d(TAG, "Image compressed. File size: ${imageFile.length()} bytes, name: ${imageFile.name}")

            val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaType())
            val multipartBody = MultipartBody.Part.createFormData(
                "photo",
                imageFile.name,
                requestImageFile
            )
            val requestBodyDescription = description.toRequestBody("text/plain".toMediaType())

            Log.d(TAG, "Initiating story upload API call...")
            viewModel.uploadStory(multipartBody, requestBodyDescription)
        } ?: run {
            Log.w(TAG, "Upload attempted without selecting an image.")
            Toast.makeText(this, getString(R.string.error_empty_image), Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        viewModel.uploadResult.observe(this) { result ->
            when (result) {
                is ResultState.Loading -> {
                    Log.d(TAG, "Story upload in progress...")
                    showLoading(true)
                }
                is ResultState.Success -> {
                    showLoading(false)
                    Log.d(TAG, "Story upload successful! Response message: ${result.data.message}")
                    Toast.makeText(this, getString(R.string.msg_upload_success), Toast.LENGTH_SHORT).show()
                    setResult(RESULT_SUCCESS)
                    finish()
                }
                is ResultState.Error -> {
                    showLoading(false)
                    Log.e(TAG, "Story upload failed with error: ${result.error}")
                    Toast.makeText(this, getString(R.string.msg_error_occurred, result.error), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.buttonAdd.isEnabled = !isLoading
        binding.btnCamera.isEnabled = !isLoading
        binding.btnGallery.isEnabled = !isLoading
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    companion object {
        const val RESULT_SUCCESS = 100
        private const val TAG = "STORY_UPLOAD"
    }
}
