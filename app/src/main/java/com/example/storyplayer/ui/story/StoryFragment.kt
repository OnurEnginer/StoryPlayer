package com.example.storyplayer.ui.story

import android.annotation.SuppressLint
import android.graphics.Insets
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import android.view.View.OnTouchListener
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.storyplayer.R
import com.example.storyplayer.data.StoryGroup
import com.example.storyplayer.databinding.FragmentStoryBinding
import com.example.storyplayer.ui.main.MainViewModel
import com.example.storyplayer.util.Utils

class StoryFragment : Fragment() {

    companion object {
        private const val STORY_GROUP = "STORY_GROUP"
        private const val TOUCH_THRESHOLD = 500L
        fun newInstance(sg: StoryGroup) =
            StoryFragment().apply{
                arguments = Bundle().apply{
                    putParcelable(STORY_GROUP, sg)
            }
        }
    }

    private lateinit var storyViewModel: StoryViewModel
    private lateinit var mainViewModel: MainViewModel
    private lateinit var player: ExoPlayer
    private var _binding: FragmentStoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        storyViewModel = ViewModelProvider(this)[StoryViewModel::class.java]
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        binding.story.setOnTouchListener(StoryTouchListener())

        storyViewModel.storyIndexLiveData.observe(viewLifecycleOwner) {
            setStory(it)
        }
        storyViewModel.storyEndedLiveData.observe(viewLifecycleOwner) {
            if (it) {
                resetTimer()
                getNextStory()
            }
        }
        storyViewModel.storyGroupLiveData.observe(viewLifecycleOwner) {
            setTimerView(it.stories.size)
        }
        storyViewModel.timeRemainingLiveData.observe(viewLifecycleOwner) {
            val index = storyViewModel.storyIndexLiveData.value!!
            val stories = storyViewModel.storyGroupLiveData.value!!.stories
            binding.pbProgressBar.progress = (((index/stories.size.toDouble())*binding.pbProgressBar.max) + (stories[index].duration-it)*(binding.pbProgressBar.max/(stories.size*stories[index].duration).toDouble())).toInt()
        }

