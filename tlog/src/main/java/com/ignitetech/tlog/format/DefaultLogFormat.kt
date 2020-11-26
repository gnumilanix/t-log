package com.ignitetech.tlog.format

import android.os.Build
import com.ignitetech.tlog.BuildConfig
import com.ignitetech.tlog.getCurrentTime
import com.ignitetech.tlog.getLogLevelName

class DefaultLogFormat(private val deviceUUID: String) : LogFormat {

    override fun formatLogMessage(logLevel: Int, tag: String, message: String): String {
        return getFormattedLogMessage(
            getLogLevelName(logLevel),
            tag,
            message,
            getCurrentTime(),
            BuildConfig.LIBRARY_PACKAGE_NAME,
            "Android-${Build.VERSION.RELEASE}",
            deviceUUID
        )
    }

    private fun getFormattedLogMessage(
        logLevelName: String,
        tag: String,
        message: String,
        timeStamp: String,
        senderName: String,
        osVersion: String,
        deviceUUID: String
    ): String {
        return "$timeStamp | $senderName : $osVersion | $deviceUUID | [$logLevelName/$tag]: $message"
    }
}