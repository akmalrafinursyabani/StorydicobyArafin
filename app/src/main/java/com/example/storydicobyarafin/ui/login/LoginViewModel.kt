package com.example.storydicobyarafin.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storydicobyarafin.data.api.response.LoginResponse
import com.example.storydicobyarafin.data.pref.UserModel
import com.example.storydicobyarafin.data.repository.UserRepository
import com.example.storydicobyarafin.utils.ResultState
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: UserRepository) : ViewModel() {

    private val _loginResult = MutableLiveData<ResultState<LoginResponse>>()
    val loginResult: LiveData<ResultState<LoginResponse>> = _loginResult

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _loginResult.value = ResultState.Loading
            _loginResult.value = repository.login(email, pass)
        }
    }

    fun saveSession(userModel: UserModel) {
        viewModelScope.launch {
            repository.saveSession(userModel)
        }
    }
}
