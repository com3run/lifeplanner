import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnLockMismatchReport
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension
import com.codingfeline.buildkonfig.compiler.FieldSpec
import java.util.Properties
import kotlin.apply

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.sqlDelight)
    alias(libs.plugins.buildkonfig)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.kover)
}

kotlin {
    compilerOptions {
        optIn.addAll(
            "kotlin.time.ExperimentalTime"
        )
    }

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    jvm("desktop")

    // On CI compile all 3 iOS targets. Locally compile arm64 (device) + simulator arm64.
    // iosX64 (Intel simulator) is skipped locally to avoid unnecessary compile time.
    val isCI = System.getenv("CI") != null
    val iosTargets = if (isCI) listOf(iosX64(), iosArm64(), iosSimulatorArm64()) else listOf(iosArm64(), iosSimulatorArm64())
    iosTargets.forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            binaryOption("bundleId", "az.tribe.lifeplanner")
            export(libs.kmpnotifier) {
                transitiveExport = false
            }
            linkerOpts.add("-lsqlite3")
        }
    }

    sourceSets {

        // Intermediate source set for Android only — libs that have no JVM artifact
        // (firebase-crashlytics, cmpcharts, compose-mediaplayer).
        // iOS gets the same libs added directly to iosMain.dependencies below.
        val mobileMain by creating {
            dependsOn(commonMain.get())
            dependencies {
                implementation(libs.firebase.crashlytics)
                implementation(libs.cmpcharts)
                implementation(libs.compose.mediaplayer)
            }
        }
        androidMain { dependsOn(mobileMain) }

        // Explicitly wire iOS hierarchy — the androidMain.dependsOn() above prevents
        // the default hierarchy template from running, so iosMain must be connected manually.
        iosMain {
            dependsOn(commonMain.get())
        }
        iosTargets.forEach { iosTarget ->
            getByName("${iosTarget.name}Main").dependsOn(iosMain.get())
        }

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.core.splashscreen)

            implementation(libs.sqldelight.android)
            //Http client
            implementation(libs.ktor.client.okhttp)
            implementation(libs.ktor.client.cio)
            implementation(libs.koin.android)

            implementation(libs.accompanist.systemuicontroller)

            implementation(libs.firebase.common.ktx)

            // Coroutines for Firebase Tasks (still used by Firebase Crashlytics/Perf)
            implementation(libs.kotlinx.coroutines.play.services)

            // WorkManager for background tasks
            implementation(libs.androidx.work.runtime)

            // Glance for home screen widgets
            implementation(libs.androidx.glance)
            implementation(libs.androidx.glance.appwidget)
            implementation(libs.androidx.glance.material3)

            // Facebook SDK (marketing & ads)
            implementation(libs.facebook.android.sdk)

            // PostHog (product analytics)
            implementation(libs.posthog.android)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(libs.phosphor.icons)
            implementation(libs.coil3.compose)
            implementation(libs.coil3.network.ktor)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.navigation.compose)

            implementation(libs.sqldelight.coroutines)

            //Ktor
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.serialization)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.auth)
            implementation(libs.ktor.client.websockets)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            implementation(libs.kotlinx.datetime)

            implementation(libs.multiplatform.settings)
            implementation(libs.multiplatform.settings.no.arg)

            implementation(libs.firebase.common)
            implementation(libs.gitlive.firebase.config)
            implementation(libs.firebase.perf)
            implementation(libs.gitlive.firebase.analytics)

            // Supabase
            implementation(libs.supabase.postgrest)
            implementation(libs.supabase.gotrue)
            implementation(libs.supabase.compose.auth)

            api(libs.kmpnotifier) // in iOS export this library
            //Kermit  for logging
            implementation(libs.kermit)
        }

        iosMain.dependencies {
            // sqlite
            implementation(libs.sqldelight.ios)
            // ktor
            implementation(libs.ktor.client.darwin)
            // HealthKit wrapper
            implementation(libs.health.kmp)
            // libs with no JVM artifact — also in mobileMain for Android
            implementation(libs.firebase.crashlytics)
            implementation(libs.cmpcharts)
            implementation(libs.compose.mediaplayer)
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.sqldelight.jvm)
                implementation(libs.ktor.client.cio)
                implementation(libs.kotlinx.coroutines.swing)
            }
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.koin.test)
            implementation(libs.turbine)
            implementation(libs.ktor.client.mock)
            implementation(libs.multiplatform.settings.test)
        }

        androidUnitTest.dependencies {
            implementation(libs.sqldelight.test)
        }
    }
}

sqldelight {
    databases {
        create("LifePlannerDB") {
            packageName.set("az.tribe.lifeplanner.database")
            schemaOutputDirectory = file("src/commonMain/sqldelight/databases")
            version = 28 // 22: HabitEntity.unit, 23: CachedPersonaEntity, 24: HabitCheckInEntity.count, 26: CachedPersonaEntity.slug+avatar_url, 27: UserSituationEntity, 28: ScreenTimeEventEntity + UserActivityPattern behavioral columns
            generateAsync.set(true)
        }
    }
    linkSqlite = true
}

