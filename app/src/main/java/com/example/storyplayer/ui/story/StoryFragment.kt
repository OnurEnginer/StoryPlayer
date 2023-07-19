package com.example.storyplayer.ui.story

import android.os.Build
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.storyplayer.data.Story
import com.example.storyplayer.databinding.FragmentStoryBinding
import com.example.storyplayer.ui.main.MainViewModel

class StoryFragment : Fragment() {

    companion object {
        fun newInstance() = StoryFragment()
    }

    private lateinit var storyViewModel: StoryViewModel
    private lateinit var mainViewModel: MainViewModel

    private var _binding: FragmentStoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        storyViewModel = ViewModelProvider(this)[StoryViewModel::class.java]
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        storyViewModel.storyLiveData.observe(viewLifecycleOwner) {
            setStory(it)
            storyViewModel.startStory(it)
        }
        storyViewModel.storyEndedLiveData.observe(viewLifecycleOwner) {
            if (it) {
                getNextStory()
            }
        }
        storyViewModel.timeRemainingLiveData.observe(viewLifecycleOwner) {
            binding.pbProgressBar.progress = binding.pbProgressBar.max-it.toInt()
        }
        mainViewModel.storyGroupLiveData.observe(viewLifecycleOwner) {
            storyViewModel.storyLiveData.postValue(mainViewModel.getNextStory(it.id))
        }
    }
    private fun setStory(story: Story?) {
        story?.let {
            Glide.with(requireContext()).load(it.mediaId).into(binding.ivStory)
            binding.pbProgressBar.max = it.duration.toInt()
        }
        if (story == null)
            requireActivity().onBackPressedDispatcher.onBackPressed()
    }
    private fun getNextStory() {
        val nextStory = mainViewModel.storyGroupLiveData.value?.let { mainViewModel.getNextStory(it.id) }
        if (nextStory == null) requireActivity().onBackPressedDispatcher.onBackPressed() else setStory(nextStory)
    }
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onResume() {
        super.onResume()
        //storyViewModel.timeRemainingLiveData.value?.let {
            //storyViewModel.startTimer(it)
        //}
        (requireActivity() as AppCompatActivity).apply {
            supportActionBar?.hide()
            window.insetsController?.apply {
                hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onPause() {
        super.onPause()
        //storyViewModel.stopTimer()
        (requireActivity() as AppCompatActivity).apply {
            supportActionBar?.show()
            window.insetsController?.apply {
                show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }
}