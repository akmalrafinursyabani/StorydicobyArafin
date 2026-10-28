package com.example.storydicobyarafin.data.repository

import com.example.storydicobyarafin.data.api.ApiService
import com.example.storydicobyarafin.data.api.response.LoginResponse
import com.example.storydicobyarafin.data.api.response.RegisterResponse
import com.example.storydicobyarafin.data.pref.UserModel
import com.example.storydicobyarafin.data.pref.UserPreference
import com.example.storydicobyarafin.utils.ResultState
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException

open class UserRepository protected constructor(
    private val userPreference: UserPreference,
    private val apiService: ApiService
) {

    open suspend fun saveSession(user: UserModel) {
        userPreference.saveSession(user)
    }

    open fun getSession(): Flow<UserModel> {
        return userPreference.getSession()
    }

    open suspend fun logout() {
        userPreference.logout()
    }

    open suspend fun register(name: String, email: String, pass: String): ResultState<RegisterResponse> {
        return try {
            val response = apiService.register(name, email, pass)
            ResultState.Success(response)
        } catch (e: HttpException) {
            val jsonInString = e.response()?.errorBody()?.string()
            val errorBody = Gson().fromJson(jsonInString, RegisterResponse::class.java)
            ResultState.Error(errorBody?.message ?: e.message())
        } catch (e: Exception) {
            ResultState.Error(e.message ?: "An unexpected error occurred")
        }
    }

    open suspend fun login(email: String, pass: String): ResultState<LoginResponse> {
        return try {
            val response = apiService.login(email, pass)
            ResultState.Success(response)
        } catch (e: HttpException) {
            val jsonInString = e.response()?.errorBody()?.string()
            val errorBody = Gson().fromJson(jsonInString, LoginResponse::class.java)
            ResultState.Error(errorBody?.message ?: e.message())
        } catch (e: Exception) {
            ResultState.Error(e.message ?: "An unexpected error occurred")
        }
    }

    companion object {
        @Volatile
        private var instance: UserRepository? = null

        fun getInstance(userPreference: UserPreference, apiService: ApiService): UserRepository =
            instance ?: synchronized(this) {
                instance ?: UserRepository(userPreference, apiService).also { instance = it }
            }
    }
}
