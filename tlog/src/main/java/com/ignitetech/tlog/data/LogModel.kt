package com.ignitetech.tlog.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Log")
data class LogModel(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,

    val tag: String,
    val logLevel: Int,
    val log: String
)