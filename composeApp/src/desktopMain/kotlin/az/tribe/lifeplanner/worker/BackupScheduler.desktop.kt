package az.tribe.lifeplanner.worker

private object DesktopBackupScheduler : BackupSchedulerInterface {
    override fun scheduleDailyBackup() {}
    override fun cancelDailyBackup() {}
}

actual fun getBackupScheduler(): BackupSchedulerInterface = DesktopBackupScheduler
