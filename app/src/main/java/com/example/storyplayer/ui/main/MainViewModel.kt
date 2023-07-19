package com.example.storyplayer.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.storyplayer.data.Story
import com.example.storyplayer.data.StoryGroup

class MainViewModel : ViewModel() {
    val storyGroupLiveData = MutableLiveData<StoryGroup>()
    private val lastStory = mutableMapOf("4" to "5")
        //mapOf("1" to "1", "2" to "2", "3" to "3")

    fun getStories(): List<StoryGroup> =
        listOf(
            StoryGroup("1",listOf(Story("1", "https://w7.pngwing.com/pngs/403/260/png-transparent-pepe-the-frog-internet-meme-frog.png", false, 5000)),"ahmed","https://www.nicepng.com/png/detail/266-2661237_1497154184065-apu-apustaja-thumbs-up.png"),
            StoryGroup("2",listOf(Story("2", "https://w7.pngwing.com/pngs/403/260/png-transparent-pepe-the-frog-internet-meme-frog.png", false, 5000)),"ellie","https://images.twinkl.co.uk/tw1n/image/private/t_630/u/ux/gwen-pose-3_ver_1.png"),
            StoryGroup("3",listOf(Story("3", "https://w7.pngwing.com/pngs/403/260/png-transparent-pepe-the-frog-internet-meme-frog.png", false, 5000)),"cho","https://i.seadn.io/gae/_9to9M96e2o7mE1U1o0oaPQ2s03Y3RSsm6Kubz9k9D4mwC2_oYDpIKTDATidGJv_X9JRyfFf3BnUBrIhoLXCcPzmqVK7z6fWujHz7-8?auto=format&dpr=1&w=1000")
        )
    fun getNextStory(storyGroupId: String): Story? {
            val lastSeenId = lastStory[storyGroupId]
            val stories = getStories().filter { it.id in storyGroupId }[0].stories
            if (lastSeenId == null) {
                lastStory[storyGroupId] = stories[0].id
                return stories[0]
            }
            for (i in stories.indices) {
                if (stories[i].id == lastSeenId)
                    return if (i<stories.size-1) {
                        lastStory[storyGroupId] = stories[i + 1].id
                        stories[i + 1]
                    } else {
                        lastStory.remove(storyGroupId)
                        null
                    }
            }
        lastStory[storyGroupId] = stories[0].id
        return stories[0]
    }
    fun onStoryGroupClicked(storyGroup: StoryGroup) {
        storyGroupLiveData.postValue(storyGroup)
    }
}