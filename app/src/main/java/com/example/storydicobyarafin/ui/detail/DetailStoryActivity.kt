package com.example.storydicobyarafin.ui.detail

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.storydicobyarafin.R
import com.example.storydicobyarafin.databinding.ActivityDetailStoryBinding
import com.example.storydicobyarafin.utils.formatDate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class DetailStoryActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityDetailStoryBinding
    private var storyLat: Double? = null
    private var storyLon: Double? = null
    private var storyName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        storyName = intent.getStringExtra(EXTRA_NAME) ?: ""
        val description = intent.getStringExtra(EXTRA_DESCRIPTION) ?: ""
        val photoUrl = intent.getStringExtra(EXTRA_PHOTO_URL) ?: ""
        val createdAt = intent.getStringExtra(EXTRA_CREATED_AT) ?: ""

        if (intent.hasExtra(EXTRA_LAT) && intent.hasExtra(EXTRA_LON)) {
            storyLat = intent.getDoubleExtra(EXTRA_LAT, DEFAULT_INVALID_LAT_LON)
            storyLon = intent.getDoubleExtra(EXTRA_LON, DEFAULT_INVALID_LAT_LON)
            if (storyLat == DEFAULT_INVALID_LAT_LON || storyLon == DEFAULT_INVALID_LAT_LON) {
                storyLat = null
                storyLon = null
            }
        }

        binding.tvDetailName.text = storyName
        binding.tvDetailDescription.text = description
        binding.tvDetailDate.text = createdAt.formatDate()

        Glide.with(this)
            .load(photoUrl)
            .into(binding.ivDetailPhoto)

        setupMapContainer()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupMapContainer() {
        val lat = storyLat
        val lon = storyLon
        if (lat != null && lon != null) {
            binding.cardMapContainer.visibility = View.VISIBLE
            binding.cardNoLocation.visibility = View.GONE

            binding.cardMapContainer.setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                        binding.scrollView.requestDisallowInterceptTouchEvent(true)
                        false
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        binding.scrollView.requestDisallowInterceptTouchEvent(false)
                        true
                    }
                    else -> false
                }
            }

            val mapFragment = supportFragmentManager
                .findFragmentById(R.id.detail_map) as? SupportMapFragment
            mapFragment?.getMapAsync(this)
        } else {
            binding.cardMapContainer.visibility = View.GONE
            binding.cardNoLocation.visibility = View.VISIBLE
            binding.tvNoLocationMsg.text = getString(R.string.msg_location_not_found)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val lat = storyLat
        val lon = storyLon
        if (lat != null && lon != null) {
            try {
                val location = LatLng(lat, lon)
                googleMap.addMarker(
                    MarkerOptions()
                        .position(location)
                        .title(storyName)
                )
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
            } catch (e: Exception) {
                Log.e(TAG, "Error displaying detail map pinpoint", e)
                binding.cardMapContainer.visibility = View.GONE
                binding.cardNoLocation.visibility = View.VISIBLE
                binding.tvNoLocationMsg.text = getString(R.string.msg_error_occurred, e.localizedMessage)
            }
        }
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
        const val EXTRA_LAT = "extra_lat"
        const val EXTRA_LON = "extra_lon"
        private const val DEFAULT_INVALID_LAT_LON = -999.0
        private const val TAG = "DETAIL_STORY_MAP"
    }
}
