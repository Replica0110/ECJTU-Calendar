package com.lonx.ecjtu.calendar.data.datasource.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ScoreEntity::class, SelectedCourseEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scoreDao(): ScoreDao
    abstract fun selectedDao(): SelectedCourseDao
}