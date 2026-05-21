@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package az.tribe.lifeplanner.di

import app.cash.sqldelight.db.SqlDriver

expect class DatabaseDriverFactory() {
    suspend fun createDriver(): SqlDriver
}