package az.tribe.lifeplanner.di

import androidx.sqlite.db.SupportSQLiteDatabase

internal fun columnExists(db: SupportSQLiteDatabase, tableName: String, columnName: String): Boolean {
    return try {
        val cursor = db.query("PRAGMA table_info($tableName)")
        cursor.use {
            while (it.moveToNext()) {
                val nameIndex = it.getColumnIndex("name")
                if (nameIndex >= 0 && it.getString(nameIndex) == columnName) {
                    return true
                }
            }
        }
        false
    } catch (e: Exception) {
        false
    }
}

internal fun addColumnSafe(db: SupportSQLiteDatabase, table: String, column: String, def: String) {
    if (!columnExists(db, table, column)) {
        try {
            db.execSQL("ALTER TABLE $table ADD COLUMN $column $def")
        } catch (_: Exception) { }
    }
}

internal fun migrateToVersion5(db: SupportSQLiteDatabase) {
    // Create HabitEntity table if not exists
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS HabitEntity (
            id TEXT PRIMARY KEY NOT NULL,
            title TEXT NOT NULL,
            description TEXT NOT NULL DEFAULT '',
            category TEXT NOT NULL,
            frequency TEXT NOT NULL DEFAULT 'DAILY',
            targetCount INTEGER NOT NULL DEFAULT 1,
            currentStreak INTEGER NOT NULL DEFAULT 0,
            longestStreak INTEGER NOT NULL DEFAULT 0,
            totalCompletions INTEGER NOT NULL DEFAULT 0,
            lastCompletedDate TEXT,
            linkedGoalId TEXT,
            correlationScore REAL NOT NULL DEFAULT 0.0,
            isActive INTEGER NOT NULL DEFAULT 1,
            createdAt TEXT NOT NULL,
            reminderTime TEXT,
            FOREIGN KEY (linkedGoalId) REFERENCES GoalEntity(id) ON DELETE SET NULL
        )
        """.trimIndent()
    )

    // Create HabitCheckInEntity table if not exists
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS HabitCheckInEntity (
            id TEXT PRIMARY KEY NOT NULL,
            habitId TEXT NOT NULL,
            date TEXT NOT NULL,
            completed INTEGER NOT NULL DEFAULT 1,
            notes TEXT NOT NULL DEFAULT '',
            FOREIGN KEY (habitId) REFERENCES HabitEntity(id) ON DELETE CASCADE
        )
        """.trimIndent()
    )

    // Create JournalEntryEntity table if not exists
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS JournalEntryEntity (
            id TEXT PRIMARY KEY NOT NULL,
            title TEXT NOT NULL,
            content TEXT NOT NULL,
            mood TEXT NOT NULL DEFAULT 'NEUTRAL',
            linkedGoalId TEXT,
            promptUsed TEXT,
            tags TEXT NOT NULL DEFAULT '',
            date TEXT NOT NULL,
            createdAt TEXT NOT NULL,
            updatedAt TEXT,
            FOREIGN KEY (linkedGoalId) REFERENCES GoalEntity(id) ON DELETE SET NULL
        )
        """.trimIndent()
    )

    // Safely add columns to UserProgressEntity if they don't exist
    val columnsToAdd = listOf(
        "totalXp" to "INTEGER NOT NULL DEFAULT 0",
        "currentLevel" to "INTEGER NOT NULL DEFAULT 1",
        "goalsCompleted" to "INTEGER NOT NULL DEFAULT 0",
        "habitsCompleted" to "INTEGER NOT NULL DEFAULT 0",
        "journalEntriesCount" to "INTEGER NOT NULL DEFAULT 0",
        "longestStreak" to "INTEGER NOT NULL DEFAULT 0"
    )

    columnsToAdd.forEach { (columnName, columnDef) ->
        if (!columnExists(db, "UserProgressEntity", columnName)) {
            try {
                db.execSQL("ALTER TABLE UserProgressEntity ADD COLUMN $columnName $columnDef")
            } catch (e: Exception) {
                // Column might already exist or table doesn't exist yet
            }
        }
    }

    // Create BadgeEntity table if not exists
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS BadgeEntity (
            id TEXT PRIMARY KEY NOT NULL,
            badgeType TEXT NOT NULL,
            earnedAt TEXT NOT NULL,
            isNew INTEGER NOT NULL DEFAULT 1
        )
        """.trimIndent()
    )

    // Create ChallengeEntity table if not exists
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS ChallengeEntity (
            id TEXT PRIMARY KEY NOT NULL,
            challengeType TEXT NOT NULL,
            startDate TEXT NOT NULL,
            endDate TEXT NOT NULL,
            currentProgress INTEGER NOT NULL DEFAULT 0,
            targetProgress INTEGER NOT NULL,
            isCompleted INTEGER NOT NULL DEFAULT 0,
            completedAt TEXT,
            xpEarned INTEGER NOT NULL DEFAULT 0
        )
        """.trimIndent()
    )

    // Create indexes if not exist
    db.execSQL("CREATE INDEX IF NOT EXISTS idx_habits_category ON HabitEntity(category)")
    db.execSQL("CREATE INDEX IF NOT EXISTS idx_habits_linked_goal ON HabitEntity(linkedGoalId)")
    db.execSQL("CREATE INDEX IF NOT EXISTS idx_habits_active ON HabitEntity(isActive)")
    db.execSQL("CREATE INDEX IF NOT EXISTS idx_checkins_habit ON HabitCheckInEntity(habitId)")
    db.execSQL("CREATE INDEX IF NOT EXISTS idx_checkins_date ON HabitCheckInEntity(date)")
    db.execSQL("CREATE INDEX IF NOT EXISTS idx_journal_date ON JournalEntryEntity(date)")
    db.execSQL("CREATE INDEX IF NOT EXISTS idx_journal_mood ON JournalEntryEntity(mood)")
    db.execSQL("CREATE INDEX IF NOT EXISTS idx_journal_goal ON JournalEntryEntity(linkedGoalId)")
    db.execSQL("CREATE INDEX IF NOT EXISTS idx_badge_type ON BadgeEntity(badgeType)")
    db.execSQL("CREATE INDEX IF NOT EXISTS idx_badge_new ON BadgeEntity(isNew)")
    db.execSQL("CREATE INDEX IF NOT EXISTS idx_challenge_type ON ChallengeEntity(challengeType)")
    db.execSQL("CREATE INDEX IF NOT EXISTS idx_challenge_completed ON ChallengeEntity(isCompleted)")
    db.execSQL("CREATE INDEX IF NOT EXISTS idx_challenge_end_date ON ChallengeEntity(endDate)")
}

internal fun migrateToVersion6(db: SupportSQLiteDatabase) {
    // Create GoalDependencyEntity table if not exists
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS GoalDependencyEntity (
            id TEXT PRIMARY KEY NOT NULL,
            sourceGoalId TEXT NOT NULL,
            targetGoalId TEXT NOT NULL,
            dependencyType TEXT NOT NULL,
            createdAt TEXT NOT NULL,
            FOREIGN KEY (sourceGoalId) REFERENCES GoalEntity(id) ON DELETE CASCADE,
            FOREIGN KEY (targetGoalId) REFERENCES GoalEntity(id) ON DELETE CASCADE
        )
        """.trimIndent()
    )

    // Create indexes for goal dependencies
    db.execSQL("CREATE INDEX IF NOT EXISTS idx_dependency_source ON GoalDependencyEntity(sourceGoalId)")
    db.execSQL("CREATE INDEX IF NOT EXISTS idx_dependency_target ON GoalDependencyEntity(targetGoalId)")
    db.execSQL("CREATE INDEX IF NOT EXISTS idx_dependency_type ON GoalDependencyEntity(dependencyType)")
}

