import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
    id("com.google.gms.google-services")
}

// ── keystore.properties 로드 (존재할 때만) ────────────────────────────────────
// 파일이 없으면 릴리즈 서명 없이 빌드 (CI나 로컬 디버그 빌드 시 정상 동작)
val keystorePropsFile = rootProject.file("keystore.properties")
val keystoreProps = Properties().also { props ->
    if (keystorePropsFile.exists()) keystorePropsFile.inputStream().use { props.load(it) }
}

android {
    namespace = "com.forlks.personal_wellness_routine"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.forlks.personal_wellness_routine"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // ── 릴리즈 서명 설정 ─────────────────────────────────────────────────────
    // keystore.properties 가 있을 때만 서명 설정 활성화
    if (keystorePropsFile.exists()) {
        signingConfigs {
            create("release") {
                storeFile     = file(keystoreProps.getProperty("storeFile") ?: "../wellflow-release.jks")
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias      = keystoreProps.getProperty("keyAlias") ?: "wellflow"
                keyPassword   = keystoreProps.getProperty("keyPassword")
            }
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true   // BuildConfig 클래스 생성 활성화
    }

    buildTypes {
        debug {
            isDebuggable = true

            // ── 광고 OFF ──────────────────────────────────────────────────
            buildConfigField("boolean", "ADS_ENABLED", "false")
            manifestPlaceholders["admobAppId"] = "ca-app-pub-3940256099942544~3347511713"
            buildConfigField("String", "BANNER_AD_UNIT_ID",
                "\"ca-app-pub-3940256099942544/6300978111\"")

            // ── GCP OAuth2 (개발용 Client ID) ─────────────────────────────
            buildConfigField("String", "GCP_WEB_CLIENT_ID",
                "\"599273408114-h9jmti2d3n6nb826h9ja1vdjh8a7rspp.apps.googleusercontent.com\"")
        }

        release {
            isMinifyEnabled   = true
            isShrinkResources = true   // 미사용 리소스 제거로 AAB 크기 최소화
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            // keystore.properties 에 릴리즈 서명 설정이 있으면 적용
            if (keystorePropsFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }

            // ── 광고 ON (keystore.properties 값 우선, 없으면 테스트 ID 유지) ─
            buildConfigField("boolean", "ADS_ENABLED", "true")
            manifestPlaceholders["admobAppId"] =
                keystoreProps.getProperty("admobAppId")
                    ?: "ca-app-pub-3940256099942544~3347511713"   // TODO: 실제 App ID 필요
            buildConfigField("String", "BANNER_AD_UNIT_ID",
                "\"${keystoreProps.getProperty("admobBannerUnitId")
                    ?: "ca-app-pub-3940256099942544/6300978111"}\"")   // TODO: 실제 Unit ID 필요

            // ── GCP OAuth2 (keystore.properties 값 우선, 없으면 개발용 ID) ──
            buildConfigField("String", "GCP_WEB_CLIENT_ID",
                "\"${keystoreProps.getProperty("gcpWebClientId")
                    ?: "599273408114-h9jmti2d3n6nb826h9ja1vdjh8a7rspp.apps.googleusercontent.com"}\"")
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

    // Credential Manager (Google Sign-In)
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)

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
