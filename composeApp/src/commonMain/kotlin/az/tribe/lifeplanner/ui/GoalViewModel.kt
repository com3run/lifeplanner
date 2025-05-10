package az.tribe.lifeplanner.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.tribe.lifeplanner.domain.Goal
import az.tribe.lifeplanner.domain.GoalCategory
import az.tribe.lifeplanner.domain.GoalStatus
import az.tribe.lifeplanner.domain.GoalTimeline
import az.tribe.lifeplanner.usecases.CreateGoalUseCase
import az.tribe.lifeplanner.usecases.DeleteGoalUseCase
import az.tribe.lifeplanner.usecases.GetAllGoalsUseCase
import az.tribe.lifeplanner.usecases.GetGoalsByCategoryUseCase
import az.tribe.lifeplanner.usecases.GetGoalsByTimelineUseCase
import az.tribe.lifeplanner.usecases.UpdateGoalUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

class GoalViewModel(
    private val getAllGoalsUseCase: GetAllGoalsUseCase,
    private val deleteGoalUseCase: DeleteGoalUseCase,
    private val getGoalsByTimelineUseCase: GetGoalsByTimelineUseCase,
    private val getGoalsByCategoryUseCase: GetGoalsByCategoryUseCase,
    private val createGoalUseCase: CreateGoalUseCase,
    private val updateGoalUseCase: UpdateGoalUseCase,
) : ViewModel() {

    private val _goals = MutableStateFlow<List<Goal>>(emptyList())
    val goals: StateFlow<List<Goal>> = _goals.asStateFlow()


    init {
//        deleteAllGoals()
        loadAllGoals()
    }


    fun loadAllGoals() {
        viewModelScope.launch {

            val result = getAllGoalsUseCase() ?: emptyList()
            _goals.value = result
            if (result.isEmpty()) {
                createGoalUseCase(dummyBusinessGoals())
                _goals.value = getAllGoalsUseCase()
            }
        }
    }

    fun getGoalById(id: String): Goal? {
        return _goals.value.find { it.id == id }
    }

    fun updateGoal(goal: Goal) {
        viewModelScope.launch {
            // Assuming there's a corresponding use case; replace with repository if needed
            updateGoalUseCase(goal) // using the same use case for simplicity
            loadAllGoals()
        }
    }

    fun deleteAllGoals() {
        viewModelScope.launch {
            deleteGoalUseCase.invoke()
            _goals.value = emptyList()
        }
    }

    fun loadGoalsByTimeline(timeline: GoalTimeline) {
        viewModelScope.launch {
            val goals = getGoalsByTimelineUseCase(timeline)
            println("🔄 Loaded ${goals.size} goals for timeline: $timeline")
            _goals.value = goals
        }
    }

    fun loadGoalsByCategory(category: GoalCategory) {
        viewModelScope.launch {
            _goals.value = getGoalsByCategoryUseCase(category)
        }
    }

    fun createGoal(goal: Goal) {
        viewModelScope.launch {
            createGoalUseCase(goal)
            loadAllGoals() // refresh list
        }
    }

    fun deleteGoal(id: String) {
        viewModelScope.launch {
            // Assuming delete functionality is implemented in the repository or use case
            deleteGoalUseCase(id)
            loadAllGoals()
        }
    }


    fun dummyBusinessGoals() = listOf(
        Goal(
            "1",
            GoalCategory.CAREER,
            "Lead quarterly strategy meeting",
            "Prepare and lead Q2 strategy session with department heads",
            GoalStatus.IN_PROGRESS,
            GoalTimeline.SHORT_TERM,
            LocalDate.parse("2025-06-01"),
            emptyList()
        ),
        Goal(
            "2",
            GoalCategory.FINANCIAL,
            "Increase sales by 15%",
            "Execute new sales strategy to boost quarterly revenue",
            GoalStatus.NOT_STARTED,
            GoalTimeline.MID_TERM,
            LocalDate.parse("2025-08-01"),
            emptyList()
        ),
        Goal(
            "3",
            GoalCategory.CAREER,
            "Mentor a junior colleague",
            "Provide weekly mentoring sessions to guide a junior team member",
            GoalStatus.IN_PROGRESS,
            GoalTimeline.LONG_TERM,
            LocalDate.parse("2025-12-15"),
            emptyList()
        ),
        Goal(
            "4",
            GoalCategory.PHYSICAL,
            "Establish morning routine",
            "Wake up at 6 AM and exercise for 30 minutes",
            GoalStatus.IN_PROGRESS,
            GoalTimeline.SHORT_TERM,
            LocalDate.parse("2025-05-30"),
            emptyList()
        ),
        Goal(
            "5",
            GoalCategory.FINANCIAL,
            "Build emergency fund",
            "Save $5,000 for unexpected expenses",
            GoalStatus.NOT_STARTED,
            GoalTimeline.LONG_TERM,
            LocalDate.parse("2025-12-31"),
            emptyList()
        ),
        Goal(
            "6",
            GoalCategory.CAREER,
            "Improve public speaking",
            "Attend Toastmasters weekly to boost presentation skills",
            GoalStatus.IN_PROGRESS,
            GoalTimeline.MID_TERM,
            LocalDate.parse("2025-09-15"),
            emptyList()
        ),
        Goal(
            "7",
            GoalCategory.SOCIAL,
            "Attend 3 networking events",
            "Expand professional connections via industry meetups",
            GoalStatus.NOT_STARTED,
            GoalTimeline.SHORT_TERM,
            LocalDate.parse("2025-06-30"),
            emptyList()
        ),
        Goal(
            "8",
            GoalCategory.CAREER,
            "Launch internal training",
            "Develop and run a skills training session for staff",
            GoalStatus.NOT_STARTED,
            GoalTimeline.MID_TERM,
            LocalDate.parse("2025-07-15"),
            emptyList()
        ),
        Goal(
            "9",
            GoalCategory.EMOTIONAL,
            "Practice work-life balance",
            "Log off by 6PM every day to reduce burnout",
            GoalStatus.IN_PROGRESS,
            GoalTimeline.LONG_TERM,
            LocalDate.parse("2025-12-31"),
            emptyList()
        ),
        Goal(
            "10",
            GoalCategory.FINANCIAL,
            "Optimize monthly expenses",
            "Audit and reduce recurring personal and business costs",
            GoalStatus.COMPLETED,
            GoalTimeline.SHORT_TERM,
            LocalDate.parse("2025-04-01"),
            emptyList()
        ),

        Goal(
            "11",
            GoalCategory.CAREER,
            "Update LinkedIn profile",
            "Revamp resume and achievements on LinkedIn",
            GoalStatus.COMPLETED,
            GoalTimeline.SHORT_TERM,
            LocalDate.parse("2025-04-20"),
            emptyList()
        ),
        Goal(
            "12",
            GoalCategory.SPIRITUAL,
            "Start gratitude journaling",
            "Write down 3 things you're grateful for each night",
            GoalStatus.IN_PROGRESS,
            GoalTimeline.LONG_TERM,
            LocalDate.parse("2025-11-30"),
            emptyList()
        ),
        Goal(
            "13",
            GoalCategory.FAMILY,
            "Plan monthly family weekend",
            "Dedicate 1 weekend per month to family time",
            GoalStatus.NOT_STARTED,
            GoalTimeline.LONG_TERM,
            LocalDate.parse("2025-12-31"),
            emptyList()
        ),
        Goal(
            "14",
            GoalCategory.PHYSICAL,
            "Lose 5 kg",
            "Follow meal plan and exercise to reduce weight",
            GoalStatus.IN_PROGRESS,
            GoalTimeline.MID_TERM,
            LocalDate.parse("2025-08-01"),
            emptyList()
        ),
        Goal(
            "15",
            GoalCategory.CAREER,
            "Get promoted to team lead",
            "Take initiative and complete stretch assignments",
            GoalStatus.NOT_STARTED,
            GoalTimeline.LONG_TERM,
            LocalDate.parse("2025-12-15"),
            emptyList()
        ),
        Goal(
            "16",
            GoalCategory.SOCIAL,
            "Organize office happy hour",
            "Foster team bonding by planning a casual meetup",
            GoalStatus.COMPLETED,
            GoalTimeline.SHORT_TERM,
            LocalDate.parse("2025-05-01"),
            emptyList()
        ),
        Goal(
            "17",
            GoalCategory.EMOTIONAL,
            "Meditate daily",
            "Meditate 10 minutes every morning",
            GoalStatus.IN_PROGRESS,
            GoalTimeline.LONG_TERM,
            LocalDate.parse("2025-12-31"),
            emptyList()
        ),
        Goal(
            "18",
            GoalCategory.FINANCIAL,
            "Launch side hustle",
            "Start consulting or freelance gig",
            GoalStatus.NOT_STARTED,
            GoalTimeline.LONG_TERM,
            LocalDate.parse("2025-11-01"),
            emptyList()
        ),
        Goal(
            "19",
            GoalCategory.CAREER,
            "Attend international conference",
            "Travel and attend an industry-leading event",
            GoalStatus.NOT_STARTED,
            GoalTimeline.LONG_TERM,
            LocalDate.parse("2025-10-10"),
            emptyList()
        ),
        Goal(
            "20",
            GoalCategory.PHYSICAL,
            "Walk 10,000 steps daily",
            "Maintain fitness through consistent activity",
            GoalStatus.IN_PROGRESS,
            GoalTimeline.MID_TERM,
            LocalDate.parse("2025-07-01"),
            emptyList()
        )
    )

}