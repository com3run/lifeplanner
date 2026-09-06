package az.tribe.lifeplanner.domain.model

/**
 * The shared failure vocabulary for the data layer. Network calls fail with [Network], local
 * storage with [Local]; a repository that coordinates both returns the [DataError] supertype.
 * Map to user-facing copy with `DataError.toUiText()` in the presentation layer.
 */
sealed interface DataError : Error {
    enum class Network : DataError {
        BAD_REQUEST,
        REQUEST_TIMEOUT,
        UNAUTHORIZED,
        FORBIDDEN,
        NOT_FOUND,
        CONFLICT,
        TOO_MANY_REQUESTS,
        NO_INTERNET,
        PAYLOAD_TOO_LARGE,
        SERVER_ERROR,
        SERVICE_UNAVAILABLE,
        SERIALIZATION,
        UNKNOWN,
    }

    enum class Local : DataError {
        DISK_FULL,
        NOT_FOUND,
        UNKNOWN,
    }
}
