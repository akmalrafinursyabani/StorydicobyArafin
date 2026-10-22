package com.example.storydicobyarafin.ui.detail

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.storydicobyarafin.R
import com.example.storydicobyarafin.databinding.ActivityDetailStoryBinding
import com.example.storydicobyarafin.utils.formatDate

class DetailStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailStoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.title_detail_story)

        val name = intent.getStringExtra(EXTRA_NAME) ?: ""
        val description = intent.getStringExtra(EXTRA_DESCRIPTION) ?: ""
        val photoUrl = intent.getStringExtra(EXTRA_PHOTO_URL) ?: ""
        val createdAt = intent.getStringExtra(EXTRA_CREATED_AT) ?: ""

        binding.tvDetailName.text = name
        binding.tvDetailDescription.text = description
        binding.tvDetailDate.text = createdAt.formatDate()

        Glide.with(this)
            .load(photoUrl)
            .into(binding.ivDetailPhoto)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    companion object {
        const val EXTRA_NAME = "extra_name"
        const val EXTRA_DESCRIPTION = "extra_description"
        const val EXTRA_PHOTO_URL = "extra_photo_url"
        const val EXTRA_CREATED_AT = "extra_created_at"
    }
}