internal fun migrateToVersion7(db: SupportSQLiteDatabase) {
    // Create ChatSessionEntity table if not exists
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS ChatSessionEntity (
            id TEXT PRIMARY KEY NOT NULL,
            title TEXT NOT NULL,
            createdAt TEXT NOT NULL,
            lastMessageAt TEXT NOT NULL,
            summary TEXT,
            coachId TEXT NOT NULL DEFAULT 'luna_general'
        )
        """.trimIndent()
    )

    // Add coachId column if table was created without it (defensive migration)
    if (!columnExists(db, "ChatSessionEntity", "coachId")) {
        try {
            db.execSQL("ALTER TABLE ChatSessionEntity ADD COLUMN coachId TEXT NOT NULL DEFAULT 'luna_general'")
        } catch (e: Exception) {
            // Column might already exist
        }
    }

    // Create ChatMessageEntity table if not exists
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS ChatMessageEntity (
            id TEXT PRIMARY KEY NOT NULL,
            sessionId TEXT NOT NULL,
            content TEXT NOT NULL,
            role TEXT NOT NULL,
            timestamp TEXT NOT NULL,
            relatedGoalId TEXT,
            metadata TEXT,
            FOREIGN KEY (sessionId) REFERENCES ChatSessionEntity(id) ON DELETE CASCADE
        )
        """.trimIndent()
    )

    // Create ReviewReportEntity table if not exists
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS ReviewReportEntity (
            id TEXT PRIMARY KEY NOT NULL,
            type TEXT NOT NULL,
            periodStart TEXT NOT NULL,
            periodEnd TEXT NOT NULL,
            generatedAt TEXT NOT NULL,
            summary TEXT NOT NULL,
            highlightsJson TEXT NOT NULL,
            insightsJson TEXT NOT NULL,
            recommendationsJson TEXT NOT NULL,
            statsJson TEXT NOT NULL,
            feedbackRating TEXT,
            feedbackComment TEXT,
            feedbackAt TEXT,
            isRead INTEGER NOT NULL DEFAULT 0
        )
        """.trimIndent()
    )

    // Create indexes for chat
    db.execSQL("CREATE INDEX IF NOT EXISTS idx_chat_session_last_message ON ChatSessionEntity(lastMessageAt)")
    db.execSQL("CREATE INDEX IF NOT EXISTS idx_chat_session_coach ON ChatSessionEntity(coachId)")
    db.execSQL("CREATE INDEX IF NOT EXISTS idx_chat_message_session ON ChatMessageEntity(sessionId)")
    db.execSQL("CREATE INDEX IF NOT EXISTS idx_chat_message_timestamp ON ChatMessageEntity(timestamp)")

    // Create indexes for reviews
    db.execSQL("CREATE INDEX IF NOT EXISTS idx_review_type ON ReviewReportEntity(type)")
    db.execSQL("CREATE INDEX IF NOT EXISTS idx_review_generated ON ReviewReportEntity(generatedAt)")
    db.execSQL("CREATE INDEX IF NOT EXISTS idx_review_unread ON ReviewReportEntity(isRead)")
}

internal fun migrateToVersion8(db: SupportSQLiteDatabase) {
    // Create ReminderEntity table if not exists
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS ReminderEntity (
            id TEXT PRIMARY KEY NOT NULL,
            title TEXT NOT NULL,
            message TEXT NOT NULL,
            type TEXT NOT NULL,
            frequency TEXT NOT NULL,
            scheduledTime TEXT NOT NULL,
            scheduledDays TEXT NOT NULL DEFAULT '',
            linkedGoalId TEXT,
            linkedHabitId TEXT,
            isEnabled INTEGER NOT NULL DEFAULT 1,
            isSmartTiming INTEGER NOT NULL DEFAULT 0,
            lastTriggeredAt TEXT,
            snoozedUntil TEXT,
            createdAt TEXT NOT NULL,
            updatedAt TEXT,
            FOREIGN KEY (linkedGoalId) REFERENCES GoalEntity(id) ON DELETE SET NULL,
            FOREIGN KEY (linkedHabitId) REFERENCES HabitEntity(id) ON DELETE SET NULL
        )
        """.trimIndent()
    )

    // Create ReminderSettingsEntity table if not exists
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS ReminderSettingsEntity (
            id TEXT PRIMARY KEY NOT NULL DEFAULT 'default',
            isEnabled INTEGER NOT NULL DEFAULT 1,
            quietHoursStart TEXT NOT NULL DEFAULT '22:00',
            quietHoursEnd TEXT NOT NULL DEFAULT '07:00',
            preferredMorningTime TEXT NOT NULL DEFAULT '08:00',
            preferredEveningTime TEXT NOT NULL DEFAULT '20:00',
            smartTimingEnabled INTEGER NOT NULL DEFAULT 1,
            maxRemindersPerDay INTEGER NOT NULL DEFAULT 5,
            weeklyReviewDay TEXT NOT NULL DEFAULT 'SUNDAY',
            weeklyReviewTime TEXT NOT NULL DEFAULT '10:00'
        )
        """.trimIndent()
    )

    // Create UserActivityPatternEntity table if not exists
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS UserActivityPatternEntity (
            id TEXT PRIMARY KEY NOT NULL DEFAULT 'default',
            mostActiveHours TEXT NOT NULL DEFAULT '',
            mostActiveDays TEXT NOT NULL DEFAULT '',
            averageResponseTime INTEGER NOT NULL DEFAULT 0,
            bestCheckInTimes TEXT NOT NULL DEFAULT '',
            lastUpdated TEXT NOT NULL
        )
        """.trimIndent()
    )

    // Create ScheduledNotificationEntity table if not exists
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS ScheduledNotificationEntity (
            id TEXT PRIMARY KEY NOT NULL,
            reminderId TEXT NOT NULL,
            title TEXT NOT NULL,
            message TEXT NOT NULL,
            scheduledAt TEXT NOT NULL,
            isDelivered INTEGER NOT NULL DEFAULT 0,
            deliveredAt TEXT,
            isSnoozed INTEGER NOT NULL DEFAULT 0,
            isDismissed INTEGER NOT NULL DEFAULT 0,
            FOREIGN KEY (reminderId) REFERENCES ReminderEntity(id) ON DELETE CASCADE
        )
        """.trimIndent()
    )

    // Create indexes for reminders
    db.execSQL("CREATE INDEX IF NOT EXISTS idx_reminder_type ON ReminderEntity(type)")
    db.execSQL("CREATE INDEX IF NOT EXISTS idx_reminder_enabled ON ReminderEntity(isEnabled)")
    db.execSQL("CREATE INDEX IF NOT EXISTS idx_reminder_goal ON ReminderEntity(linkedGoalId)")
    db.execSQL("CREATE INDEX IF NOT EXISTS idx_reminder_habit ON ReminderEntity(linkedHabitId)")
    db.execSQL("CREATE INDEX IF NOT EXISTS idx_scheduled_reminder ON ScheduledNotificationEntity(reminderId)")
    db.execSQL("CREATE INDEX IF NOT EXISTS idx_scheduled_time ON ScheduledNotificationEntity(scheduledAt)")
    db.execSQL("CREATE INDEX IF NOT EXISTS idx_scheduled_delivered ON ScheduledNotificationEntity(isDelivered)")
}

internal fun migrateToVersion9(db: SupportSQLiteDatabase) {
    // Add linkedHabitId column to JournalEntryEntity if it doesn't exist
    if (!columnExists(db, "JournalEntryEntity", "linkedHabitId")) {
        try {
            db.execSQL("ALTER TABLE JournalEntryEntity ADD COLUMN linkedHabitId TEXT")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_journal_habit ON JournalEntryEntity(linkedHabitId)")
        } catch (e: Exception) {
            // Column might already exist
        }
    }
}

internal fun migrateToVersion10(db: SupportSQLiteDatabase) {
    // Create CustomCoachEntity table
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS CustomCoachEntity (
            id TEXT PRIMARY KEY NOT NULL,
            name TEXT NOT NULL,
            icon TEXT NOT NULL,
            iconBackgroundColor TEXT NOT NULL DEFAULT '#6366F1',
            iconAccentColor TEXT NOT NULL DEFAULT '#818CF8',
            systemPrompt TEXT NOT NULL,
            characteristics TEXT NOT NULL DEFAULT '',
            isFromTemplate INTEGER NOT NULL DEFAULT 0,
            templateId TEXT,
            createdAt TEXT NOT NULL,
            updatedAt TEXT
        )
        """.trimIndent()
    )

    // Create CoachGroupEntity table
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS CoachGroupEntity (
            id TEXT PRIMARY KEY NOT NULL,
            name TEXT NOT NULL,
            icon TEXT NOT NULL,
            description TEXT NOT NULL DEFAULT '',
            createdAt TEXT NOT NULL,
            updatedAt TEXT
        )
        """.trimIndent()
    )

    // Create CoachGroupMemberEntity table
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS CoachGroupMemberEntity (
            id TEXT PRIMARY KEY NOT NULL,
            groupId TEXT NOT NULL,
            coachType TEXT NOT NULL,
            coachId TEXT NOT NULL,
            displayOrder INTEGER NOT NULL DEFAULT 0,
            FOREIGN KEY (groupId) REFERENCES CoachGroupEntity(id) ON DELETE CASCADE
        )
        """.trimIndent()
    )

    // Create indexes
    db.execSQL("CREATE INDEX IF NOT EXISTS idx_custom_coach_template ON CustomCoachEntity(templateId)")
    db.execSQL("CREATE INDEX IF NOT EXISTS idx_coach_group_member_group ON CoachGroupMemberEntity(groupId)")
    db.execSQL("CREATE INDEX IF NOT EXISTS idx_coach_group_member_coach ON CoachGroupMemberEntity(coachId)")
}

