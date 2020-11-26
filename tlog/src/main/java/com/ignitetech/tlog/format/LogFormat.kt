package com.ignitetech.tlog.format

import java.io.Serializable

interface LogFormat : Serializable {
    /**
     * Implement this method to override the default log message format.
     *
     * @param logLevel The level of logcat logging that Parse should do.
     * @param message  Log message that need to be customized.
     * @return Formatted Log Message that will store in database.
     */
    fun formatLogMessage(logLevel: Int, tag: String, message: String): String
}