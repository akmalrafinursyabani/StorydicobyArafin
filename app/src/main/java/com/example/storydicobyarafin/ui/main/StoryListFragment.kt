package com.example.storydicobyarafin.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.storydicobyarafin.R
import com.example.storydicobyarafin.databinding.FragmentStoryListBinding
import com.example.storydicobyarafin.ui.ViewModelFactory
import com.example.storydicobyarafin.ui.adapter.StoryAdapter
import com.example.storydicobyarafin.ui.detail.DetailStoryActivity
import com.example.storydicobyarafin.utils.ResultState

class StoryListFragment : Fragment() {

    private var _binding: FragmentStoryListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels {
        ViewModelFactory.getInstance(requireContext())
    }

    private lateinit var storyAdapter: StoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStoryListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "StoryListFragment onViewCreated. Initializing RecyclerView and fetching stories...")

        setupRecyclerView()
        setupSwipeRefresh()
        observeViewModel()

        viewModel.fetchStories()
    }

    private fun setupRecyclerView() {
        storyAdapter = StoryAdapter { storyItem, ivPhoto, tvName ->
            Log.d(TAG, "Story item clicked: id=${storyItem.id}, name=${storyItem.name}. Launching DetailStoryActivity with Shared Element transition...")
            val intent = Intent(requireContext(), DetailStoryActivity::class.java).apply {
                putExtra(DetailStoryActivity.EXTRA_NAME, storyItem.name)
                putExtra(DetailStoryActivity.EXTRA_DESCRIPTION, storyItem.description)
                putExtra(DetailStoryActivity.EXTRA_PHOTO_URL, storyItem.photoUrl)
                putExtra(DetailStoryActivity.EXTRA_CREATED_AT, storyItem.createdAt)
            }

            val optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(
                requireActivity(),
                Pair(ivPhoto, "photo"),
                Pair(tvName, "name")
            )

            startActivity(intent, optionsCompat.toBundle())
        }

        binding.rvStories.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = storyAdapter
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            Log.d(TAG, "Swipe to refresh triggered. Refetching stories...")
            viewModel.fetchStories()
        }
    }

    private fun observeViewModel() {
        viewModel.storiesResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is ResultState.Loading -> {
                    Log.d(TAG, "Fetching story list loading...")
                    if (!binding.swipeRefresh.isRefreshing) {
                        showLoading(true)
                    }
                }
                is ResultState.Success -> {
                    showLoading(false)
                    binding.swipeRefresh.isRefreshing = false
                    val stories = result.data
                    Log.d(TAG, "Successfully fetched ${stories.size} stories.")
                    storyAdapter.submitList(stories)
                    binding.tvEmpty.visibility = if (stories.isEmpty()) View.VISIBLE else View.GONE
                }
                is ResultState.Error -> {
                    showLoading(false)
                    binding.swipeRefresh.isRefreshing = false
                    Log.e(TAG, "Failed to fetch stories: ${result.error}")
                    Toast.makeText(requireContext(), getString(R.string.msg_error_occurred, result.error), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "STORY_FETCH"
    }
}
