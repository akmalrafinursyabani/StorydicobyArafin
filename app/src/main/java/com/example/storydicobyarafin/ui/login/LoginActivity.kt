package com.example.storydicobyarafin.ui.login

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.storydicobyarafin.MainActivity
import com.example.storydicobyarafin.R
import com.example.storydicobyarafin.databinding.ActivityLoginBinding
import com.example.storydicobyarafin.ui.ViewModelFactory
import com.example.storydicobyarafin.ui.register.RegisterActivity
import com.example.storydicobyarafin.utils.ResultState

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel by viewModels<LoginViewModel> {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(TAG, "LoginActivity created.")

        setupAction()
        playAnimation()
        observeViewModel()
    }

    private fun setupAction() {
        binding.btnLogin.setOnClickListener {
            val email = binding.edLoginEmail.text.toString().trim()
            val password = binding.edLoginPassword.text.toString().trim()

            if (email.isEmpty()) {
                binding.edLoginEmail.error = getString(R.string.error_empty_field)
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                binding.edLoginPassword.error = getString(R.string.error_empty_field)
                return@setOnClickListener
            }
            if (password.length < 8) {
                binding.edLoginPassword.error = getString(R.string.error_password_min_8)
                return@setOnClickListener
            }

            Log.d(TAG, "Attempting login for email: $email")
            viewModel.login(email, password)
        }

        binding.tvRegisterLink.setOnClickListener {
            Log.d(TAG, "Navigating to RegisterActivity.")
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun observeViewModel() {
        viewModel.loginResult.observe(this) { result ->
            when (result) {
                is ResultState.Loading -> {
                    Log.d(TAG, "Login request loading...")
                    showLoading(true)
                }
                is ResultState.Success -> {
                    showLoading(false)
                    val loginResult = result.data.loginResult
                    if (loginResult != null) {
                        Log.d(TAG, "Login successful and session persisted. Navigating to MainActivity...")
                        ViewModelFactory.clearInstance()
                        val intent = Intent(this, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(intent)
                        finish()
                    } else {
                        Log.e(TAG, "Login response received but loginResult is null")
                    }
                }
                is ResultState.Error -> {
                    showLoading(false)
                    Log.e(TAG, "Login failed with error: ${result.error}")
                    Toast.makeText(this, getString(R.string.msg_error_occurred, result.error), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun playAnimation() {
        val logo = ObjectAnimator.ofFloat(binding.ivLogo, View.ALPHA, 1f).setDuration(400)
        val title = ObjectAnimator.ofFloat(binding.tvTitle, View.ALPHA, 1f).setDuration(400)
        val subtitle = ObjectAnimator.ofFloat(binding.tvSubtitle, View.ALPHA, 1f).setDuration(400)
        val email = ObjectAnimator.ofFloat(binding.tilEmail, View.ALPHA, 1f).setDuration(400)
        val password = ObjectAnimator.ofFloat(binding.tilPassword, View.ALPHA, 1f).setDuration(400)
        val btnLogin = ObjectAnimator.ofFloat(binding.btnLogin, View.ALPHA, 1f).setDuration(400)
        val registerPrompt = ObjectAnimator.ofFloat(binding.llRegisterPrompt, View.ALPHA, 1f).setDuration(400)

        binding.ivLogo.alpha = 0f
        binding.tvTitle.alpha = 0f
        binding.tvSubtitle.alpha = 0f
        binding.tilEmail.alpha = 0f
        binding.tilPassword.alpha = 0f
        binding.btnLogin.alpha = 0f
        binding.llRegisterPrompt.alpha = 0f

        AnimatorSet().apply {
            playSequentially(logo, title, subtitle, email, password, btnLogin, registerPrompt)
            start()
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !isLoading
    }

    companion object {
        private const val TAG = "AUTH_FLOW"
    }
}