android {

    signingConfigs {
        val keystoreFile = file("${rootDir}/lifeplanner.jks")
        val localProps = Properties().apply {
            val propsFile = rootProject.file("local.properties")
            if (propsFile.exists()) load(propsFile.inputStream())
        }
        if (keystoreFile.exists()) {
            create("release") {
                storeFile = keystoreFile
                storePassword = localProps["RELEASE_STORE_PASSWORD"]?.toString()
                    ?: project.findProperty("RELEASE_STORE_PASSWORD") as? String
                keyAlias = localProps["RELEASE_KEY_ALIAS"]?.toString()
                    ?: project.findProperty("RELEASE_KEY_ALIAS") as? String
                keyPassword = localProps["RELEASE_KEY_PASSWORD"]?.toString()
                    ?: project.findProperty("RELEASE_KEY_PASSWORD") as? String
            }
        }
    }

    namespace = "az.tribe.lifeplanner"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    flavorDimensions += "store"
    productFlavors {
        create("playStore") {
            dimension = "store"
        }
        create("quest") {
            dimension = "store"
            versionNameSuffix = "-quest"
        }
    }

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")

    defaultConfig {
        applicationId = "az.tribe.lifeplanner"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 10
        versionName = "2.4"

    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            val keystoreFile = file("${rootDir}/lifeplanner.jks")
            if (keystoreFile.exists() && signingConfigs.findByName("release") != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // Firebase BOM
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))

    // Play Store only — not available on Meta Quest
    "playStoreImplementation"(libs.play.app.update)
    "playStoreImplementation"(libs.play.app.update.ktx)
    "playStoreImplementation"(libs.play.review)
    "playStoreImplementation"(libs.play.review.ktx)
    "playStoreImplementation"(libs.health.connect)

    // Meta Horizon Platform SDK (Quest only)
    "questImplementation"("com.meta.horizon.platform.ovr:android-platform-sdk:71")
}


compose.desktop {
    application {
        mainClass = "az.tribe.lifeplanner.MainKt"
    }
}

buildkonfig {
    packageName = "az.tribe.lifeplanner"

    val localProperties =
        Properties().apply {
            val propsFile = rootProject.file("local.properties")
            if (propsFile.exists()) {
                load(propsFile.inputStream())
            }
        }

    defaultConfigs {
        buildConfigField(
            FieldSpec.Type.BOOLEAN,
            "isDebug", "true"
        )
        buildConfigField(
            FieldSpec.Type.STRING,
            "SUPABASE_URL",
            localProperties["SUPABASE_URL"]?.toString() ?: "",
        )
        buildConfigField(
            FieldSpec.Type.STRING,
            "SUPABASE_ANON_KEY",
            localProperties["SUPABASE_ANON_KEY"]?.toString() ?: "",
        )
        buildConfigField(
            FieldSpec.Type.STRING,
            "POSTHOG_API_KEY",
            localProperties["POSTHOG_API_KEY"]?.toString() ?: "",
        )
        buildConfigField(
            FieldSpec.Type.STRING,
            "POSTHOG_HOST",
            localProperties["POSTHOG_HOST"]?.toString() ?: "https://us.i.posthog.com",
        )
        buildConfigField(
            FieldSpec.Type.STRING,
            "PERSONA_API_SECRET",
            localProperties["PERSONA_API_SECRET"]?.toString() ?: "",
        )
        buildConfigField(
            FieldSpec.Type.STRING,
            "APP_VERSION",
            "2.3",
        )
        buildConfigField(
            FieldSpec.Type.STRING,
            "HORIZON_APP_ID",
            localProperties["HORIZON_APP_ID"]?.toString() ?: "",
        )
    }

    targetConfigs("release") {
        create("android") {
            buildConfigField(FieldSpec.Type.BOOLEAN, "isDebug", "false")
        }
        create("iosArm64") {
            buildConfigField(FieldSpec.Type.BOOLEAN, "isDebug", "false")
        }
        create("iosX64") {
            buildConfigField(FieldSpec.Type.BOOLEAN, "isDebug", "false")
        }
        create("iosSimulatorArm64") {
            buildConfigField(FieldSpec.Type.BOOLEAN, "isDebug", "false")
        }
    }
}


kover {
    // Disable instrumentation during normal builds — only runs when you invoke koverXmlReport etc.
    if (System.getenv("KOVER_ENABLED") == null) {
        disable()
    }
    reports {
        filters {
            includes {
                classes(
                    "az.tribe.lifeplanner.data.mapper.*",
                    "az.tribe.lifeplanner.domain.model.*",
                    "az.tribe.lifeplanner.domain.enum.*",
                    "az.tribe.lifeplanner.usecases.*",
                    "az.tribe.lifeplanner.ui.*ViewModel*",
                    "az.tribe.lifeplanner.ui.*UiState*",
                    "az.tribe.lifeplanner.data.repository.GoalTemplateProvider",
                )
            }
            excludes {
                classes(
                    "az.tribe.lifeplanner.data.repository.*Impl*",
                    "az.tribe.lifeplanner.data.sync.*",
                    "az.tribe.lifeplanner.ui.*Screen*",
                    "az.tribe.lifeplanner.ui.*Component*",
                )
            }
        }
    }
}

rootProject.plugins.withType(org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin::class.java) {
    rootProject.the<YarnRootExtension>().yarnLockMismatchReport =
        YarnLockMismatchReport.WARNING // NONE | FAIL
    rootProject.the<YarnRootExtension>().reportNewYarnLock = false // true
    rootProject.the<YarnRootExtension>().yarnLockAutoReplace = false // true
}
