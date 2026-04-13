plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.forlks.personal_wellness_routine"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.forlks.personal_wellness_routine"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.0.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
        buildConfig = true   // BuildConfig 클래스 생성 활성화
    }

    buildTypes {
        debug {
            isDebuggable = true
            // ── 광고 OFF ──────────────────────────────────
            buildConfigField("boolean", "ADS_ENABLED", "false")
            // 테스트 AdMob App ID (manifest 에서 참조)
            manifestPlaceholders["admobAppId"] = "ca-app-pub-3940256099942544~3347511713"
            // 배너 단위 ID (사용되지 않음 — ADS_ENABLED=false)
            buildConfigField("String", "BANNER_AD_UNIT_ID",
                "\"ca-app-pub-3940256099942544/6300978111\"")
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // ── 광고 ON ───────────────────────────────────
            buildConfigField("boolean", "ADS_ENABLED", "true")
            // TODO: 출시 전 실제 AdMob App ID 로 교체
            manifestPlaceholders["admobAppId"] = "ca-app-pub-3940256099942544~3347511713"
            // TODO: 출시 전 실제 배너 Ad Unit ID 로 교체
            buildConfigField("String", "BANNER_AD_UNIT_ID",
                "\"ca-app-pub-3940256099942544/6300978111\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.hilt.navigation.compose)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Navigation
    implementation(libs.navigation.compose)

    // WorkManager
    implementation(libs.work.runtime.ktx)
    implementation(libs.hilt.work)
    ksp(libs.hilt.compiler)

    // Coroutines
    implementation(libs.coroutines.android)

    // AdMob
    implementation(libs.play.services.ads)

    // Lottie
    implementation(libs.lottie.compose)

    // DataStore
    implementation(libs.datastore.preferences)

    // Gson
    implementation(libs.gson)

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.15.0"))
    implementation("com.google.firebase:firebase-analytics-ktx")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
