package az.tribe.lifeplanner.data.network

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The bug this guards: the AI proxy answers 401 whenever there is no user session (guest mode,
 * expired session), and every caller reported it as "Check your connection", sending users to fix
 * a network that was never broken.
 */
class AiErrorsTest {

    @Test
    fun `auth failure tells the user to sign in, not to check the network`() {
        val message = AiAuthRequiredException("AI proxy auth error 401: {}").toUserFacingAiMessage("generate questions")

        assertTrue(message.contains("Sign in"), "should tell the user to sign in, was: $message")
        assertTrue(
            !message.contains("connection", ignoreCase = true),
            "must not blame the network for an auth failure, was: $message",
        )
    }

    @Test
    fun `non-auth failures still report as a connection problem`() {
        val message = IllegalStateException("AI proxy error 500: boom").toUserFacingAiMessage("generate questions")

        assertEquals("Couldn't generate questions. Check your connection and try again.", message)
    }

    @Test
    fun `action name is interpolated for the non-auth case`() {
        assertTrue(
            IllegalStateException("boom").toUserFacingAiMessage("generate habits").contains("generate habits"),
        )
    }

    @Test
    fun `a null throwable is treated as a generic failure`() {
        val message = (null as Throwable?).toUserFacingAiMessage("generate goal")

        assertEquals("Couldn't generate goal. Check your connection and try again.", message)
    }
}
