package com.example.storydicobyarafin.ui.register

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.storydicobyarafin.R
import com.example.storydicobyarafin.databinding.ActivityRegisterBinding
import com.example.storydicobyarafin.ui.ViewModelFactory
import com.example.storydicobyarafin.utils.ResultState

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel by viewModels<RegisterViewModel> {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(TAG, "RegisterActivity created.")

        setupAction()
        playAnimation()
        observeViewModel()
    }

    private fun setupAction() {
        binding.btnRegister.setOnClickListener {
            val name = binding.edRegisterName.text.toString().trim()
            val email = binding.edRegisterEmail.text.toString().trim()
            val password = binding.edRegisterPassword.text.toString().trim()

            if (name.isEmpty()) {
                binding.edRegisterName.error = getString(R.string.error_empty_field)
                return@setOnClickListener
            }
            if (email.isEmpty()) {
                binding.edRegisterEmail.error = getString(R.string.error_empty_field)
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                binding.edRegisterPassword.error = getString(R.string.error_empty_field)
                return@setOnClickListener
            }
            if (password.length < 8) {
                binding.edRegisterPassword.error = getString(R.string.error_password_min_8)
                return@setOnClickListener
            }

            Log.d(TAG, "Attempting registration for email: $email, name: $name")
            viewModel.register(name, email, password)
        }

        binding.tvLoginLink.setOnClickListener {
            Log.d(TAG, "Login link clicked. Finishing RegisterActivity.")
            finish()
        }
    }

    private fun observeViewModel() {
        viewModel.registerResult.observe(this) { result ->
            when (result) {
                is ResultState.Loading -> {
                    Log.d(TAG, "Register request loading...")
                    showLoading(true)
                }
                is ResultState.Success -> {
                    showLoading(false)
                    Log.d(TAG, "Registration successful. Message: ${result.data.message}")
                    Toast.makeText(this, getString(R.string.msg_register_success), Toast.LENGTH_SHORT).show()
                    finish()
                }
                is ResultState.Error -> {
                    showLoading(false)
                    Log.e(TAG, "Registration failed with error: ${result.error}")
                    Toast.makeText(this, getString(R.string.msg_error_occurred, result.error), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun playAnimation() {
        val logo = ObjectAnimator.ofFloat(binding.ivLogo, View.ALPHA, 1f).setDuration(400)
        val title = ObjectAnimator.ofFloat(binding.tvTitle, View.ALPHA, 1f).setDuration(400)
        val subtitle = ObjectAnimator.ofFloat(binding.tvSubtitle, View.ALPHA, 1f).setDuration(400)
        val name = ObjectAnimator.ofFloat(binding.tilName, View.ALPHA, 1f).setDuration(400)
        val email = ObjectAnimator.ofFloat(binding.tilEmail, View.ALPHA, 1f).setDuration(400)
        val password = ObjectAnimator.ofFloat(binding.tilPassword, View.ALPHA, 1f).setDuration(400)
        val btnRegister = ObjectAnimator.ofFloat(binding.btnRegister, View.ALPHA, 1f).setDuration(400)
        val loginPrompt = ObjectAnimator.ofFloat(binding.llLoginPrompt, View.ALPHA, 1f).setDuration(400)

        binding.ivLogo.alpha = 0f
        binding.tvTitle.alpha = 0f
        binding.tvSubtitle.alpha = 0f
        binding.tilName.alpha = 0f
        binding.tilEmail.alpha = 0f
        binding.tilPassword.alpha = 0f
        binding.btnRegister.alpha = 0f
        binding.llLoginPrompt.alpha = 0f

        AnimatorSet().apply {
            playSequentially(logo, title, subtitle, name, email, password, btnRegister, loginPrompt)
            start()
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnRegister.isEnabled = !isLoading
    }

    companion object {
        private const val TAG = "AUTH_FLOW"
    }
}
