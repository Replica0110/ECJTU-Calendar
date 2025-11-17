package com.lonx.ecjtu.calendar.data.datasource.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScoreDao {

    @Query("SELECT * FROM scores WHERE term = :term")
    fun getScoresByTerm(term: String): Flow<List<ScoreEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScores(scores: List<ScoreEntity>)

    @Query("DELETE FROM scores WHERE term = :term")
    suspend fun deleteScoresByTerm(term: String)

    @Query("SELECT DISTINCT term FROM scores ORDER BY term DESC")
    fun getAllTerms(): Flow<List<String>>
}