package az.tribe.lifeplanner.domain.service

import az.tribe.lifeplanner.domain.enum.GoalStatus
import az.tribe.lifeplanner.domain.model.Milestone
import az.tribe.lifeplanner.testutil.testGoal
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GoalOptimizerTest {

    private val today = LocalDate(2026, 7, 29)
    private val past = LocalDate(2026, 7, 1)
    private val future = LocalDate(2026, 12, 1)
    private val oldCreated = LocalDateTime(2026, 3, 6, 10, 0, 0) // ~145 days before today
    private val done = Milestone(id = "m1", title = "done", isCompleted = true)
    private val open = Milestone(id = "m2", title = "open", isCompleted = false)

    @Test
    fun allStepsDone_suggestsComplete() {
        val g = testGoal(dueDate = future, milestones = listOf(done, done))
        val s = GoalOptimizer.suggestions(listOf(g), today).single()
        assertEquals(GoalOptimizer.Kind.READY_TO_COMPLETE, s.kind)
    }

    @Test
    fun overdue_suggestsReschedule() {
        val g = testGoal(dueDate = past, completionRate = 0.5f, milestones = listOf(done, open))
        val s = GoalOptimizer.suggestions(listOf(g), today).single()
        assertEquals(GoalOptimizer.Kind.RESCHEDULE_OVERDUE, s.kind)
    }

    @Test
    fun lastStepLeft_suggestsFinish() {
        val g = testGoal(dueDate = future, milestones = listOf(done, done, open))
        val s = GoalOptimizer.suggestions(listOf(g), today).single()
        assertEquals(GoalOptimizer.Kind.FINISH_ALMOST, s.kind)
    }

    @Test
    fun oldAndBarelyStarted_suggestsRefocus() {
        val g = testGoal(dueDate = future, completionRate = 0.1f, createdAt = oldCreated)
        val s = GoalOptimizer.suggestions(listOf(g), today).single()
        assertEquals(GoalOptimizer.Kind.REFOCUS_STALE, s.kind)
    }

    @Test
    fun completedGoal_hasNoSuggestion() {
        val g = testGoal(status = GoalStatus.COMPLETED, dueDate = past, milestones = listOf(done, done))
        assertTrue(GoalOptimizer.suggestions(listOf(g), today).isEmpty())
    }

    @Test
    fun healthyInProgressGoal_hasNoSuggestion() {
        val g = testGoal(dueDate = future, completionRate = 0.5f, createdAt = oldCreated)
        assertTrue(GoalOptimizer.suggestions(listOf(g), today).isEmpty())
    }

    @Test
    fun completeBeatsOverdue_whenBothApply() {
        // Overdue but every step is done -> closing it out takes priority over rescheduling.
        val g = testGoal(dueDate = past, milestones = listOf(done, done))
        val s = GoalOptimizer.suggestions(listOf(g), today).single()
        assertEquals(GoalOptimizer.Kind.READY_TO_COMPLETE, s.kind)
    }

    @Test
    fun respectsLimitAndPriorityOrder() {
        val ready = testGoal(id = "a", dueDate = future, milestones = listOf(done))
        val overdue = testGoal(id = "b", dueDate = past, completionRate = 0.5f)
        val stale = testGoal(id = "c", dueDate = future, completionRate = 0.1f, createdAt = oldCreated)
        val out = GoalOptimizer.suggestions(listOf(stale, overdue, ready), today, limit = 2)
        assertEquals(2, out.size)
        assertEquals(GoalOptimizer.Kind.READY_TO_COMPLETE, out[0].kind)
        assertEquals(GoalOptimizer.Kind.RESCHEDULE_OVERDUE, out[1].kind)
    }
}
