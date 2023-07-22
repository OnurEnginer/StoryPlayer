package com.example.storyplayer.util

import android.content.res.Resources

class Utils {
    companion object {
        fun dpToPx(dp: Int): Int = dp*Resources.getSystem().displayMetrics.density.toInt()

        fun pxToDp(px: Int): Int = (px / Resources.getSystem().displayMetrics.density).toInt()
    }
}