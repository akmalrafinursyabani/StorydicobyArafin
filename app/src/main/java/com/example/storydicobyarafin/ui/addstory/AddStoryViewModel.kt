package com.example.storydicobyarafin.ui.addstory

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storydicobyarafin.data.api.response.FileUploadResponse
import com.example.storydicobyarafin.data.repository.StoryRepository
import com.example.storydicobyarafin.utils.ResultState
import com.example.storydicobyarafin.utils.reduceFileImage
import com.example.storydicobyarafin.utils.uriToFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class AddStoryViewModel(private val storyRepository: StoryRepository) : ViewModel() {

    private val _currentImageUri = MutableLiveData<Uri?>()
    val currentImageUri: LiveData<Uri?> = _currentImageUri

    private val _uploadResult = MutableLiveData<ResultState<FileUploadResponse>>()
    val uploadResult: LiveData<ResultState<FileUploadResponse>> = _uploadResult

    fun setCurrentImageUri(uri: Uri?) {
        _currentImageUri.value = uri
    }

    fun uploadStory(
        context: Context,
        uri: Uri,
        description: String,
        lat: Double? = null,
        lon: Double? = null
    ) {
        viewModelScope.launch {
            _uploadResult.value = ResultState.Loading
            try {
                val result = withContext(Dispatchers.IO) {
                    val imageFile = uriToFile(uri, context).reduceFileImage()
                    val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaType())
                    val multipartBody = MultipartBody.Part.createFormData(
                        "photo",
                        imageFile.name,
                        requestImageFile
                    )
                    val requestBodyDescription = description.toRequestBody("text/plain".toMediaType())
                    val requestBodyLat = lat?.toString()?.toRequestBody("text/plain".toMediaType())
                    val requestBodyLon = lon?.toString()?.toRequestBody("text/plain".toMediaType())

                    storyRepository.uploadImage(
                        multipartBody,
                        requestBodyDescription,
                        requestBodyLat,
                        requestBodyLon
                    )
                }
                _uploadResult.value = result
            } catch (e: Exception) {
                _uploadResult.value = ResultState.Error(e.message ?: "Failed to process image")
            }
        }
    }
}
