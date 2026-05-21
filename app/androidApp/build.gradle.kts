import org.jetbrains.kotlin.gradle.dsl.JvmTarget
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

android {

    signingConfigs {
        val keystoreFile = file("${rootDir}/lifeplanner.jks")
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
        versionCode = 10
        versionName = "2.5"
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
