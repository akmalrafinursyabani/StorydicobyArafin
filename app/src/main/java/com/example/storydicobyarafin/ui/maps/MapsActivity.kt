package com.example.storydicobyarafin.ui.maps

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.storydicobyarafin.R
import com.example.storydicobyarafin.data.api.response.ListStoryItem
import com.example.storydicobyarafin.databinding.ActivityMapsBinding
import com.example.storydicobyarafin.databinding.ItemInfoWindowBinding
import com.example.storydicobyarafin.ui.ViewModelFactory
import com.example.storydicobyarafin.ui.detail.DetailStoryActivity
import com.example.storydicobyarafin.utils.ResultState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapsBinding
    private lateinit var mMap: GoogleMap
    private val viewModel by viewModels<MapsViewModel> {
        ViewModelFactory.getInstance(this)
    }

    private val boundsBuilder = LatLngBounds.Builder()
    private val markerImageCache = HashMap<String, Bitmap>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(TAG, "MapsActivity created. Initializing map and fetching location stories...")

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.title_maps)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true

        mMap.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            override fun getInfoWindow(marker: Marker): View? {
                val windowBinding = ItemInfoWindowBinding.inflate(layoutInflater)
                val story = marker.tag as? ListStoryItem ?: return null

                windowBinding.tvInfoName.text = story.name
                windowBinding.tvInfoDescription.text = story.description

                val cachedBitmap = markerImageCache[story.id]
                if (cachedBitmap != null) {
                    windowBinding.ivInfoPhoto.setImageBitmap(cachedBitmap)
                } else {
                    Glide.with(applicationContext)
                        .asBitmap()
                        .load(story.photoUrl)
                        .into(object : CustomTarget<Bitmap>() {
                            override fun onResourceReady(
                                resource: Bitmap,
                                transition: Transition<in Bitmap>?
                            ) {
                                markerImageCache[story.id] = resource
                                if (marker.isInfoWindowShown) {
                                    marker.showInfoWindow()
                                }
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {}

                            override fun onLoadFailed(errorDrawable: Drawable?) {
                                Log.e(TAG, "Failed to load image for marker story: ${story.id}")
                            }
                        })
                }

                return windowBinding.root
            }

            override fun getInfoContents(marker: Marker): View? {
                return null
            }
        })

        mMap.setOnInfoWindowClickListener { marker ->
            val story = marker.tag as? ListStoryItem
            if (story != null) {
                Log.d(TAG, "InfoWindow clicked for story: ${story.id}. Launching DetailStoryActivity.")
                val intent = Intent(this, DetailStoryActivity::class.java).apply {
                    putExtra(DetailStoryActivity.EXTRA_NAME, story.name)
                    putExtra(DetailStoryActivity.EXTRA_DESCRIPTION, story.description)
                    putExtra(DetailStoryActivity.EXTRA_PHOTO_URL, story.photoUrl)
                    putExtra(DetailStoryActivity.EXTRA_CREATED_AT, story.createdAt)
                    story.lat?.let { putExtra(DetailStoryActivity.EXTRA_LAT, it) }
                    story.lon?.let { putExtra(DetailStoryActivity.EXTRA_LON, it) }
                }
                startActivity(intent)
            }
        }

        observeViewModel()
        viewModel.fetchStoriesWithLocation()
    }

    private fun observeViewModel() {
        viewModel.storiesWithLocation.observe(this) { result ->
            when (result) {
                is ResultState.Loading -> {
                    showLoading(true)
                }
                is ResultState.Success -> {
                    showLoading(false)
                    val stories = result.data
                    Log.d(TAG, "Fetched ${stories.size} stories with location.")
                    if (stories.isNotEmpty()) {
                        var hasLocation = false
                        stories.forEach { story ->
                            val lat = story.lat
                            val lon = story.lon
                            if (lat != null && lon != null) {
                                val latLng = LatLng(lat, lon)
                                val marker = mMap.addMarker(
                                    MarkerOptions()
                                        .position(latLng)
                                        .title(story.name)
                                        .snippet(story.description)
                                )
                                marker?.tag = story
                                boundsBuilder.include(latLng)
                                hasLocation = true

                                Glide.with(applicationContext)
                                    .asBitmap()
                                    .load(story.photoUrl)
                                    .into(object : CustomTarget<Bitmap>() {
                                        override fun onResourceReady(
                                            resource: Bitmap,
                                            transition: Transition<in Bitmap>?
                                        ) {
                                            markerImageCache[story.id] = resource
                                        }

                                        override fun onLoadCleared(placeholder: Drawable?) {}
                                    })
                            }
                        }

                        if (hasLocation) {
                            try {
                                val bounds: LatLngBounds = boundsBuilder.build()
                                mMap.animateCamera(
                                    CameraUpdateFactory.newLatLngBounds(
                                        bounds,
                                        resources.displayMetrics.widthPixels,
                                        resources.displayMetrics.heightPixels,
                                        300
                                    )
                                )
                            } catch (e: Exception) {
                                Log.e(TAG, "Failed to fit map bounds", e)
                            }
                        }
                    } else {
                        Toast.makeText(this, getString(R.string.msg_no_stories), Toast.LENGTH_SHORT).show()
                    }
                }
                is ResultState.Error -> {
                    showLoading(false)
                    Log.e(TAG, "Failed to fetch location stories: ${result.error}")
                    Toast.makeText(this, getString(R.string.msg_error_occurred, result.error), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    companion object {
        private const val TAG = "MAPS_FLOW"
    }
}
