package com.example.android.sampledata

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "records")
data class Record(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    var description: String,
    val xid: String
)
