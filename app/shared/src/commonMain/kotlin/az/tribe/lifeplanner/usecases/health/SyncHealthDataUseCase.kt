package az.tribe.lifeplanner.usecases.health

import az.tribe.lifeplanner.domain.repository.HealthRepository

class SyncHealthDataUseCase(
    private val repository: HealthRepository,
    private val autoCompleteHealthHabits: AutoCompleteHealthHabitsUseCase,
) {
    suspend operator fun invoke() {
        repository.syncFromPlatform()
        // Fresh data may satisfy linked habit targets; failures must not break the sync.
        runCatching { autoCompleteHealthHabits() }
    }
}
