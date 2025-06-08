package com.hadeedahyan.sliideandroidapp.utils

import java.util.concurrent.TimeUnit

object TimeUtils {
    fun formatRelativeTime(timestamp: Long?): String {
        if (timestamp == null) return "No creation date"
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < TimeUnit.SECONDS.toMillis(60) -> "${TimeUnit.MILLISECONDS.toSeconds(diff)} second${if (diff < 1000) "" else "s"} ago"
            diff < TimeUnit.MINUTES.toMillis(60) -> "${TimeUnit.MILLISECONDS.toMinutes(diff)} minute${if (TimeUnit.MILLISECONDS.toMinutes(diff) == 1L) "" else "s"} ago"
            else -> "Fetched at $timestamp"
        }
    }
}