internal fun migrateToVersion11(db: SupportSQLiteDatabase) {
    // Create FocusSessionEntity table
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS FocusSessionEntity (
            id TEXT PRIMARY KEY NOT NULL,
            goalId TEXT NOT NULL,
            milestoneId TEXT NOT NULL,
            plannedDurationMinutes INTEGER NOT NULL,
            actualDurationSeconds INTEGER NOT NULL DEFAULT 0,
            wasCompleted INTEGER NOT NULL DEFAULT 0,
            xpEarned INTEGER NOT NULL DEFAULT 0,
            startedAt TEXT NOT NULL,
            completedAt TEXT,
            createdAt TEXT NOT NULL,
            FOREIGN KEY (goalId) REFERENCES GoalEntity(id) ON DELETE CASCADE,
            FOREIGN KEY (milestoneId) REFERENCES MilestoneEntity(id) ON DELETE CASCADE
        )
        """.trimIndent()
    )

    // Create indexes
    db.execSQL("CREATE INDEX IF NOT EXISTS idx_focus_goal ON FocusSessionEntity(goalId)")
    db.execSQL("CREATE INDEX IF NOT EXISTS idx_focus_milestone ON FocusSessionEntity(milestoneId)")
    db.execSQL("CREATE INDEX IF NOT EXISTS idx_focus_completed ON FocusSessionEntity(wasCompleted)")
    db.execSQL("CREATE INDEX IF NOT EXISTS idx_focus_started ON FocusSessionEntity(startedAt)")
}

internal fun migrateToVersion12(db: SupportSQLiteDatabase) {
    // Add mood, ambientSound, focusTheme columns to FocusSessionEntity
    try {
        db.execSQL("ALTER TABLE FocusSessionEntity ADD COLUMN mood TEXT")
    } catch (_: Exception) { /* Column already exists */ }
    try {
        db.execSQL("ALTER TABLE FocusSessionEntity ADD COLUMN ambientSound TEXT")
    } catch (_: Exception) { /* Column already exists */ }
    try {
        db.execSQL("ALTER TABLE FocusSessionEntity ADD COLUMN focusTheme TEXT")
    } catch (_: Exception) { /* Column already exists */ }
}

internal fun migrateToSyncColumns(db: SupportSQLiteDatabase) {
    val tablesWithLastSynced = listOf(
        "GoalEntity", "MilestoneEntity", "GoalHistoryEntity",
        "UserProgressEntity", "HabitEntity", "HabitCheckInEntity",
        "JournalEntryEntity", "BadgeEntity", "ChallengeEntity",
        "GoalDependencyEntity", "ChatSessionEntity", "ChatMessageEntity",
        "ReviewReportEntity", "ReminderEntity", "CustomCoachEntity",
        "CoachGroupEntity", "CoachGroupMemberEntity", "FocusSessionEntity"
    )
    for (table in tablesWithLastSynced) {
        addColumnSafe(db, table, "sync_updated_at", "TEXT")
        addColumnSafe(db, table, "is_deleted", "INTEGER NOT NULL DEFAULT 0")
        addColumnSafe(db, table, "sync_version", "INTEGER NOT NULL DEFAULT 0")
        addColumnSafe(db, table, "last_synced_at", "TEXT")
    }
    // UserEntity gets the same minus last_synced_at (already has lastSyncedAt)
    addColumnSafe(db, "UserEntity", "sync_updated_at", "TEXT")
    addColumnSafe(db, "UserEntity", "is_deleted", "INTEGER NOT NULL DEFAULT 0")
    addColumnSafe(db, "UserEntity", "sync_version", "INTEGER NOT NULL DEFAULT 0")
}

internal fun migrateToVersion13(db: SupportSQLiteDatabase) {
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS CoachPersonaOverrideEntity (
            coachId TEXT PRIMARY KEY NOT NULL,
            userPersona TEXT NOT NULL DEFAULT '',
            updatedAt TEXT NOT NULL,
            sync_updated_at TEXT,
            is_deleted INTEGER NOT NULL DEFAULT 0,
            sync_version INTEGER NOT NULL DEFAULT 0,
            last_synced_at TEXT
        )
        """.trimIndent()
    )
}

