package com.example.storydicobyarafin.ui.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storydicobyarafin.data.api.response.RegisterResponse
import com.example.storydicobyarafin.data.repository.UserRepository
import com.example.storydicobyarafin.utils.ResultState
import kotlinx.coroutines.launch

class RegisterViewModel(private val repository: UserRepository) : ViewModel() {

    private val _registerResult = MutableLiveData<ResultState<RegisterResponse>>()
    val registerResult: LiveData<ResultState<RegisterResponse>> = _registerResult

    fun register(name: String, email: String, pass: String) {
        viewModelScope.launch {
            _registerResult.value = ResultState.Loading
            _registerResult.value = repository.register(name, email, pass)
        }
    }
}
