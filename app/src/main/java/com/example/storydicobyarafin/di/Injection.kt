package com.example.storydicobyarafin.di

import android.content.Context
import com.example.storydicobyarafin.data.api.ApiConfig
import com.example.storydicobyarafin.data.pref.UserPreference
import com.example.storydicobyarafin.data.pref.dataStore
import com.example.storydicobyarafin.data.repository.StoryRepository
import com.example.storydicobyarafin.data.repository.UserRepository

object Injection {

    fun provideUserRepository(context: Context): UserRepository {
        val pref = UserPreference.getInstance(context.dataStore)
        val apiService = ApiConfig.getApiService(pref)
        return UserRepository.getInstance(pref, apiService)
    }

    fun provideStoryRepository(context: Context): StoryRepository {
        val pref = UserPreference.getInstance(context.dataStore)
        val apiService = ApiConfig.getApiService(pref)
        return StoryRepository.getInstance(apiService)
    }
}