internal fun migrateToVersion14(db: SupportSQLiteDatabase) {
    addColumnSafe(db, "GoalEntity", "aiReasoning", "TEXT")
}

internal fun migrateToVersion15(db: SupportSQLiteDatabase) {
    // Create BeginnerObjectiveEntity table (matches migration 13.sqm)
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS BeginnerObjectiveEntity (
            id TEXT PRIMARY KEY NOT NULL,
            objectiveType TEXT NOT NULL,
            isCompleted INTEGER NOT NULL DEFAULT 0,
            completedAt TEXT,
            xpAwarded INTEGER NOT NULL DEFAULT 0,
            createdAt TEXT NOT NULL,
            sync_updated_at TEXT,
            is_deleted INTEGER NOT NULL DEFAULT 0,
            sync_version INTEGER NOT NULL DEFAULT 0,
            last_synced_at TEXT
        )
        """.trimIndent()
    )
}

internal fun migrateToVersion16(db: SupportSQLiteDatabase) {
    // Add type column to HabitEntity (matches migration 18.sqm)
    addColumnSafe(db, "HabitEntity", "type", "TEXT NOT NULL DEFAULT 'BUILD'")

    // Create AbilityEntity table
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS AbilityEntity (
            id TEXT PRIMARY KEY NOT NULL,
            title TEXT NOT NULL,
            description TEXT NOT NULL DEFAULT '',
            iconEmoji TEXT NOT NULL DEFAULT '⚡',
            totalXp INTEGER NOT NULL DEFAULT 0,
            currentLevel INTEGER NOT NULL DEFAULT 1,
            isActive INTEGER NOT NULL DEFAULT 1,
            createdAt TEXT NOT NULL,
            lastActivityDate TEXT,
            sync_updated_at TEXT,
            is_deleted INTEGER NOT NULL DEFAULT 0,
            sync_version INTEGER NOT NULL DEFAULT 0,
            last_synced_at TEXT
        )
        """.trimIndent()
    )

    // Create AbilityHabitLinkEntity table
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS AbilityHabitLinkEntity (
            id TEXT PRIMARY KEY NOT NULL,
            abilityId TEXT NOT NULL,
            habitId TEXT NOT NULL,
            xpWeight REAL NOT NULL DEFAULT 1.0,
            createdAt TEXT NOT NULL,
            FOREIGN KEY (abilityId) REFERENCES AbilityEntity(id) ON DELETE CASCADE,
            FOREIGN KEY (habitId) REFERENCES HabitEntity(id) ON DELETE CASCADE
        )
        """.trimIndent()
    )

    db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS idx_ability_habit_unique ON AbilityHabitLinkEntity(abilityId, habitId)")
}

internal fun migrateToVersion17(db: SupportSQLiteDatabase) {
    // Create AbilityGoalLinkEntity table (matches migration 19.sqm)
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS AbilityGoalLinkEntity (
            id TEXT PRIMARY KEY NOT NULL,
            abilityId TEXT NOT NULL,
            goalId TEXT NOT NULL,
            createdAt TEXT NOT NULL,
            FOREIGN KEY (abilityId) REFERENCES AbilityEntity(id) ON DELETE CASCADE,
            FOREIGN KEY (goalId) REFERENCES GoalEntity(id) ON DELETE CASCADE
        )
        """.trimIndent()
    )
    db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS idx_ability_goal_unique ON AbilityGoalLinkEntity(abilityId, goalId)")
}

