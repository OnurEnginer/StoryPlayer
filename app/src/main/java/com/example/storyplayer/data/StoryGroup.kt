package com.example.storyplayer.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class StoryGroup(
    val id: String,
    val stories: List<Story>,
    val username: String,
    val photo: String
): Parcelable
@Parcelize
data class Story(
    val id: String,
    val mediaId: String,
    val isVideo: Boolean,
    val duration: Long
): Parcelable
