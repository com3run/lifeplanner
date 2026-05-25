package az.tribe.lifeplanner.ui.balance

import az.tribe.lifeplanner.domain.model.Story

internal fun getCoachTipStories(): List<Story> = listOf(
    Story(
        id = "coach_tip_luna_balance",
        title = "Balance is a Practice",
        subtitle = "I used to think balance meant giving equal time to everything. It doesn't. It means giving the right energy to the right things. Notice one area that feels heavy today, and lighten it intentionally.\n\n- Luna",
        emoji = "✨",
        category = "coach_tip",
        gradientStart = "#6366F1",
        gradientEnd = "#818CF8",
        ctaText = "Chat with Luna",
        ctaAction = "coach_luna_general",
        label = "Luna"
    ),
    Story(
        id = "coach_tip_alex_career",
        title = "Your Next Move is Closer",
        subtitle = "The best career moves aren't big leaps, they're consistent micro-skills built daily. Set one professional goal this week. Just one. Consistency beats intensity every single time.\n\n- Alex",
        emoji = "💼",
        category = "coach_tip",
        gradientStart = "#1976D2",
        gradientEnd = "#42A5F5",
        ctaText = "Chat with Alex",
        ctaAction = "coach_alex_career",
        label = "Alex"
    ),
    Story(
        id = "coach_tip_morgan_money",
        title = "The 1% Money Rule",
        subtitle = "Save 1% more than you did last month. That's it. Most people try to overhaul their finances at once and burn out. Small, consistent improvements compound into real wealth over time.\n\n- Morgan",
        emoji = "💰",
        category = "coach_tip",
        gradientStart = "#388E3C",
        gradientEnd = "#66BB6A",
        ctaText = "Chat with Morgan",
        ctaAction = "coach_morgan_finance",
        label = "Morgan"
    ),
    Story(
        id = "coach_tip_kai_fitness",
        title = "Movement is Medicine",
        subtitle = "You don't need 2 hours at the gym. 15 minutes of intentional movement beats an hour of dreading it. Start one streak this week and watch your energy shift completely.\n\n- Kai",
        emoji = "💪",
        category = "coach_tip",
        gradientStart = "#E53935",
        gradientEnd = "#EF5350",
        ctaText = "Chat with Kai",
        ctaAction = "coach_kai_fitness",
        label = "Kai"
    ),
    Story(
        id = "coach_tip_sam_social",
        title = "Invest in One Relationship",
        subtitle = "Relationships compound like interest. One genuine message, one real conversation, one act of presence, they matter more than you think. Who haven't you reached out to in a while?\n\n- Sam",
        emoji = "🤝",
        category = "coach_tip",
        gradientStart = "#7B1FA2",
        gradientEnd = "#AB47BC",
        ctaText = "Chat with Sam",
        ctaAction = "coach_sam_social",
        label = "Sam"
    ),
    Story(
        id = "coach_tip_river_wellness",
        title = "Rest is Productive",
        subtitle = "In a world that glorifies hustle, choosing rest is radical. Your brain consolidates learning, processes emotions, and resets energy during downtime. Protect your recovery, it's not optional.\n\n- River",
        emoji = "🧘",
        category = "coach_tip",
        gradientStart = "#00796B",
        gradientEnd = "#26A69A",
        ctaText = "Chat with River",
        ctaAction = "coach_river_wellness",
        label = "River"
    )
)
