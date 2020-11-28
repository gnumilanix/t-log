package com.ignitetech.tlog

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