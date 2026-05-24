package az.tribe.lifeplanner.data.mapper

import az.tribe.lifeplanner.database.DecisionProfileEntity
import az.tribe.lifeplanner.domain.model.DecisionProfile
import az.tribe.lifeplanner.domain.model.DialSetting
import kotlin.time.Clock

// SQLDelight maps REAL → Double and INTEGER → Long; DialSetting uses Float / Int.

fun DecisionProfileEntity.toDomain(): DecisionProfile = DecisionProfile(
    id = id,
    confidenceThreshold = DialSetting(confidenceThresholdValue.toFloat(), confidenceThresholdConfidence.toFloat(), confidenceThresholdSamples.toInt()),
    noveltySalience = DialSetting(noveltySalienceValue.toFloat(), noveltySalienceConfidence.toFloat(), noveltySalienceSamples.toInt()),
    delayDiscounting = DialSetting(delayDiscountingValue.toFloat(), delayDiscountingConfidence.toFloat(), delayDiscountingSamples.toInt()),
    punishmentSensitivity = DialSetting(punishmentSensitivityValue.toFloat(), punishmentSensitivityConfidence.toFloat(), punishmentSensitivitySamples.toInt()),
    rewardSensitivity = DialSetting(rewardSensitivityValue.toFloat(), rewardSensitivityConfidence.toFloat(), rewardSensitivitySamples.toInt()),
    riskAversion = DialSetting(riskAversionValue.toFloat(), riskAversionConfidence.toFloat(), riskAversionSamples.toInt()),
)

fun DecisionProfile.toEntity(): DecisionProfileEntity = DecisionProfileEntity(
    id = id,
    confidenceThresholdValue = confidenceThreshold.value.toDouble(),
    confidenceThresholdConfidence = confidenceThreshold.confidence.toDouble(),
    confidenceThresholdSamples = confidenceThreshold.sampleSize.toLong(),
    noveltySalienceValue = noveltySalience.value.toDouble(),
    noveltySalienceConfidence = noveltySalience.confidence.toDouble(),
    noveltySalienceSamples = noveltySalience.sampleSize.toLong(),
    delayDiscountingValue = delayDiscounting.value.toDouble(),
    delayDiscountingConfidence = delayDiscounting.confidence.toDouble(),
    delayDiscountingSamples = delayDiscounting.sampleSize.toLong(),
    punishmentSensitivityValue = punishmentSensitivity.value.toDouble(),
    punishmentSensitivityConfidence = punishmentSensitivity.confidence.toDouble(),
    punishmentSensitivitySamples = punishmentSensitivity.sampleSize.toLong(),
    rewardSensitivityValue = rewardSensitivity.value.toDouble(),
    rewardSensitivityConfidence = rewardSensitivity.confidence.toDouble(),
    rewardSensitivitySamples = rewardSensitivity.sampleSize.toLong(),
    riskAversionValue = riskAversion.value.toDouble(),
    riskAversionConfidence = riskAversion.confidence.toDouble(),
    riskAversionSamples = riskAversion.sampleSize.toLong(),
    sync_updated_at = Clock.System.now().toString(),
    is_deleted = 0L,
    sync_version = 0L,
    last_synced_at = null,
)
