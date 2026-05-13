package az.tribe.lifeplanner.data.analytics

actual object PostHogAnalytics {
    actual fun setup(apiKey: String, host: String) {}
    actual fun identify(userId: String, properties: Map<String, Any>) {}
    actual fun reset() {}
    actual fun capture(event: String, properties: Map<String, Any>) {}
    actual fun setUserProperties(properties: Map<String, Any>) {}
    actual fun screen(screenName: String, properties: Map<String, Any>) {}
    actual fun isFeatureEnabled(flag: String): Boolean = false
    actual fun getFeatureFlag(flag: String): Any? = null
    actual fun reloadFeatureFlags() {}
    actual fun group(type: String, key: String, properties: Map<String, Any>) {}
    actual fun flush() {}
}
