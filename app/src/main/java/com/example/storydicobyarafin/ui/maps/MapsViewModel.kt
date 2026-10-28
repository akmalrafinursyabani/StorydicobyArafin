package com.example.storydicobyarafin.ui.maps

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storydicobyarafin.data.api.response.ListStoryItem
import com.example.storydicobyarafin.data.repository.StoryRepository
import com.example.storydicobyarafin.utils.ResultState
import kotlinx.coroutines.launch

class MapsViewModel(private val storyRepository: StoryRepository) : ViewModel() {

    private val _storiesWithLocation = MutableLiveData<ResultState<List<ListStoryItem>>>()
    val storiesWithLocation: LiveData<ResultState<List<ListStoryItem>>> = _storiesWithLocation

    fun fetchStoriesWithLocation() {
        viewModelScope.launch {
            _storiesWithLocation.value = ResultState.Loading
            _storiesWithLocation.value = storyRepository.getStoriesWithLocation()
        }
    }
}