        val storyGroup = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireArguments().getParcelable(STORY_GROUP, StoryGroup::class.java)!!
        } else {
            requireArguments().getParcelable(STORY_GROUP)!!
        }
        val index = mainViewModel.getLastSeenIndex(storyGroup.id)
        var totalTime = 0L
        for (story in storyGroup.stories)
            totalTime += story.duration
        storyViewModel.setTotalTime(totalTime)
        binding.pbProgressBar.max = totalTime.toInt()
        storyViewModel.setStoryGroup(storyGroup)
        storyViewModel.setStoryIndex(index)
    }
    private fun setStory(index: Int) {
        val storyGroup = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireArguments().getParcelable(STORY_GROUP, StoryGroup::class.java)!!
        } else {
            requireArguments().getParcelable(STORY_GROUP)!!
        }
        val story = storyGroup.stories[index]
        story.let {
            storyViewModel.setStory(it)
            if (it.isVideo) {
                if (this::player.isInitialized.not()) {
                    player = ExoPlayer.Builder(requireContext()).build()
                    binding.pvPlayer.player = player
                }
                binding.pvPlayer.visibility = View.VISIBLE
                binding.ivStory.visibility = View.GONE
                val mediaItem = MediaItem.fromUri(Uri.parse("android.resource://${requireActivity().packageName}/raw/${it.mediaId}"))
                player.setMediaItem(mediaItem)
                player.addListener(object: Player.Listener{
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        super.onPlaybackStateChanged(playbackState)
                        if (playbackState == Player.STATE_READY) {
                            storyViewModel.startStory(it)
                            updateLastSeen(storyGroup.id, story.id)
                        }
                    }
                })
                player.prepare()
                player.playWhenReady = true
            }
            else {
                binding.pvPlayer.visibility = View.GONE
                binding.ivStory.visibility = View.VISIBLE
                Glide.with(requireContext()).load(R.drawable::class.java.getField(it.mediaId).getInt(null)).addListener(
                    object: RequestListener<Drawable>{
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            storyViewModel.startStory(it)
                            updateLastSeen(storyGroup.id, story.id)
                            return false
                        }
                    }
                ).into(binding.ivStory)
            }
        }
    }

    private fun playStory() {
        val story = storyViewModel.storyLiveData.value
        if (story?.isVideo == true)
            player.play()
        storyViewModel.startStory(story)
        storyViewModel.storyGroupLiveData.value?.let { storyViewModel.storyLiveData.value?.let { it1 -> updateLastSeen(it.id, it1.id) } }
    }
    private fun resumeStory() {
        val story = storyViewModel.storyLiveData.value
        if (story?.isVideo == true)
            player.play()
        storyViewModel.resumeStory()
    }

    private fun resetStory() {
        val story = storyViewModel.storyLiveData.value
        if (story?.isVideo == true) {
            player.seekTo(0)
            player.pause()
        }
        storyViewModel.stopStory()
    }
    private fun pauseStory() {
        val story = storyViewModel.storyLiveData.value
        if (story?.isVideo == true)
            player.pause()
        storyViewModel.pauseStory()
    }
    private fun updateLastSeen(storyGroupId: String, storyId: String?) {
        mainViewModel.setLastSeenId(storyGroupId, storyId)
    }
    private fun getNextStory() {
        val storyGroup = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireArguments().getParcelable(STORY_GROUP, StoryGroup::class.java)!!
        } else {
            requireArguments().getParcelable(STORY_GROUP)!!
        }
        val index = storyViewModel.storyIndexLiveData.value
        if (index != null) {
            if (index >= storyGroup.stories.size-1) {
                updateLastSeen(storyGroup.id, null)
                mainViewModel.getNextStoryGroup()
            }
            else {
                storyViewModel.setStoryIndex(index+1)
            }
        }
    }

    private fun getPrevStory() {
        val storyGroup = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireArguments().getParcelable(STORY_GROUP, StoryGroup::class.java)!!
        } else {
            requireArguments().getParcelable(STORY_GROUP)!!
        }
        val index = mainViewModel.getLastSeenIndex(storyGroup.id)
        if (index <= 0) {
            mainViewModel.getPrevStoryGroup()
        }
        else {
            storyViewModel.setStoryIndex(index-1)
        }
    }
    private fun setTimerView(stories: Int) {
        val cs = ConstraintSet()
        val views = IntArray(stories-1)
        val weights = FloatArray(stories-1)

        for (i in 0 until stories-1) {
            val view = View.inflate(requireContext(), R.layout.story_timer_separator, null)
            view.id = View.generateViewId()
            binding.story.addView(view)
            view.layoutParams.width = Utils.dpToPx(5)
            view.layoutParams.height = binding.pbProgressBar.height
            cs.clone(binding.story)
            views[i] = view.id
            weights[i] = 1F
        }
        if (stories == 2) {
            cs.connect(views[0], ConstraintSet.START, binding.pbProgressBar.id, ConstraintSet.START)
            cs.connect(views[0], ConstraintSet.TOP, binding.pbProgressBar.id, ConstraintSet.TOP)
            cs.connect(views[0], ConstraintSet.BOTTOM, binding.pbProgressBar.id, ConstraintSet.BOTTOM)
            cs.connect(views[0], ConstraintSet.END, binding.pbProgressBar.id, ConstraintSet.END)
        }
        else if (stories > 2) {
            cs.createHorizontalChain(
                ConstraintSet.PARENT_ID,
                ConstraintSet.LEFT,
                ConstraintSet.PARENT_ID,
                ConstraintSet.RIGHT,
                views,
                weights,
                ConstraintSet.CHAIN_SPREAD
            )
            for (view in views) {
                cs.connect(view, ConstraintSet.TOP, binding.pbProgressBar.id, ConstraintSet.TOP)
            }
        }
        cs.applyTo(binding.story)
    }
    private fun resetTimer() {
        storyViewModel.stopStory()
    }
    override fun onResume() {
        super.onResume()
        playStory()
    }
    override fun onPause() {
        super.onPause()
        resetStory()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (this::player.isInitialized)
            player.release()
    }
    inner class StoryTouchListener: OnTouchListener {
        private var touchTime: Long = 0
        private var longTouch = false
        private fun getScreenWidth (): Int {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

                val windowMetrics = requireActivity().windowManager.currentWindowMetrics
                val insets: Insets = windowMetrics.windowInsets
                    .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
                windowMetrics.bounds.width() - insets.left - insets.right
            } else {
                val displayMetrics = DisplayMetrics()
                requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
                displayMetrics.widthPixels
            }
        }
        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            v?.performClick()
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    touchTime = System.currentTimeMillis()
                    pauseStory()
                }
                MotionEvent.ACTION_UP -> {
                    if (System.currentTimeMillis() - touchTime > TOUCH_THRESHOLD) {
                        resumeStory()
                        longTouch = false
                    }
                    else if (event.x/getScreenWidth() > 0.5) {
                        getNextStory()
                    } else {
                        getPrevStory()
                    }
                }
            }
            return true
        }
    }
}