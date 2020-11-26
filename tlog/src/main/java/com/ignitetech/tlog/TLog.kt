package com.ignitetech.tlog

import android.content.Context
import android.util.Log
import com.ignitetech.tlog.data.LogDatabase
import com.ignitetech.tlog.data.LogModel
import com.ignitetech.tlog.data.LogRepository
import com.ignitetech.tlog.format.DefaultLogFormat
import com.ignitetech.tlog.format.LogFormat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

object TLog {
    private const val TAG_ASSERT = "ASSERT"
    private val EXPIRY_TIME = TimeUnit.DAYS.toSeconds(7).toInt()
    private var logLevel = Log.WARN

    private val serviceJob = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + serviceJob)

    private var logRepository: LogRepository? = null
    private var logFormat: LogFormat? = null

    fun initialize(
        context: Context,
        deviceId: String,
        expiryTimeInSeconds: Int = EXPIRY_TIME,
        logFormat: LogFormat = DefaultLogFormat(deviceId)
    ) {
        synchronized(TLog::class.java) {
            this.logFormat = logFormat

            logRepository = LogRepository(LogDatabase.getDatabase(context).logRepository()).apply {
                coroutineScope.launch {
                    clearOldLogs(expiryTimeInSeconds)
                }
            }
        }
    }

    /**
     * Call this method to define a custom log message format.
     *
     * @param logFormat LogFormat to set custom log message format.
     */
    fun setLogFormat(logFormat: LogFormat) {
        this.logFormat = logFormat
    }

    /**
     * Sets the level of logging to display, where each level includes all those below it.
     * The default level is LOG_LEVEL_NONE. Please ensure this is set to Log#ERROR
     * or LOG_LEVEL_NONE before deploying your app to ensure no sensitive information is
     * logged. The levels are:
     *
     *  * [Log.ASSERT]
     *  * [Log.VERBOSE]
     *  * [Log.DEBUG]
     *  * [Log.INFO]
     *  * [Log.WARN]
     *  * [Log.ERROR]
     *
     *
     * @param logLevel The level of logcat logging that Parse should do.
     */
    fun setLogLevel(logLevel: Int) {
        TLog.logLevel = logLevel
    }

    fun v(tag: String, message: String, tr: Throwable? = null) {
        if (Log.VERBOSE >= logLevel) {
            Log.v(tag, getMessage(message, tr))
        }

        r(tag, Log.VERBOSE, message)
    }

    fun d(tag: String, message: String, tr: Throwable? = null) {
        if (Log.DEBUG >= logLevel) {
            Log.d(tag, getMessage(message, tr))
        }

        r(tag, Log.DEBUG, message)
    }

    fun i(tag: String, message: String, tr: Throwable? = null) {
        if (Log.INFO >= logLevel) {
            Log.i(tag, getMessage(message, tr))
        }

        r(tag, Log.INFO, message)
    }

    fun w(tag: String, message: String, tr: Throwable? = null) {
        if (Log.WARN >= logLevel) {
            Log.w(tag, getMessage(message, tr))
        }

        r(tag, Log.WARN, message)
    }

    fun e(tag: String, message: String, tr: Throwable? = null) {
        if (Log.ERROR >= logLevel) {
            Log.e(tag, getMessage(message, tr))
        }

        r(tag, Log.ERROR, message)
    }

    fun exception(tag: String, message: String? = null, tr: Throwable? = null) {
        val methodName = Thread.currentThread().stackTrace[1].methodName

        if (Log.ERROR >= logLevel) {
            Log.e(tag, "**********************************************")
            Log.e(tag, "EXCEPTION: $methodName, $message\n${Log.getStackTraceString(tr)}")
            Log.e(tag, "**********************************************")
        }

        r(tag, Log.ERROR, "EXCEPTION: $methodName, $message")
    }

    fun a(message: String) {
        r(TAG_ASSERT, Log.ASSERT, message)
    }

    private fun r(tag: String, logLevel: Int, message: String) {
        val formattedMessage = getFormattedLog(logLevel, tag, message)

        if (!formattedMessage.isNullOrEmpty()) {
            coroutineScope.launch {
                logRepository?.addLog(tag, logLevel, formattedMessage)
            }
        }
    }

    private fun getMessage(message: String, tr: Throwable?): String {
        return "$message\n${Log.getStackTraceString(tr)}"
    }

    private fun getFormattedLog(logLevel: Int, tag: String, message: String): String? {
        return logFormat?.formatLogMessage(logLevel, tag, message)
    }

    /**
     * Call this method to get a list of stored Device Logs.
     *
     * @param deleteLogs If true then logs will delete from the device.
     * @param page    If there are more than one batch of device log then specify the batch number.
     * Batch number should be greater than or equal to 1.
     * @return List of [LogModel] or empty list if batch number is greater than the
     * [TLog.getLogPages]
     */
    suspend fun getLogs(deleteLogs: Boolean, page: Int): List<LogModel> {
        return logRepository?.getLogs(page)?.also {
            if (deleteLogs) {
                deleteLogs(it)
            }
        } ?: listOf()
    }

    /**
     * Call this method to check whether any device logs are available.
     *
     * @return true If device has some pending logs otherwise false.
     */
    suspend fun hasLogs(): Boolean {
        return (logRepository?.count() ?: 0) > 0L
    }

    /**
     * Call this method to get the count of stored device logs.
     *
     * @return The number of device logs.
     */
    suspend fun logsCount(): Long {
        return logRepository?.count() ?: 0
    }

    /**
     * Call this method to get number of device logs batches. Each batch contains the 5000 device
     * logs.
     *
     * @return The number of device logs batches.
     */
    suspend fun getLogPages(): Int {
        return logRepository?.getPages() ?: 0
    }

    /**
     * Call this method to delete all logs from device.
     */
    suspend fun deleteLogs() {
        logRepository?.clearSavedLogs()
    }

    suspend fun deleteLogs(logs: List<LogModel>) {
        logRepository?.clearLogs(logs)
    }

    suspend fun dispatchLogs(dispatcher: suspend (page: Int, logs: List<LogModel>) -> Boolean) {
        for (page in 1..getLogPages()) {
            val logs = getLogs(false, page)

            if (logs.isEmpty()) {
                break
            }

            if (dispatcher(page, logs)) {
                deleteLogs(logs)
            }
        }
    }
}