internal fun migrateToVersion18(db: SupportSQLiteDatabase) {
    // Create DayRecapEntity table (matches migration 20.sqm)
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS DayRecapEntity (
            date TEXT PRIMARY KEY NOT NULL,
            recap TEXT NOT NULL,
            generatedAt TEXT NOT NULL
        )
        """.trimIndent()
    )
}

internal fun migrateToVersion19(db: SupportSQLiteDatabase) {
    // Rename categories 7→6: drop FAMILY (→PEOPLE), rename remaining to plain words
    listOf("GoalEntity", "HabitEntity").forEach { table ->
        db.execSQL("UPDATE $table SET category = 'MONEY'     WHERE category = 'FINANCIAL'")
        db.execSQL("UPDATE $table SET category = 'BODY'      WHERE category = 'PHYSICAL'")
        db.execSQL("UPDATE $table SET category = 'PEOPLE'    WHERE category = 'SOCIAL'")
        db.execSQL("UPDATE $table SET category = 'PEOPLE'    WHERE category = 'FAMILY'")
        db.execSQL("UPDATE $table SET category = 'WELLBEING' WHERE category = 'EMOTIONAL'")
        db.execSQL("UPDATE $table SET category = 'PURPOSE'   WHERE category = 'SPIRITUAL'")
    }
}

internal fun migrateToVersion20(db: SupportSQLiteDatabase) {
    // Add unit column to HabitEntity for storing numeric target units (e.g. "L", "min", "times")
    addColumnSafe(db, "HabitEntity", "unit", "TEXT")
}

internal fun migrateToVersion21(db: SupportSQLiteDatabase) {
    // Create CachedPersonaEntity table (matches migration 23.sqm)
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS CachedPersonaEntity (
            id TEXT NOT NULL PRIMARY KEY,
            name TEXT NOT NULL,
            description TEXT,
            style TEXT,
            image_url TEXT,
            is_active INTEGER NOT NULL DEFAULT 1,
            fetched_at TEXT NOT NULL
        )
        """.trimIndent()
    )
}

