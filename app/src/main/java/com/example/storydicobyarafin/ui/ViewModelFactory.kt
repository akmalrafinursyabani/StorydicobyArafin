package com.example.storydicobyarafin.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.storydicobyarafin.data.repository.StoryRepository
import com.example.storydicobyarafin.data.repository.UserRepository
import com.example.storydicobyarafin.di.Injection
import com.example.storydicobyarafin.ui.addstory.AddStoryViewModel
import com.example.storydicobyarafin.ui.login.LoginViewModel
import com.example.storydicobyarafin.ui.main.MainViewModel
import com.example.storydicobyarafin.ui.register.RegisterViewModel

class ViewModelFactory private constructor(
    private val userRepository: UserRepository,
    private val storyRepository: StoryRepository
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(userRepository) as T
            }
            modelClass.isAssignableFrom(RegisterViewModel::class.java) -> {
                RegisterViewModel(userRepository) as T
            }
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                MainViewModel(storyRepository, userRepository) as T
            }
            modelClass.isAssignableFrom(AddStoryViewModel::class.java) -> {
                AddStoryViewModel(storyRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: ViewModelFactory? = null

        @JvmStatic
        fun getInstance(context: Context): ViewModelFactory {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ViewModelFactory(
                    Injection.provideUserRepository(context),
                    Injection.provideStoryRepository(context)
                ).also { INSTANCE = it }
            }
        }

        fun clearInstance() {
            INSTANCE = null
        }
    }
}
