package com.example.storydicobyarafin.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import androidx.paging.map
import com.example.storydicobyarafin.data.api.ApiService
import com.example.storydicobyarafin.data.api.response.FileUploadResponse
import com.example.storydicobyarafin.data.api.response.ListStoryItem
import com.example.storydicobyarafin.data.api.response.StoryResponse
import com.example.storydicobyarafin.data.database.StoryDatabase
import com.example.storydicobyarafin.utils.ResultState
import com.google.gson.Gson
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException

open class StoryRepository protected constructor(
    private val database: StoryDatabase,
    private val apiService: ApiService
) {

    @OptIn(ExperimentalPagingApi::class)
    open fun getStoriesPaging(): LiveData<PagingData<ListStoryItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = 7,
                initialLoadSize = 7
            ),
            remoteMediator = StoryRemoteMediator(database, apiService),
            pagingSourceFactory = {
                database.storyDao().getAllStories()
            }
        ).liveData.map { pagingData ->
            pagingData.map { entity ->
                ListStoryItem(
                    id = entity.id,
                    name = entity.name,
                    description = entity.description,
                    photoUrl = entity.photoUrl,
                    createdAt = entity.createdAt,
                    lat = entity.lat,
                    lon = entity.lon
                )
            }
        }
    }

    open suspend fun getStoriesWithLocation(): ResultState<List<ListStoryItem>> {
        return try {
            val response = apiService.getStories(location = 1)
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

    open suspend fun uploadImage(
        imageFile: MultipartBody.Part,
        description: RequestBody,
        lat: RequestBody? = null,
        lon: RequestBody? = null
    ): ResultState<FileUploadResponse> {
        return try {
            val response = apiService.uploadImage(imageFile, description, lat, lon)
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

        fun getInstance(database: StoryDatabase, apiService: ApiService): StoryRepository =
            instance ?: synchronized(this) {
                instance ?: StoryRepository(database, apiService).also { instance = it }
            }
    }
}
