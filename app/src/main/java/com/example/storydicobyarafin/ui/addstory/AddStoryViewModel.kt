package com.example.storydicobyarafin.ui.addstory

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storydicobyarafin.data.api.response.FileUploadResponse
import com.example.storydicobyarafin.data.repository.StoryRepository
import com.example.storydicobyarafin.utils.ResultState
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody

class AddStoryViewModel(private val storyRepository: StoryRepository) : ViewModel() {

    private val _uploadResult = MutableLiveData<ResultState<FileUploadResponse>>()
    val uploadResult: LiveData<ResultState<FileUploadResponse>> = _uploadResult

    fun uploadStory(file: MultipartBody.Part, description: RequestBody) {
        viewModelScope.launch {
            _uploadResult.value = ResultState.Loading
            _uploadResult.value = storyRepository.uploadImage(file, description)
        }
    }
}
