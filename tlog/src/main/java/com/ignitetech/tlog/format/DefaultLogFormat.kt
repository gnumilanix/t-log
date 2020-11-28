package com.ignitetech.tlog.format

class DefaultLogFormat : LogFormat {
    override fun formatLogMessage(logLevel: Int, tag: String, message: String): String {
        return message
    }
}