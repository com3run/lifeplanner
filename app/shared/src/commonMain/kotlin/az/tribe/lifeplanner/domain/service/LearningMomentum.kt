package az.tribe.lifeplanner.domain.service

import az.tribe.lifeplanner.domain.enum.BadgeType
import az.tribe.lifeplanner.domain.model.XpRewards

/**
 * A learn session, described as something already under way.
 *
 * The library was a band at the bottom of the feed: three lessons offered daily, each one a
 * self-contained card that could be read or scrolled past with equal consequence. Nothing on the
 * screen said the user was three lessons into a six lesson path, or that two more would finish it,
 * so there was nothing to come back for.
 *
 * This is the missing part, and it is all a reader needs to feel the pull: where they are, how much
 * is left, what the next lesson is called, and what finishing is worth. The numbers are the real
 * ones, which is the whole point. Progress you cannot see does not motivate, and progress that is
 * exaggerated stops being believed the first time the user counts for themselves.
 */
object LearningMomentum {

    /**
     * Where the reader is on the path. The copy changes with it, because the same sentence at the
     * start and at the end is a sentence that is not paying attention: at the beginning what matters
     * is how small the first step is, and at the end it is how close the finish is.
     */
    enum class Stage {
        /** Nothing read in this path yet. */
        OPENING,

        /** Under way, not yet half. */
        UNDER_WAY,

        /** Half or more of the path is read. */
        HALFWAY,

        /** One or two lessons left after this one. */
        CLOSING,

        /** This is the last unread lesson in the path. */
        LAST,
    }

    data class State(
        val lessonId: String,
        val lessonTitle: String,
        val lessonEmoji: String,
        val readMinutes: Int,
        val pathTitle: String,
        val pathEmoji: String,
        /** 1-based position of the lesson about to be read, matching the hub's "3 of 7". */
        val position: Int,
        /** Every lesson in the path, including any still locked. */
        val total: Int,
        /** Lessons of the path already read. */
        val read: Int,
        /** Unread lessons the user can open today, after this one. */
        val readableAfterThis: Int,
        /** Lessons of the path still above the user's level, so out of reach for now. */
        val lockedAhead: Int,
        /** The lesson after this one, named so there is something to come back for. */
        val upNextTitle: String?,
        /** True when [upNextTitle] belongs to the next path rather than this one. */
        val upNextStartsNewPath: Boolean,
        /**
         * The badge finishing the path earns, and only when it is actually reachable: a promise the
         * user cannot act on is worse than no promise (see [KnowledgeLibrary.badgeFor]).
         */
        val badge: BadgeType?,
        /** XP this lesson pays, shown before it is read rather than announced after. */
        val xp: Int,
        val stage: Stage,
        /** One line, true of this position on the path. */
        val line: String,
    )

    /**
     * @param level the user's gamification level, which gates what they can open.
     * @param readIds every lesson already read.
     * @return null when there is nothing left to read at this level, in which case the screen says
     *   nothing rather than inventing a next step.
     */
    fun of(level: Int, readIds: Set<String>): State? {
        val resume = KnowledgeLibrary.resumePoint(level, readIds) ?: return null
        val lessons = KnowledgeLibrary.lessonsOf(resume.path)
        val unlocked = lessons.filter { it.minLevel <= level }
        val unread = unlocked.filter { it.id !in readIds }
        val readableAfterThis = (unread.size - 1).coerceAtLeast(0)
        val lockedAhead = lessons.size - unlocked.size

        val nextInPath = unread.getOrNull(1)
        val nextAnywhere = nextInPath
            ?: KnowledgeLibrary.nextAfter(resume.lesson.id, readIds, level)

        // A badge is only mentioned when the path can be cleared now. With a lesson still locked,
        // "two more and it is yours" is a sentence the app cannot honour.
        val badge = KnowledgeLibrary.badgeFor(resume.path.id)?.takeIf { lockedAhead == 0 }

        val stage = when {
            resume.readInPath == 0 -> Stage.OPENING
            readableAfterThis == 0 -> Stage.LAST
            readableAfterThis <= 2 -> Stage.CLOSING
            resume.readInPath * 2 >= resume.totalInPath -> Stage.HALFWAY
            else -> Stage.UNDER_WAY
        }

        return State(
            lessonId = resume.lesson.id,
            lessonTitle = resume.lesson.title,
            lessonEmoji = resume.lesson.emoji,
            readMinutes = resume.lesson.readMin,
            pathTitle = resume.path.title,
            pathEmoji = resume.path.emoji,
            position = resume.readInPath + 1,
            total = resume.totalInPath,
            read = resume.readInPath,
            readableAfterThis = readableAfterThis,
            lockedAhead = lockedAhead,
            upNextTitle = nextAnywhere?.title,
            upNextStartsNewPath = nextInPath == null && nextAnywhere != null,
            badge = badge,
            xp = XpRewards.LESSON_READ,
            stage = stage,
            line = line(stage, resume, readableAfterThis, badge, lockedAhead),
        )
    }

    private fun line(
        stage: Stage,
        resume: KnowledgeLibrary.LearnResumePoint,
        readableAfterThis: Int,
        badge: BadgeType?,
        lockedAhead: Int,
    ): String = when (stage) {
        // The opening line sells the size of the step, not the size of the path. "Seven lessons"
        // is a commitment; "two minutes" is a decision someone can make right now.
        Stage.OPENING -> "${resume.totalInPath} lessons in this path. This one takes ${resume.lesson.readMin} minutes."

        Stage.UNDER_WAY -> "${resume.readInPath} of ${resume.totalInPath} read. Picking up where you stopped."

        Stage.HALFWAY -> "Past halfway. ${readableAfterThis + 1} to go."

        Stage.CLOSING -> when {
            badge != null -> "${readableAfterThis + 1} more and \"${badge.displayName}\" is yours."
            else -> "${readableAfterThis + 1} more to finish the path."
        }

        Stage.LAST -> when {
            badge != null -> "Last one. Finish it and \"${badge.displayName}\" is yours."
            lockedAhead > 0 -> "Last one you can open. The rest of the path unlocks as you level up."
            else -> "Last one in this path."
        }
    }
}
