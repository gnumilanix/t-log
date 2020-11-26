package com.ignitetech.tlog.data

import com.ignitetech.tlog.getFormattedTime
import java.util.*
import kotlin.math.ceil

internal class LogRepository(private val logDao: LogDao) {
    suspend fun addLog(tag: String, logLevel: Int, log: String) {
        if (log.isNotEmpty()) {
            logDao.addLogs(LogModel(0, tag, logLevel, log))
        }
    }

    suspend fun clearSavedLogs() {
        logDao.deleteAll()
    }

    suspend fun getLogs(page: Int, limit: Int = LIMIT): List<LogModel> {
        if (page < 0) {
            throw IllegalStateException("Page must not be zero or negative")
        }

        return logDao.getLogs(LIMIT, page * limit)
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
        private const val LIMIT = 5000
    }
}