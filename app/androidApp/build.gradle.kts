import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.File
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.firebase.performance)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}

val localProperties: Properties = Properties().apply {
    val propsFile = rootProject.file("local.properties")
    if (propsFile.exists()) propsFile.inputStream().use { load(it) }
}

fun localProp(key: String): String? =
    localProperties[key]?.toString() ?: project.findProperty(key) as? String

// Resolve the release keystore. Set RELEASE_STORE_FILE in local.properties to point at it, given as
// an absolute path, a ~-relative path, or a path relative to the repo root. Defaults to
// lifeplanner.jks at the repo root. This lets the keystore live outside the repo (e.g.
// ~/Documents/tribe/lifeplanner.jks) without copying it in.
fun resolveKeystoreFile(): File {
    val configured = localProp("RELEASE_STORE_FILE")?.trim()?.takeIf { it.isNotEmpty() }
        ?: return rootProject.file("lifeplanner.jks")
    val expanded = if (configured.startsWith("~/")) {
        System.getProperty("user.home") + configured.substring(1)
    } else configured
    val f = File(expanded)
    return if (f.isAbsolute) f else rootProject.file(expanded)
}

android {

    signingConfigs {
        val keystoreFile = resolveKeystoreFile()
        if (keystoreFile.exists()) {
            create("release") {
                storeFile = keystoreFile
                storePassword = localProp("RELEASE_STORE_PASSWORD")
                keyAlias = localProp("RELEASE_KEY_ALIAS")
                keyPassword = localProp("RELEASE_KEY_PASSWORD")
            }
        }
    }

    namespace = "az.tribe.lifeplanner"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "az.tribe.lifeplanner"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 11
        versionName = "3.0.0"
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
            val keystoreFile = resolveKeystoreFile()
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
    implementation(projects.app.shared)

    debugImplementation(compose.uiTooling)
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // Firebase BOM (gradle plugins for crashlytics/perf need the actual SDK on the classpath)
    implementation(platform(libs.firebase.bom))

    implementation(libs.androidx.activity.compose)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)

    // Google Play services
    implementation(libs.play.app.update)
    implementation(libs.play.app.update.ktx)
    implementation(libs.play.review)
    implementation(libs.play.review.ktx)

    // Health Connect (needed at app link time)
    implementation(libs.health.connect)
}
