package com.example.storydicobyarafin

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.storydicobyarafin.databinding.ActivityMainBinding
import com.example.storydicobyarafin.ui.ViewModelFactory
import com.example.storydicobyarafin.ui.addstory.AddStoryActivity
import com.example.storydicobyarafin.ui.login.LoginActivity
import com.example.storydicobyarafin.ui.main.MainViewModel
import com.example.storydicobyarafin.ui.main.StoryListFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstance(this)
    }

    private val addStoryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AddStoryActivity.RESULT_SUCCESS) {
            viewModel.fetchStories()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        observeSession()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, StoryListFragment())
                .commit()
        }

        binding.fabAddStory.setOnClickListener {
            val intent = Intent(this, AddStoryActivity::class.java)
            addStoryLauncher.launch(intent)
        }
    }

    private fun observeSession() {
        viewModel.getSession().observe(this) { user ->
            if (!user.isLogin || user.token.isEmpty()) {
                val intent = Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finish()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                viewModel.logout()
                ViewModelFactory.clearInstance()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
