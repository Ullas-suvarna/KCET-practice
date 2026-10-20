package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Dao
interface TestResultDao {
    @Query("SELECT * FROM test_results ORDER BY timestamp DESC")
    fun getAllResults(): Flow<List<TestResultEntity>>

    @Query("SELECT * FROM test_results ORDER BY timestamp DESC LIMIT 1")
    fun getLatestResult(): Flow<TestResultEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: TestResultEntity): Long

    @Query("SELECT * FROM test_results WHERE attemptId = :attemptId AND attemptId != '' LIMIT 1")
    suspend fun getResultByAttemptId(attemptId: String): TestResultEntity?

    @Query("SELECT * FROM test_results WHERE testName = :testName AND timestamp = :timestamp AND score = :score LIMIT 1")
    suspend fun findExactDuplicate(testName: String, timestamp: Long, score: Int): TestResultEntity?

    @Query("SELECT * FROM test_results WHERE testName = :testName AND score = :score AND totalQuestions = :totalQuestions AND timestamp >= :minTimestamp AND timestamp <= :maxTimestamp LIMIT 1")
    suspend fun findRecentDuplicate(testName: String, score: Int, totalQuestions: Int, minTimestamp: Long, maxTimestamp: Long): TestResultEntity?

    @Query("SELECT * FROM test_results")
    suspend fun getAllResultsList(): List<TestResultEntity>

    @Query("DELETE FROM test_results")
    suspend fun clearHistory()

    @Query("DELETE FROM test_results WHERE id = :id")
    suspend fun deleteResultById(id: Int)
}

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE questionId = :questionId")
    suspend fun removeBookmark(questionId: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE questionId = :questionId)")
    fun isBookmarked(questionId: Int): Flow<Boolean>
}

@Database(entities = [TestResultEntity::class, BookmarkEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun testResultDao(): TestResultDao
    abstract fun bookmarkDao(): BookmarkDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE test_results ADD COLUMN attemptId TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kcet_practice_db"
                ).addMigrations(MIGRATION_2_3).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
