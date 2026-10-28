package com.example.storydicobyarafin.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.storydicobyarafin.databinding.FragmentStoryListBinding
import com.example.storydicobyarafin.ui.ViewModelFactory
import com.example.storydicobyarafin.ui.adapter.LoadingStateAdapter
import com.example.storydicobyarafin.ui.adapter.StoryAdapter
import com.example.storydicobyarafin.ui.detail.DetailStoryActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

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

        Log.d(TAG, "StoryListFragment onViewCreated. Initializing Paging 3 Adapter...")

        setupRecyclerView()
        setupSwipeRefresh()
        observePagingData()
    }

    fun refreshList() {
        if (::storyAdapter.isInitialized) {
            storyAdapter.refresh()
        }
    }

    private fun setupRecyclerView() {
        storyAdapter = StoryAdapter { storyItem, ivPhoto, tvName ->
            Log.d(TAG, "Story item clicked: id=${storyItem.id}, name=${storyItem.name}. Opening DetailStoryActivity...")
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

        binding.btnRetry.setOnClickListener {
            storyAdapter.retry()
        }

        binding.rvStories.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = storyAdapter.withLoadStateFooter(
                footer = LoadingStateAdapter { storyAdapter.retry() }
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            storyAdapter.loadStateFlow.collectLatest { loadStates ->
                val isRefreshing = loadStates.refresh is LoadState.Loading
                val isError = loadStates.refresh is LoadState.Error
                val isEmpty = loadStates.refresh is LoadState.NotLoading && storyAdapter.itemCount == 0

                binding.progressBar.isVisible = isRefreshing && !binding.swipeRefresh.isRefreshing
                binding.llError.isVisible = isError
                binding.tvEmpty.isVisible = isEmpty
                binding.rvStories.isVisible = !isError && !isEmpty

                if (loadStates.refresh is LoadState.Error) {
                    val errorState = loadStates.refresh as LoadState.Error
                    binding.tvError.text = errorState.error.localizedMessage ?: "Failed to load stories"
                    Log.e(TAG, "Paging LoadState Error: ${errorState.error.message}")
                }

                if (loadStates.refresh !is LoadState.Loading) {
                    binding.swipeRefresh.isRefreshing = false
                }
            }
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            Log.d(TAG, "Swipe to refresh triggered. Refreshing adapter...")
            storyAdapter.refresh()
        }
    }

    private fun observePagingData() {
        viewModel.storiesPaging.observe(viewLifecycleOwner) { pagingData ->
            storyAdapter.submitData(lifecycle, pagingData)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "STORY_FETCH"
    }
}