internal fun migrateToVersion22(db: SupportSQLiteDatabase) {
    // Add count column to HabitCheckInEntity for multi-count habits (matches migration 24.sqm)
    addColumnSafe(db, "HabitCheckInEntity", "count", "INTEGER NOT NULL DEFAULT 0")
}

internal fun migrateToVersion23(db: SupportSQLiteDatabase) {
    // Extend CachedPersonaEntity with rich TribeBot persona fields (matches migration 25.sqm)
    addColumnSafe(db, "CachedPersonaEntity", "title", "TEXT NOT NULL DEFAULT 'Coach'")
    addColumnSafe(db, "CachedPersonaEntity", "category", "TEXT NOT NULL DEFAULT 'WELLBEING'")
    addColumnSafe(db, "CachedPersonaEntity", "emoji", "TEXT NOT NULL DEFAULT '✨'")
    addColumnSafe(db, "CachedPersonaEntity", "greeting", "TEXT")
    addColumnSafe(db, "CachedPersonaEntity", "specialties", "TEXT")
    addColumnSafe(db, "CachedPersonaEntity", "personality", "TEXT")
    addColumnSafe(db, "CachedPersonaEntity", "avatar_bg_color", "TEXT NOT NULL DEFAULT '#6366F1'")
    addColumnSafe(db, "CachedPersonaEntity", "avatar_accent_color", "TEXT NOT NULL DEFAULT '#818CF8'")
    addColumnSafe(db, "CachedPersonaEntity", "avatar_icon_name", "TEXT NOT NULL DEFAULT 'star'")
    addColumnSafe(db, "CachedPersonaEntity", "bio", "TEXT")
    addColumnSafe(db, "CachedPersonaEntity", "fun_fact", "TEXT")
    addColumnSafe(db, "CachedPersonaEntity", "xp_to_unlock", "INTEGER NOT NULL DEFAULT 0")
    addColumnSafe(db, "CachedPersonaEntity", "is_default_unlocked", "INTEGER NOT NULL DEFAULT 1")
    addColumnSafe(db, "CachedPersonaEntity", "timezone", "TEXT")
    addColumnSafe(db, "CachedPersonaEntity", "city", "TEXT")
    addColumnSafe(db, "CachedPersonaEntity", "country_flag", "TEXT")
}

