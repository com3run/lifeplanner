package az.tribe.lifeplanner.di

import org.koin.dsl.module
import org.koin.test.verify.verify
import kotlin.test.Test

/**
 * Every constructor parameter of every Koin binding must be satisfiable by the graph, at build
 * time rather than on first navigation. Route arguments arrive through `parametersOf`, so their
 * primitive types are declared as extra.
 */
class KoinModulesTest {

    @Test
    fun `every binding in the graph can be resolved`() {
        module { includes(appModules) }.verify(
            extraTypes = listOf(
                // Route arguments and tuning parameters arrive through parametersOf or defaults.
                String::class, Int::class, Long::class, Double::class, Float::class, Boolean::class,
                Function0::class, Function1::class,
                // SyncManager keeps a private test constructor; Koin only ever calls the public one.
                kotlinx.coroutines.flow.Flow::class,
                // HttpClient is built through its DSL, not injected an engine.
                io.ktor.client.engine.HttpClientEngine::class,
            ),
        )
    }
}
