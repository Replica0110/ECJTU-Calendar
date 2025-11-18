package com.lonx.ecjtu.calendar.data.datasource.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Dao
interface SelectedCourseDao {
    @Query("SELECT * FROM selected_course WHERE term = :term")
    fun getSelectedCoursesByTerm(term: String): Flow<List<SelectedCourseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSelectedCourses(courses: List<SelectedCourseEntity>)

    @Query("DELETE FROM selected_course WHERE term = :term")
    suspend fun deleteSelectedCoursesByTerm(term: String)

    @Query("SELECT DISTINCT term FROM selected_course ORDER BY term DESC")
    fun getAllTerms(): Flow<List<String>>
}