internal fun migrateToVersion24(db: SupportSQLiteDatabase) {
    // Add slug and avatar_url to CachedPersonaEntity (matches migration 26.sqm)
    addColumnSafe(db, "CachedPersonaEntity", "slug", "TEXT")
    addColumnSafe(db, "CachedPersonaEntity", "avatar_url", "TEXT")
}

internal fun migrateToVersion26(db: SupportSQLiteDatabase) {
    // Add new columns to UserActivityPatternEntity for richer behavioral tracking
    addColumnSafe(db, "UserActivityPatternEntity", "featureEngagementJson", "TEXT NOT NULL DEFAULT '{}'")
    addColumnSafe(db, "UserActivityPatternEntity", "sessionAvgMinutes", "REAL NOT NULL DEFAULT 0.0")
    addColumnSafe(db, "UserActivityPatternEntity", "sessionsPerWeek", "REAL NOT NULL DEFAULT 0.0")

    // Create ScreenTimeEventEntity for raw behavioral event log
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS ScreenTimeEventEntity (
            id TEXT PRIMARY KEY NOT NULL,
            screen TEXT NOT NULL,
            eventType TEXT NOT NULL,
            timestamp TEXT NOT NULL,
            durationMs INTEGER NOT NULL DEFAULT 0,
            date TEXT NOT NULL
        )
        """.trimIndent()
    )
    db.execSQL("CREATE INDEX IF NOT EXISTS idx_screen_event_date ON ScreenTimeEventEntity(date)")
    db.execSQL("CREATE INDEX IF NOT EXISTS idx_screen_event_screen ON ScreenTimeEventEntity(screen)")
}

internal fun migrateToVersion25(db: SupportSQLiteDatabase) {
    // Create UserSituationEntity table (matches migration 27.sqm)
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS UserSituationEntity (
            id TEXT PRIMARY KEY NOT NULL,
            meta_json TEXT NOT NULL DEFAULT '{}',
            career_json TEXT NOT NULL DEFAULT '{}',
            money_json TEXT NOT NULL DEFAULT '{}',
            body_json TEXT NOT NULL DEFAULT '{}',
            people_json TEXT NOT NULL DEFAULT '{}',
            purpose_json TEXT NOT NULL DEFAULT '{}',
            last_updated_by TEXT NOT NULL DEFAULT '',
            sync_updated_at TEXT,
            is_deleted INTEGER NOT NULL DEFAULT 0,
            sync_version INTEGER NOT NULL DEFAULT 0,
            last_synced_at TEXT
        )
        """.trimIndent()
    )
}

internal fun migrateToVersion29(db: SupportSQLiteDatabase) {
    // Schema v29: LifeValueEntity table (Pillar 1), matches migration 29.sqm
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS LifeValueEntity (
            id TEXT NOT NULL PRIMARY KEY,
            title TEXT NOT NULL,
            description TEXT NOT NULL DEFAULT '',
            isActive INTEGER NOT NULL DEFAULT 1,
            sortOrder INTEGER NOT NULL DEFAULT 0,
            sync_updated_at TEXT,
            is_deleted INTEGER NOT NULL DEFAULT 0,
            sync_version INTEGER NOT NULL DEFAULT 0,
            last_synced_at TEXT
        )
        """.trimIndent()
    )
    db.execSQL("CREATE INDEX IF NOT EXISTS idx_life_value_active ON LifeValueEntity(isActive, is_deleted)")
}

internal fun migrateToVersion30(db: SupportSQLiteDatabase) {
    // Schema v30: GoalEntity.valueId (Pillar 1 Why-Chain), matches migration 30.sqm
    addColumnSafe(db, "GoalEntity", "valueId", "TEXT")
}

internal fun migrateToVersion31(db: SupportSQLiteDatabase) {
    // Schema v31: DecisionEntity table (Pillar 3), matches migration 31.sqm
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS DecisionEntity (
            id TEXT NOT NULL PRIMARY KEY,
            question TEXT NOT NULL,
            optionsConsidered TEXT NOT NULL DEFAULT '',
            chosenOption TEXT NOT NULL,
            reasoning TEXT NOT NULL DEFAULT '',
            relatedGoalId TEXT,
            expectedOutcome TEXT NOT NULL DEFAULT '',
            confidence INTEGER NOT NULL DEFAULT 50,
            decidedAt TEXT NOT NULL,
            actualOutcome TEXT,
            outcomeReviewedAt TEXT,
            outcomeQuality TEXT,
            sync_updated_at TEXT,
            is_deleted INTEGER NOT NULL DEFAULT 0,
            sync_version INTEGER NOT NULL DEFAULT 0,
            last_synced_at TEXT
        )
        """.trimIndent()
    )
    db.execSQL("CREATE INDEX IF NOT EXISTS idx_decision_goal ON DecisionEntity(relatedGoalId)")
}

