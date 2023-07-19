package com.example.storyplayer.data

data class StoryGroup(
    val id: String,
    val stories: List<Story>,
    val username: String,
    val photo: String
)

data class Story(
    val id: String,
    val mediaId: String,
    val isVideo: Boolean,
    val duration: Long
)
