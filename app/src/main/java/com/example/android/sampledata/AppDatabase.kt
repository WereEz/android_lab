package com.example.android.sampledata

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Record::class], version = 1, )
abstract class AppDatabase : RoomDatabase() {
    abstract fun recordDao(): RecordDao
}