internal fun migrateToVersion32(db: SupportSQLiteDatabase) {
    // Schema v32: Goal.predictedDueDate + Milestone.estimatedEffort (Pillar 4 Causal Model), matches migration 32.sqm
    addColumnSafe(db, "GoalEntity", "predictedDueDate", "TEXT")
    addColumnSafe(db, "MilestoneEntity", "estimatedEffort", "INTEGER")
}

internal fun migrateToVersion33(db: SupportSQLiteDatabase) {
    // Schema v33: IdentityStatementEntity table (Pillar 5, "I'm becoming someone who…"), matches migration 33.sqm
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS IdentityStatementEntity (
            id TEXT NOT NULL PRIMARY KEY,
            statement TEXT NOT NULL,
            valueId TEXT,
            isActive INTEGER NOT NULL DEFAULT 1,
            sortOrder INTEGER NOT NULL DEFAULT 0,
            createdAt TEXT NOT NULL,
            sync_updated_at TEXT,
            is_deleted INTEGER NOT NULL DEFAULT 0,
            sync_version INTEGER NOT NULL DEFAULT 0,
            last_synced_at TEXT
        )
        """.trimIndent()
    )
    db.execSQL("CREATE INDEX IF NOT EXISTS idx_identity_statement_value ON IdentityStatementEntity(valueId)")
}

internal fun migrateToVersion34(db: SupportSQLiteDatabase) {
    // Schema v34: DecisionProfileEntity table (Pillar 7, the user's six Innate "wiring" dials), matches migration 34.sqm
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS DecisionProfileEntity (
            id TEXT NOT NULL PRIMARY KEY,
            confidenceThresholdValue REAL NOT NULL DEFAULT 0.5,
            confidenceThresholdConfidence REAL NOT NULL DEFAULT 0.0,
            confidenceThresholdSamples INTEGER NOT NULL DEFAULT 0,
            noveltySalienceValue REAL NOT NULL DEFAULT 0.5,
            noveltySalienceConfidence REAL NOT NULL DEFAULT 0.0,
            noveltySalienceSamples INTEGER NOT NULL DEFAULT 0,
            delayDiscountingValue REAL NOT NULL DEFAULT 0.5,
            delayDiscountingConfidence REAL NOT NULL DEFAULT 0.0,
            delayDiscountingSamples INTEGER NOT NULL DEFAULT 0,
            punishmentSensitivityValue REAL NOT NULL DEFAULT 0.5,
            punishmentSensitivityConfidence REAL NOT NULL DEFAULT 0.0,
            punishmentSensitivitySamples INTEGER NOT NULL DEFAULT 0,
            rewardSensitivityValue REAL NOT NULL DEFAULT 0.5,
            rewardSensitivityConfidence REAL NOT NULL DEFAULT 0.0,
            rewardSensitivitySamples INTEGER NOT NULL DEFAULT 0,
            riskAversionValue REAL NOT NULL DEFAULT 0.5,
            riskAversionConfidence REAL NOT NULL DEFAULT 0.0,
            riskAversionSamples INTEGER NOT NULL DEFAULT 0,
            sync_updated_at TEXT,
            is_deleted INTEGER NOT NULL DEFAULT 0,
            sync_version INTEGER NOT NULL DEFAULT 0,
            last_synced_at TEXT
        )
        """.trimIndent()
    )
}

internal fun migrateToVersion35(db: SupportSQLiteDatabase) {
    // Schema v35: health-to-habit auto-complete linkage, matches migration 35.sqm
    addColumnSafe(db, "HabitEntity", "healthMetricType", "TEXT")
    addColumnSafe(db, "HabitEntity", "healthTarget", "REAL")
}
