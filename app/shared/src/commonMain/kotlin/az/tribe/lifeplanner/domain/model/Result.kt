package az.tribe.lifeplanner.domain.model

/**
 * Marker for typed failures. Every error carried by a [Result] implements it, whether it is a
 * shared [DataError] or a feature-specific enum such as a validation error.
 */
interface Error

/**
 * A typed success-or-failure outcome that works across every layer: data, domain, presentation,
 * validation. Expected failures are returned as [Result.Error] rather than thrown, so callers see
 * the exact error type in the signature and never catch raw exceptions for the expected case.
 */
sealed interface Result<out D, out E : Error> {
    data class Success<out D>(val data: D) : Result<D, Nothing>
    data class Error<out E : az.tribe.lifeplanner.domain.model.Error>(val error: E) : Result<Nothing, E>
}

typealias EmptyResult<E> = Result<Unit, E>

inline fun <T, E : Error, R> Result<T, E>.map(map: (T) -> R): Result<R, E> {
    return when (this) {
        is Result.Error -> Result.Error(error)
        is Result.Success -> Result.Success(map(data))
    }
}

inline fun <T, E : Error, R> Result<T, E>.flatMap(transform: (T) -> Result<R, E>): Result<R, E> {
    return when (this) {
        is Result.Error -> this
        is Result.Success -> transform(data)
    }
}

inline fun <T, E : Error> Result<T, E>.onSuccess(action: (T) -> Unit): Result<T, E> {
    return when (this) {
        is Result.Error -> this
        is Result.Success -> {
            action(data)
            this
        }
    }
}

inline fun <T, E : Error> Result<T, E>.onFailure(action: (E) -> Unit): Result<T, E> {
    return when (this) {
        is Result.Error -> {
            action(error)
            this
        }
        is Result.Success -> this
    }
}

fun <T, E : Error> Result<T, E>.asEmptyResult(): EmptyResult<E> {
    return map { }
}

/** The success value, or null when this is a failure. Handy at the edge where a null is acceptable. */
fun <T, E : Error> Result<T, E>.getOrNull(): T? = (this as? Result.Success)?.data
