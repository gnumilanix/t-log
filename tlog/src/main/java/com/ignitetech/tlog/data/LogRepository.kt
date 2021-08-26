package com.ignitetech.tlog.data

import android.os.Build
import com.ignitetech.tlog.getCurrentTime
import com.ignitetech.tlog.getFormattedTime
import java.util.*
import kotlin.math.ceil

internal class LogRepository(private val logDao: LogDao) {
    suspend fun addLog(tag: String, logLevel: Int, log: String) {
        if (log.isNotEmpty()) {
            logDao.addLogs(LogModel(0, getCurrentTime(), Build.VERSION.SDK_INT, tag, logLevel, log))
        }
    }

    suspend fun clearSavedLogs() {
        logDao.deleteAll()
    }

    suspend fun clearSavedLogs(offset: Int, logLevel: Int) {
        if (offset < 0) {
            throw IllegalStateException("Page must not be zero or negative")
        }

        logDao.deleteAll(offset, logLevel)
    }

    suspend fun getLogs(page: Int, limit: Int = LIMIT): List<LogModel> {
        if (page < 0) {
            throw IllegalStateException("Page must not be zero or negative")
        }

        return logDao.getLogs(limit, page * limit)
    }

    suspend fun clearLogs(pushedLogs: List<LogModel>) {
        if (pushedLogs.isNotEmpty()) {
            logDao.deleteLogs(*pushedLogs.toTypedArray())
        }
    }

    suspend fun count(): Long {
        return logDao.getCount()
    }

    suspend fun getPages(limit: Int = LIMIT): Int {
        return ceil((count() * 1.0f / limit).toDouble()).toInt()
    }

    suspend fun clearOldLogs(expiryTimeInSeconds: Int) {
        val calendar = Calendar.getInstance().apply {
            add(Calendar.SECOND, -expiryTimeInSeconds)
        }

        logDao.deleteOlderThan(getFormattedTime(calendar.time))
    }

    companion object {
        const val LIMIT = 100
    }
}