package az.tribe.lifeplanner.data.review

actual class InAppReviewManager {
    actual fun requestReview(trigger: String) {
        // No-op on Meta Quest — Google Play Review not available
    }
}
