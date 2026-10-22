package com.example.storydicobyarafin.data.repository

import com.example.storydicobyarafin.data.api.ApiService
import com.example.storydicobyarafin.data.api.response.FileUploadResponse
import com.example.storydicobyarafin.data.api.response.ListStoryItem
import com.example.storydicobyarafin.data.api.response.StoryResponse
import com.example.storydicobyarafin.utils.ResultState
import com.google.gson.Gson
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException

class StoryRepository private constructor(
    private val apiService: ApiService
) {

    suspend fun getStories(): ResultState<List<ListStoryItem>> {
        return try {
            val response = apiService.getStories()
            if (!response.error) {
                ResultState.Success(response.listStory)
            } else {
                ResultState.Error(response.message)
            }
        } catch (e: HttpException) {
            val jsonInString = e.response()?.errorBody()?.string()
            val errorBody = Gson().fromJson(jsonInString, StoryResponse::class.java)
            ResultState.Error(errorBody?.message ?: e.message())
        } catch (e: Exception) {
            ResultState.Error(e.message ?: "An unexpected error occurred")
        }
    }

    suspend fun uploadImage(
        imageFile: MultipartBody.Part,
        description: RequestBody
    ): ResultState<FileUploadResponse> {
        return try {
            val response = apiService.uploadImage(imageFile, description)
            if (!response.error) {
                ResultState.Success(response)
            } else {
                ResultState.Error(response.message)
            }
        } catch (e: HttpException) {
            val jsonInString = e.response()?.errorBody()?.string()
            val errorBody = Gson().fromJson(jsonInString, FileUploadResponse::class.java)
            ResultState.Error(errorBody?.message ?: e.message())
        } catch (e: Exception) {
            ResultState.Error(e.message ?: "An unexpected error occurred")
        }
    }

    companion object {
        @Volatile
        private var instance: StoryRepository? = null

        fun getInstance(apiService: ApiService): StoryRepository =
            instance ?: synchronized(this) {
                instance ?: StoryRepository(apiService).also { instance = it }
            }
    }
}
