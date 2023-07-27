package com.example.storyplayer.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.storyplayer.data.Story
import com.example.storyplayer.data.StoryGroup

class MainViewModel : ViewModel() {
    lateinit var list: List<StoryGroup>
    private val lastStoryId = mutableMapOf("4" to "5", "55" to null)
    val storyGroupIndexLiveData = MutableLiveData<Int>()

    fun getStories(): List<StoryGroup> =
        listOf(
            StoryGroup("1",listOf(Story("1", "img_feels_good_man", false, 5000), Story("2","img_mcqueen", false, 5000),Story("1", "img_miami_heat", false, 5000)),"Ahmad","img_ahmad"),
            StoryGroup("2",listOf(Story("6", "img_japan_1", false, 5000),Story("7", "img_japan_2", false, 5000)),"Ellie","img_ellie"),
            StoryGroup("3",listOf(Story("15", "img_barbie", false, 5000), Story("16", "pigeon_video", true, 8500)),"Cho","img_cho")
        )
    fun setStoryGroup(index: Int) {
        storyGroupIndexLiveData.postValue(index)
    }
    fun getNextStoryGroup() {
        val index = if (storyGroupIndexLiveData.value == null || storyGroupIndexLiveData.value!! >= getStories().size-1) -1 else storyGroupIndexLiveData.value!!+1
        storyGroupIndexLiveData.postValue(index)
    }
    fun getPrevStoryGroup() {
        val index = if (storyGroupIndexLiveData.value == null || storyGroupIndexLiveData.value!! <= 0) -1 else storyGroupIndexLiveData.value!!-1
        storyGroupIndexLiveData.postValue(index)
    }
    fun onStoryGroupClicked(index: Int) {
        storyGroupIndexLiveData.postValue(index)
    }
    fun getLastSeenIndex(storyGroupId: String): Int {
        val lastSeenId = lastStoryId[storyGroupId]
        val stories = list.filter { it.id in storyGroupId }[0].stories
        if (lastSeenId == null) {
            lastStoryId[storyGroupId] = stories[0].id
            return 0
        }
        for (i in stories.indices) {
            if (stories[i].id == lastSeenId)
                return i
        }
        lastStoryId[storyGroupId] = stories[0].id
        return 0
    }
    fun setLastSeenId(storyGroupId: String, storyId: String?) {
        lastStoryId[storyGroupId] = storyId
    }
}