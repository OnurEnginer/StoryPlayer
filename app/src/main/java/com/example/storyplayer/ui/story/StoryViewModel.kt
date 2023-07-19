package com.example.storyplayer.ui.story

import android.os.CountDownTimer
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.storyplayer.data.Story

class StoryViewModel : ViewModel() {
    val storyLiveData = MutableLiveData<Story>()
    val storyEndedLiveData = MutableLiveData<Boolean>()
    val timeRemainingLiveData = MutableLiveData<Long>()
    private lateinit var timer: CountDownTimer

    fun startStory(story: Story?) {
        story?.let {
            storyEndedLiveData.postValue(false)
            startTimer(story.duration)
        }
    }

    fun stopTimer() {
        timer.cancel()
    }

    fun startTimer(time: Long) {
        timer = object : CountDownTimer(time, 10) {
            override fun onTick(millisUntilFinished: Long) {
                timeRemainingLiveData.postValue(millisUntilFinished)
            }

            override fun onFinish() {
                storyEndedLiveData.postValue(true)
                stopTimer()
            }
        }.start()
    }
}