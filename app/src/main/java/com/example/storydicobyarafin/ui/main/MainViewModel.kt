package com.example.storydicobyarafin.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.storydicobyarafin.data.api.response.ListStoryItem
import com.example.storydicobyarafin.data.pref.UserModel
import com.example.storydicobyarafin.data.repository.StoryRepository
import com.example.storydicobyarafin.data.repository.UserRepository
import com.example.storydicobyarafin.utils.ResultState
import kotlinx.coroutines.launch

class MainViewModel(
    private val storyRepository: StoryRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _storiesResult = MutableLiveData<ResultState<List<ListStoryItem>>>()
    val storiesResult: LiveData<ResultState<List<ListStoryItem>>> = _storiesResult

    fun getSession(): LiveData<UserModel> {
        return userRepository.getSession().asLiveData()
    }

    fun fetchStories() {
        viewModelScope.launch {
            _storiesResult.value = ResultState.Loading
            _storiesResult.value = storyRepository.getStories()
        }
    }

    fun logout() {
        viewModelScope.launch {
            userRepository.logout()
        }
    }
}
