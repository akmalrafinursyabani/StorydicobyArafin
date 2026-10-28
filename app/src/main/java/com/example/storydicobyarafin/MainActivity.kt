package com.example.storydicobyarafin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.storydicobyarafin.databinding.ActivityMainBinding
import com.example.storydicobyarafin.ui.ViewModelFactory
import com.example.storydicobyarafin.ui.addstory.AddStoryActivity
import com.example.storydicobyarafin.ui.login.LoginActivity
import com.example.storydicobyarafin.ui.main.MainViewModel
import com.example.storydicobyarafin.ui.main.StoryListFragment
import com.example.storydicobyarafin.ui.maps.MapsActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstance(this)
    }

    private val addStoryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AddStoryActivity.RESULT_SUCCESS) {
            Log.d(TAG, "New story added successfully. Refreshing story list fragment.")
            val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
            if (fragment is StoryListFragment) {
                fragment.refreshList()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(TAG, "MainActivity created. Initializing views and observing session...")

        setupToolbar()
        observeSession()

        if (savedInstanceState == null) {
            Log.d(TAG, "Attaching StoryListFragment dynamically into fragment_container...")
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, StoryListFragment())
                .commit()
        }

        binding.fabAddStory.setOnClickListener {
            Log.d(TAG, "FAB clicked. Launching AddStoryActivity.")
            val intent = Intent(this, AddStoryActivity::class.java)
            addStoryLauncher.launch(intent)
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_maps -> {
                    Log.d(TAG, "Maps action item clicked. Launching MapsActivity...")
                    startActivity(Intent(this, MapsActivity::class.java))
                    true
                }
                R.id.action_logout -> {
                    Log.d(TAG, "Logout menu item clicked on Toolbar. Displaying confirmation dialog...")
                    showLogoutConfirmationDialog()
                    true
                }
                else -> false
            }
        }
    }

    private fun observeSession() {
        viewModel.getSession().observe(this) { user ->
            Log.d(TAG, "Session state updated: isLogin=${user.isLogin}, email=${user.email}")
            if (!user.isLogin || user.token.isEmpty()) {
                Log.d(TAG, "User is not logged in or token is empty. Redirecting to LoginActivity...")
                val intent = Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finish()
            }
        }
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.title_logout))
            .setMessage(getString(R.string.msg_logout_confirm))
            .setPositiveButton(getString(R.string.btn_yes)) { _, _ ->
                Log.d(TAG, "Logout confirmed by user. Clearing session data and resetting ViewModelFactory...")
                viewModel.logout()
                ViewModelFactory.clearInstance()
            }
            .setNegativeButton(getString(R.string.btn_cancel)) { dialog, _ ->
                Log.d(TAG, "Logout cancelled by user.")
                dialog.dismiss()
            }
            .show()
    }

    companion object {
        private const val TAG = "LOGOUT_FLOW"
    }
}
