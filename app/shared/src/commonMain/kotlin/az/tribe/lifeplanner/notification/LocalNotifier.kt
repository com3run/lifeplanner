package az.tribe.lifeplanner.notification

/**
 * Shows a local notification immediately (no scheduling), e.g. "habit auto-completed from
 * health data". Safe to call from any coroutine; failures are logged, never thrown.
 */
expect fun notifyNow(id: String, title: String, message: String)
