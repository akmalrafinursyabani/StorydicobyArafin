package com.example.storydicobyarafin.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.storydicobyarafin.data.api.response.ListStoryItem
import com.example.storydicobyarafin.data.pref.UserModel
import com.example.storydicobyarafin.data.repository.StoryRepository
import com.example.storydicobyarafin.data.repository.UserRepository
import kotlinx.coroutines.launch

class MainViewModel(
    storyRepository: StoryRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    val storiesPaging: LiveData<PagingData<ListStoryItem>> =
        storyRepository.getStoriesPaging().cachedIn(viewModelScope)

    fun getSession(): LiveData<UserModel> {
        return userRepository.getSession().asLiveData()
    }

    fun logout() {
        viewModelScope.launch {
            userRepository.logout()
        }
    }
}
