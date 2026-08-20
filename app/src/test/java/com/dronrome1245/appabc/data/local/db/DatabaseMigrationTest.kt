package com.dronrome1245.appabc.data.local.db

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DatabaseMigrationTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val databaseName = "app_abc_migration_1_2_test.db"

    @After
    fun cleanUp() {
        context.deleteDatabase(databaseName)
    }

    @Test
    fun `migration 1 to 2 preserves attempts and backfills letter progress`() {
        context.deleteDatabase(databaseName)
        createVersionOneHelper().use { helper ->
            val db = helper.writableDatabase
            insertAttempt(db, 1, "А", "А", 1, 100, 1_000, "s1")
            insertAttempt(db, 2, "А", "М", 0, 300, 2_000, "s1")
            insertAttempt(db, 3, "А", "А", 1, 200, 3_000, "s1")
            insertAttempt(db, 4, "М", "М", 1, 400, 4_000, "s1")
        }

        createVersionTwoHelper().use { helper ->
            val db = helper.writableDatabase

            db.query("SELECT COUNT(*) AS count FROM attempts").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals(4, cursor.getInt(cursor.getColumnIndexOrThrow("count")))
            }

            db.query("SELECT attemptsCount, correctCount, lastSeenTimestamp, averageResponseTimeMs FROM letter_progress WHERE letter = 'А'").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals(3, cursor.getInt(cursor.getColumnIndexOrThrow("attemptsCount")))
                assertEquals(2, cursor.getInt(cursor.getColumnIndexOrThrow("correctCount")))
                assertEquals(3_000L, cursor.getLong(cursor.getColumnIndexOrThrow("lastSeenTimestamp")))
                assertEquals(200L, cursor.getLong(cursor.getColumnIndexOrThrow("averageResponseTimeMs")))
            }

            db.query("SELECT totalQuestions, correctAnswers, passed FROM session_results WHERE sessionId = 's1'").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals(4, cursor.getInt(cursor.getColumnIndexOrThrow("totalQuestions")))
                assertEquals(3, cursor.getInt(cursor.getColumnIndexOrThrow("correctAnswers")))
                assertEquals(0, cursor.getInt(cursor.getColumnIndexOrThrow("passed")))
            }
        }
    }

    private fun createVersionOneHelper(): SupportSQLiteOpenHelper {
        val callback = object : SupportSQLiteOpenHelper.Callback(1) {
            override fun onCreate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS attempts (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, targetLetter TEXT NOT NULL, selectedLetter TEXT NOT NULL, isCorrect INTEGER NOT NULL, responseTimeMs INTEGER NOT NULL, timestamp INTEGER NOT NULL, sessionId TEXT NOT NULL, levelId INTEGER NOT NULL, learningPolicyVersion INTEGER NOT NULL, curriculumVersion INTEGER NOT NULL)"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS letters (symbol TEXT NOT NULL, spokenName TEXT NOT NULL, levelIntroduced INTEGER NOT NULL, PRIMARY KEY(symbol))"
                )
            }

            override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
        }
        return FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(databaseName)
                .callback(callback)
                .build()
        )
    }

    private fun createVersionTwoHelper(): SupportSQLiteOpenHelper {
        val callback = object : SupportSQLiteOpenHelper.Callback(2) {
            override fun onCreate(db: SupportSQLiteDatabase) = Unit

            override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
                assertEquals(1, oldVersion)
                assertEquals(2, newVersion)
                DatabaseMigrations.MIGRATION_1_2.migrate(db)
            }
        }
        return FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(databaseName)
                .callback(callback)
                .build()
        )
    }

    private fun insertAttempt(
        db: SupportSQLiteDatabase,
        id: Long,
        target: String,
        selected: String,
        correct: Int,
        responseTimeMs: Long,
        timestamp: Long,
        sessionId: String
    ) {
        db.execSQL(
            "INSERT INTO attempts (id, targetLetter, selectedLetter, isCorrect, responseTimeMs, timestamp, sessionId, levelId, learningPolicyVersion, curriculumVersion) VALUES (?, ?, ?, ?, ?, ?, ?, 1, 2, 2)",
            arrayOf(id, target, selected, correct, responseTimeMs, timestamp, sessionId)
        )
    }
}
