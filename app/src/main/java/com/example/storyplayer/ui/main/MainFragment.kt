package com.example.storyplayer.ui.main

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.storyplayer.R
import com.example.storyplayer.databinding.FragmentMainBinding
import com.example.storyplayer.ui.storygroups.StoryGroupsFragment
import com.example.storyplayer.ui.storyoverview.StoryOverviewAdapter

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel
    private lateinit var storyOverviewAdapter: StoryOverviewAdapter

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val list = viewModel.getStories()
        parentFragmentManager.addOnBackStackChangedListener {
            if (isStoryViewAdded()) {
                binding.rvStoryGroups.visibility = View.GONE
                binding.main.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.black))
            }
            else {
                binding.rvStoryGroups.visibility = View.VISIBLE
                binding.main.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            }
        }
        viewModel.storyGroupIndexLiveData.observe(viewLifecycleOwner) {
            if (it != -1 && isStoryViewAdded().not()) {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.story_container, StoryGroupsFragment.newInstance())
                    .addToBackStack(StoryGroupsFragment::class.java.simpleName)
                    .commit()
            }
        }
        binding.rvStoryGroups.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        storyOverviewAdapter = StoryOverviewAdapter(list) {
             viewModel.onStoryGroupClicked(it)
        }
        binding.rvStoryGroups.adapter = storyOverviewAdapter
    }
    private fun isStoryViewAdded(): Boolean {
        return requireActivity().supportFragmentManager
            .fragments
            .filterIsInstance<StoryGroupsFragment>()
            .isNotEmpty()
    }
}