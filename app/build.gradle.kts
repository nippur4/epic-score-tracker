import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
}

// Optional release signing driven by a git-ignored keystore.properties at repo root.
val keystorePropsFile = rootProject.file("keystore.properties")
val keystoreProps = Properties().apply {
    if (keystorePropsFile.exists()) load(keystorePropsFile.inputStream())
}

// AdMob IDs come from an optional admob.properties at repo root (see admob.properties.example).
// Debug builds always use Google's test IDs. Release uses the real ones when the file has them and
// falls back to test IDs otherwise, so a malformed/missing App ID can never crash the app at launch.
val admobPropsFile = rootProject.file("admob.properties")
val admobProps = Properties().apply {
    if (admobPropsFile.exists()) load(admobPropsFile.inputStream())
}
fun admobId(key: String, testId: String): String {
    val v = admobProps.getProperty(key)?.trim().orEmpty()
    if (v.startsWith("ca-app-pub-") && !v.contains('X')) return v
    logger.warn("admob.properties: '$key' not set, release will use Google's TEST id")
    return testId
}
val testAdmobAppId = "ca-app-pub-3940256099942544~3347511713"
val testBanner = "ca-app-pub-3940256099942544/6300978111"
val testInterstitial = "ca-app-pub-3940256099942544/1033173712"
val testRewarded = "ca-app-pub-3940256099942544/5224354917"
val privacyPolicyUrl = admobProps.getProperty("privacyPolicyUrl")?.trim().orEmpty()

android {
    namespace = "com.epichypernova.scoretracker"
    compileSdk = 36

    defaultConfig {
        applicationId = "epic.score.tracker"
        minSdk = 24
        targetSdk = 36
        versionCode = 5
        versionName = "1.2.1"
        resourceConfigurations += listOf("es", "en")
        vectorDrawables { useSupportLibrary = true }

        manifestPlaceholders["admobAppId"] = testAdmobAppId
        buildConfigField("String", "ADMOB_BANNER", "\"$testBanner\"")
        buildConfigField("String", "ADMOB_INTERSTITIAL", "\"$testInterstitial\"")
        buildConfigField("String", "ADMOB_REWARDED", "\"$testRewarded\"")
        buildConfigField("String", "PRIVACY_POLICY_URL", "\"$privacyPolicyUrl\"")
    }

    if (keystorePropsFile.exists()) {
        signingConfigs {
            create("release") {
                storeFile = rootProject.file(keystoreProps.getProperty("storeFile"))
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (keystorePropsFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
            manifestPlaceholders["admobAppId"] = admobId("appId", testAdmobAppId)
            buildConfigField("String", "ADMOB_BANNER", "\"${admobId("banner", testBanner)}\"")
            buildConfigField("String", "ADMOB_INTERSTITIAL", "\"${admobId("interstitial", testInterstitial)}\"")
            buildConfigField("String", "ADMOB_REWARDED", "\"${admobId("rewarded", testRewarded)}\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.10.01")
    implementation(composeBom)

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")

    implementation("androidx.navigation:navigation-compose:2.8.3")
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Image loading + SVG decoding (for optional per-game logos in assets/logos/*.svg)
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("io.coil-kt:coil-svg:2.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // Ads (Google AdMob) + User Messaging Platform (GDPR/UK consent form)
    implementation("com.google.android.gms:play-services-ads:23.6.0")
    implementation("com.google.android.ump:user-messaging-platform:3.1.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
