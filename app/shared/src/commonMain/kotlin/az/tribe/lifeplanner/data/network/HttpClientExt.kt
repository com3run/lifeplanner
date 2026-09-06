package az.tribe.lifeplanner.data.network

import az.tribe.lifeplanner.data.model.ErrorDto
import az.tribe.lifeplanner.domain.model.DataError
import az.tribe.lifeplanner.domain.model.Result
import co.touchlab.kermit.Logger
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.call.body
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.ensureActive
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlin.coroutines.coroutineContext

/**
 * Runs a Ktor call and turns every expected failure into a typed [DataError.Network]. Cancellation
 * is re-raised through `ensureActive`, so a cancelled coroutine never reads as a network error.
 */
suspend inline fun <reified T> safeCall(
    execute: () -> HttpResponse,
): Result<T, DataError.Network> {
    val response = try {
        execute()
    } catch (e: SocketTimeoutException) {
        return Result.Error(DataError.Network.REQUEST_TIMEOUT)
    } catch (e: UnresolvedAddressException) {
        return Result.Error(DataError.Network.NO_INTERNET)
    } catch (e: SerializationException) {
        return Result.Error(DataError.Network.SERIALIZATION)
    } catch (e: Exception) {
        coroutineContext.ensureActive()
        Logger.e("HttpClientExt") { "Network request failed: ${e.message}" }
        return Result.Error(DataError.Network.UNKNOWN)
    }
    return responseToResult(response)
}

suspend inline fun <reified T> responseToResult(
    response: HttpResponse,
): Result<T, DataError.Network> {
    return when (response.status.value) {
        in 200..299 -> {
            try {
                Result.Success(response.body<T>())
            } catch (e: NoTransformationFoundException) {
                Result.Error(DataError.Network.SERIALIZATION)
            } catch (e: SerializationException) {
                Result.Error(DataError.Network.SERIALIZATION)
            }
        }
        400 -> {
            logServerMessage(response)
            Result.Error(DataError.Network.BAD_REQUEST)
        }
        401 -> Result.Error(DataError.Network.UNAUTHORIZED)
        403 -> Result.Error(DataError.Network.FORBIDDEN)
        404 -> Result.Error(DataError.Network.NOT_FOUND)
        408 -> Result.Error(DataError.Network.REQUEST_TIMEOUT)
        409 -> Result.Error(DataError.Network.CONFLICT)
        413 -> Result.Error(DataError.Network.PAYLOAD_TOO_LARGE)
        429 -> Result.Error(DataError.Network.TOO_MANY_REQUESTS)
        503 -> Result.Error(DataError.Network.SERVICE_UNAVAILABLE)
        in 500..599 -> Result.Error(DataError.Network.SERVER_ERROR)
        else -> Result.Error(DataError.Network.UNKNOWN)
    }
}

/** The proxy sends a sanitized `{ "message": ... }` body on 400; keep it in the log for diagnosis. */
suspend fun logServerMessage(response: HttpResponse) {
    val message = runCatching { Json.decodeFromString<ErrorDto>(response.bodyAsText()).message }.getOrNull()
    if (message != null) Logger.w("HttpClientExt") { "Server rejected request: $message" }
}
