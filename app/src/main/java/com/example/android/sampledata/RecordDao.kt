package com.example.android.sampledata

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface RecordDao {
    @Insert
    fun addRecord(record: Record)

    @Query("SELECT * FROM records WHERE id = :id")
    fun getRecordById(id: Long): Record?

    @Query("SELECT * FROM records WHERE xid = :xid")
    fun getRecordsByXid(xid: String): List<Record>
    @Delete
    fun deleteRecord(record: Record)
    @Query("SELECT * FROM records")
    fun getAllRecords(): List<Record>
    @Update
    fun updateRecord(record: Record)

}
