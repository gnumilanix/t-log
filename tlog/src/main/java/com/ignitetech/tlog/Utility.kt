package com.ignitetech.tlog

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}

fun getCurrentTime(): String {
    return DATE_FORMAT.format(Date())
}

fun getFormattedTime(date: Date): String {
    return DATE_FORMAT.format(date)
}

fun getLogLevelName(messageLogLevel: Int): String {
    return when (messageLogLevel) {
        Log.VERBOSE -> "VERBOSE"
        Log.DEBUG -> "DEBUG"
        Log.INFO -> "INFO"
        Log.WARN -> "WARN"
        Log.ERROR -> "ERROR"
        Log.ASSERT -> "ASSERT"
        else -> "NONE"
    }
}