package az.tribe.lifeplanner.domain.service

import az.tribe.lifeplanner.domain.model.ScoreSource
import az.tribe.lifeplanner.domain.model.WheelArea
import az.tribe.lifeplanner.domain.model.WheelReport

/**
 * Which area to offer help with, and whether to offer any at all.
 *
 * Separated from the content of a suggestion on purpose. Picking the area is a judgement about the
 * user's situation and is worth testing; the words are a writing problem that will change often.
 *
 * The bar for saying nothing is deliberately high. A prompt that appears every time you open a
 * screen stops being help and becomes furniture you learn to ignore, and the areas here are ones
 * people already feel bad about. Silence is the right answer more often than not.
 */
object WheelNudgePicker {

    /** At or above this, an area is fine and does not want advice. */
    private const val COMFORTABLE = 6.5

    /** Below this, the area is in real difficulty. */
    private const val STRUGGLING = 4.0

    /**
     * The area worth a suggestion, or null to stay quiet.
     *
     * Null when: nothing is measured yet, everything is comfortable, or the wheel is so uniformly
     * low that singling one area out would be arbitrary. That last case matters — someone whose
     * whole wheel sits at 3 does not need to be told their Friends score is the problem.
     */
    fun pick(report: WheelReport): WheelArea? {
        // Only areas we actually know about. Nudging someone about a placeholder 5 we invented
        // would be advising them on a number they never gave us.
        val known = report.segments.filter { it.source != ScoreSource.ESTIMATED }
        if (known.isEmpty()) return null

        val weakest = known.minByOrNull { it.score } ?: return null
        if (weakest.score >= COMFORTABLE) return null

        // A flat wheel has no weakest area in any meaningful sense, only a lowest one. If the
        // spread is tiny the pick is noise, and acting on noise is how an app starts feeling
        // arbitrary. This holds at every level: the old guard switched off below STRUGGLING,
        // so a wheel of all 3s crowned an arbitrary area, which is exactly the case the doc
        // above promises not to do. Uniformly low is a hard time, not a Friends problem.
        val spread = known.maxOf { it.score } - known.minOf { it.score }
        if (spread < 1.5) return null

        return weakest.area
    }

    /**
     * How hard the suggestion should push, which the copy can use to pick its register.
     *
     * An area at 6 wants a light touch. An area at 2 does not want a cheerful tip, and offering
     * one is how an app reads as not paying attention.
     */
    fun urgency(report: WheelReport, area: WheelArea): NudgeUrgency {
        val score = report.segments.firstOrNull { it.area == area }?.score ?: return NudgeUrgency.GENTLE
        return when {
            score < STRUGGLING -> NudgeUrgency.SERIOUS
            score < 5.5 -> NudgeUrgency.MODERATE
            else -> NudgeUrgency.GENTLE
        }
    }
}

enum class NudgeUrgency { GENTLE, MODERATE, SERIOUS }
