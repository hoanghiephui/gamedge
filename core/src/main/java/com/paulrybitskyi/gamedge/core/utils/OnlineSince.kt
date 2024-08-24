package com.paulrybitskyi.gamedge.core.utils

import java.util.Locale

object OnlineSince {
    fun getOnlineSince(startedAt: Long): String {
        val seconds = (System.currentTimeMillis() - startedAt) / 1000
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return String.format(Locale.US, "%d:%02d:%02d", hours, minutes, secs)
    }
}
