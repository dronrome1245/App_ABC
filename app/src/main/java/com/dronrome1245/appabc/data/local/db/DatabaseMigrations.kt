package com.dronrome1245.appabc.data.local.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `letter_progress` (
                    `letter` TEXT NOT NULL,
                    `attemptsCount` INTEGER NOT NULL,
                    `correctCount` INTEGER NOT NULL,
                    `lastSeenTimestamp` INTEGER NOT NULL,
                    `averageResponseTimeMs` INTEGER NOT NULL,
                    PRIMARY KEY(`letter`)
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `session_results` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `sessionId` TEXT NOT NULL,
                    `levelId` INTEGER NOT NULL,
                    `totalQuestions` INTEGER NOT NULL,
                    `correctAnswers` INTEGER NOT NULL,
                    `passed` INTEGER NOT NULL,
                    `completedAt` INTEGER NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS `index_session_results_sessionId` ON `session_results` (`sessionId`)"
            )
        }
    }
}
