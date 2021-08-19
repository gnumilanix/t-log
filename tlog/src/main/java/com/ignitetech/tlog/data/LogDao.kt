package com.ignitetech.tlog.data

import androidx.room.*

@Dao
interface LogDao {
    @Query("SELECT COUNT(*) FROM Log")
    suspend fun getCount(): Long

    @Transaction
    @Query("SELECT * FROM Log LIMIT :limit OFFSET :offset")
    suspend fun getLogs(limit: Int, offset: Int): List<LogModel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addLogs(vararg logs: LogModel)

    @Delete
    suspend fun deleteLogs(vararg logs: LogModel)

    @Query("DELETE FROM Log WHERE log < :date")
    suspend fun deleteOlderThan(date: String): Int

    @Query("DELETE FROM Log")
    suspend fun deleteAll()

    @Query("DELETE FROM Log WHERE id > (SELECT id FROM Log LIMIT 1 OFFSET :offset)")
    suspend fun deleteAll(offset: Int)
}