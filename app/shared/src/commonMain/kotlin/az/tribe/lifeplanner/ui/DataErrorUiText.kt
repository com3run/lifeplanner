package az.tribe.lifeplanner.ui

import az.tribe.lifeplanner.domain.model.DataError
import leanlifeplanner.app.shared.generated.resources.Res
import leanlifeplanner.app.shared.generated.resources.error_authorization
import leanlifeplanner.app.shared.generated.resources.error_disk_full
import leanlifeplanner.app.shared.generated.resources.error_no_internet
import leanlifeplanner.app.shared.generated.resources.error_request_timeout
import leanlifeplanner.app.shared.generated.resources.error_serialization
import leanlifeplanner.app.shared.generated.resources.error_too_many_requests
import leanlifeplanner.app.shared.generated.resources.error_unknown

/** Maps every shared data failure to localized copy. Feature-specific errors get their own mapping. */
fun DataError.toUiText(): UiText = when (this) {
    DataError.Network.NO_INTERNET -> UiText.StringResource(Res.string.error_no_internet)
    DataError.Network.REQUEST_TIMEOUT -> UiText.StringResource(Res.string.error_request_timeout)
    DataError.Network.UNAUTHORIZED,
    DataError.Network.FORBIDDEN -> UiText.StringResource(Res.string.error_authorization)
    DataError.Network.TOO_MANY_REQUESTS -> UiText.StringResource(Res.string.error_too_many_requests)
    DataError.Network.SERIALIZATION -> UiText.StringResource(Res.string.error_serialization)
    DataError.Local.DISK_FULL -> UiText.StringResource(Res.string.error_disk_full)
    DataError.Network.BAD_REQUEST,
    DataError.Network.NOT_FOUND,
    DataError.Network.CONFLICT,
    DataError.Network.PAYLOAD_TOO_LARGE,
    DataError.Network.SERVER_ERROR,
    DataError.Network.SERVICE_UNAVAILABLE,
    DataError.Network.UNKNOWN,
    DataError.Local.NOT_FOUND,
    DataError.Local.UNKNOWN -> UiText.StringResource(Res.string.error_unknown)
}
