import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.project"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.project"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Read API keys from local.properties
        val localPropertiesFile = rootProject.file("local.properties")
        val localProperties = Properties()
        if (localPropertiesFile.exists()) {
            localProperties.load(localPropertiesFile.inputStream())
        }

        // Add multiple API keys to BuildConfig
        val finnhubApiKey = localProperties.getProperty("FINNHUB_API_KEY", "")
        val finnhubApiKey2 = localProperties.getProperty("FINNHUB_API_KEY_2", "")
        val finnhubApiKey3 = localProperties.getProperty("FINNHUB_API_KEY_3", "")
        val finnhubApiKey4 = localProperties.getProperty("FINNHUB_API_KEY_4", "")

        buildConfigField("String", "FINNHUB_API_KEY", "\"$finnhubApiKey\"")
        buildConfigField("String", "FINNHUB_API_KEY_2", "\"$finnhubApiKey2\"")
        buildConfigField("String", "FINNHUB_API_KEY_3", "\"$finnhubApiKey3\"")
        buildConfigField("String", "FINNHUB_API_KEY_4", "\"$finnhubApiKey4\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        buildConfig = true
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // WebSocket and JSON
    implementation(libs.okhttp)
    implementation(libs.gson)

    // AndroidX Lifecycle
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)

    // RecyclerView and CardView
    implementation(libs.recyclerview)
    implementation(libs.cardview)

    // Chart library
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}