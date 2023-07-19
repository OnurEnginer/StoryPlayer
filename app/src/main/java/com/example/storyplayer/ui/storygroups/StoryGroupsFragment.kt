package com.example.storyplayer.ui.storygroups

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.storyplayer.databinding.FragmentStoryGroupsBinding

class StoryGroupsFragment : Fragment() {

    private var _binding: FragmentStoryGroupsBinding? = null
    private val binding get() = _binding!!
    companion object {
        fun newInstance() = StoryGroupsFragment()
    }

    private lateinit var viewModel: StoryGroupsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
         _binding = FragmentStoryGroupsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[StoryGroupsViewModel::class.java]

    }
}