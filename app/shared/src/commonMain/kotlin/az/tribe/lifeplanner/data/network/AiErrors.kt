package az.tribe.lifeplanner.data.network

/**
 * Raised when the AI proxy rejects the request because there is no valid user session.
 *
 * The `ai-proxy` edge function requires a real Supabase user JWT (`supabase/functions/ai-proxy
 * /index.ts`, "Validate auth: require a valid Supabase user session"). It answers 401 both when the
 * client sends the anon key instead of a user token ("Session expired") and when no user id can be
 * parsed from the JWT ("Invalid token"). A guest has no JWT at all, so **every** AI call fails for
 * guests.
 *
 * This is a distinct type because the failure is not a connectivity problem, and telling the user
 * to "check your connection" sends them to fix something that is not broken. See
 * [toUserFacingAiMessage].
 */
class AiAuthRequiredException(message: String) : Exception(message)

/**
 * Map a failure from an AI call to something worth showing a user.
 *
 * [action] names what was being attempted, lowercase and without punctuation, for example
 * "generate questions". It is only used for the non-auth case, since the auth message needs to talk
 * about signing in rather than about the failed action.
 */
fun Throwable?.toUserFacingAiMessage(action: String): String = when (this) {
    is AiAuthRequiredException -> "Sign in to use AI features. They need an account, so they are not available in guest mode."
    else -> "Couldn't $action. Check your connection and try again."
}
