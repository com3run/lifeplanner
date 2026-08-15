package az.tribe.lifeplanner.ui.wheel

import kotlin.test.Test
import kotlin.test.assertTrue

class ScoreColorTest {

    private fun red(s: Double) = scoreColor(s).red
    private fun green(s: Double) = scoreColor(s).green
    private fun blue(s: Double) = scoreColor(s).blue

    @Test
    fun `the ramp runs warm at the bottom and cool at the top`() {
        // A 2 should look nothing like a 9 at a glance, which is the whole point of colouring it.
        assertTrue(red(2.0) > green(2.0), "a 2 should read warm")
        assertTrue(green(9.0) > red(9.0), "a 9 should read cool")
    }

    @Test
    fun `neighbouring scores are visibly different`() {
        // Five hard bands would make 6 and 7 identical, and the bar would jump rather than shift
        // as the user moves across it.
        val six = scoreColor(6.0)
        val seven = scoreColor(7.0)
        val delta = kotlin.math.abs(six.red - seven.red) +
            kotlin.math.abs(six.green - seven.green) +
            kotlin.math.abs(six.blue - seven.blue)

        assertTrue(delta > 0.05f, "6 and 7 are the same colour")
    }

    @Test
    fun `a middling score is not coloured like an alarm`() {
        // A 4 is an ordinary part of an ordinary life. Colouring half the scale red tells someone
        // their life is failing when they were only being honest with us, which is a good way to
        // teach them to stop being honest.
        val four = scoreColor(4.0)
        val one = scoreColor(1.0)

        assertTrue(four.green > one.green, "a 4 is as red as a 1")
    }

    @Test
    fun `the very bottom is unmistakable`() {
        // Reserved for where someone is genuinely struggling, so it still means something.
        assertTrue(red(1.0) > 0.8f && green(1.0) < 0.5f, "a 1 does not read as serious")
    }

    @Test
    fun `out of range scores do not produce nonsense`() {
        // Clamped rather than extrapolated: a stray 0 or 11 should look like the end of the scale,
        // not like a colour from outside it.
        assertTrue(scoreColor(0.0) == scoreColor(1.0))
        assertTrue(scoreColor(11.0) == scoreColor(10.0))
    }

    @Test
    fun `half steps land between their neighbours`() {
        // The wheel stores halves, so 7.5 has to be a real colour rather than snapping to 7.
        val seven = scoreColor(7.0)
        val half = scoreColor(7.5)
        val eight = scoreColor(8.0)

        assertTrue(half != seven && half != eight, "7.5 snapped to a neighbour")
        assertTrue(half.blue > seven.blue && half.blue < eight.blue, "7.5 is off the ramp")
    }
}
