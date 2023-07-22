package com.example.storyplayer.ui.story

import android.os.CountDownTimer
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.storyplayer.data.Story
import com.example.storyplayer.data.StoryGroup
import kotlin.properties.Delegates

class StoryViewModel : ViewModel() {
    val storyLiveData = MutableLiveData<Story>()
    val storyIndexLiveData = MutableLiveData<Int>()
    val storyGroupLiveData = MutableLiveData<StoryGroup>()
    val storyEndedLiveData = MutableLiveData<Boolean>()
    val timeRemainingLiveData = MutableLiveData<Long>()
    private var totalTime = 0L
    private lateinit var timer: CountDownTimer

    fun setTotalTime(time: Long) {
        totalTime = time
    }
    fun setStoryGroup(storyGroup: StoryGroup) {
        storyGroupLiveData.postValue(storyGroup)
    }
    fun setStoryIndex(index: Int) {
        storyIndexLiveData.postValue(index)
    }
    fun setStory(story: Story) {
        storyLiveData.postValue(story)
        timeRemainingLiveData.postValue(story.duration)
    }
    fun setIsEnded(isEnded: Boolean) {
        storyEndedLiveData.postValue(isEnded)
    }
    fun startStory(story: Story?) {
        story?.let {
            setIsEnded(false)
            startTimer(story.duration)
        }
    }
    fun stopStory() {
        if (this::timer.isInitialized)
            timer.cancel()
        storyLiveData.value?.let{
            timeRemainingLiveData.postValue(it.duration)
        }
    }
    fun resumeStory() {
        timeRemainingLiveData.value?.let { startTimer(it) }
    }
    fun pauseStory() {
        if (this::timer.isInitialized)
            timer.cancel()
        setIsEnded(false)
    }


    private fun startTimer(time: Long) {
        timer = object : CountDownTimer(time, 10) {
            override fun onTick(millisUntilFinished: Long) {
                timeRemainingLiveData.postValue(millisUntilFinished)
            }

            override fun onFinish() {
                setIsEnded(true)
                stopStory()
            }
        }.start()
    }
}