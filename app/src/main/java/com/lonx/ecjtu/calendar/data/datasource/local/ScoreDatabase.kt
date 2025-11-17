package com.lonx.ecjtu.calendar.data.datasource.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ScoreEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ScoreDatabase : RoomDatabase() {
    abstract fun scoreDao(): ScoreDao
}