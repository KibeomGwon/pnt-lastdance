import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
    id("kotlin-parcelize")
    id("com.google.dagger.hilt.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.d104.pnt"
    compileSdk = 36

    val localProperties = Properties().apply {
        project.rootProject.file("local.properties").inputStream().use { load(it) }
    }

    defaultConfig {
        applicationId = "com.d104.pnt"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        manifestPlaceholders["GOOGLE_MAP_API_KEY"] = localProperties.getProperty("GOOGLE_MAP_API_KEY") ?: ""
        manifestPlaceholders["INGAME_MAPS_ID"] = localProperties.getProperty("INGAME_MAPS_ID") ?: ""
        manifestPlaceholders["SETTING_MAPS_ID"] = localProperties.getProperty("SETTING_MAPS_ID") ?: ""
        manifestPlaceholders["CLIENT_ID"] = localProperties.getProperty("CLIENT_ID") ?: ""
        manifestPlaceholders["CLIENT_SECRET"] = localProperties.getProperty("CLIENT_SECRET") ?: ""

        // Kakao Login
        manifestPlaceholders["KAKAO_NATIVE_APP_KEY"] = localProperties.getProperty("KAKAO_NATIVE_APP_KEY") ?: ""

        buildConfigField(
            "String",
            "GOOGLE_MAP_API_KEY",
            "\"${localProperties["GOOGLE_MAP_API_KEY"]}\""
        )
        buildConfigField(
            "String",
            "INGAME_MAP_ID",
            "\"${localProperties["INGAME_MAP_ID"]}\""
        )
        buildConfigField(
            "String",
            "SETTING_MAP_ID",
            "\"${localProperties["SETTING_MAP_ID"]}\""
        )
        buildConfigField(
            "String",
            "KAKAO_NATIVE_APP_KEY",
            "\"${localProperties["KAKAO_NATIVE_APP_KEY"]}\""
        )
        buildConfigField(
            "String",
            "CLIENT_ID",
            "\"${localProperties["CLIENT_ID"]}\""
        )
        buildConfigField(
            "String",
            "CLIENT_SECRET",
            "\"${localProperties["CLIENT_SECRET"]}\""
        )
    }

    signingConfigs {
        create("pnt-debug") {
            storeFile = file(localProperties.getProperty("SIGNING_STORE_FILE"))
            storePassword = localProperties.getProperty("SIGNING_STORE_PASSWORD")
            keyAlias = localProperties.getProperty("SIGNING_KEY_ALIAS")
            keyPassword = localProperties.getProperty("SIGNING_KEY_PASSWORD")
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("pnt-debug")
        }

        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11

        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    // Android Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose BOM
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("androidx.compose.material:material-icons-extended:1.6.1")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Retrofit (HTTP 통신)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Gson (JSON 파싱)
    implementation("com.google.code.gson:gson:2.10.1")
    implementation(libs.androidx.foundation.layout)

    // Room (로컬 DB)
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")

    // DataStore (설정 저장)
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Google Maps & Location
    implementation("com.google.maps.android:maps-compose:4.3.3")
    implementation("com.google.maps.android:android-maps-utils:3.8.2")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.1.0")

    // CameraX (카메라 & QR 스캔)
    val cameraxVersion = "1.3.1"
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")

    // QR Code & ML Kit
    implementation("com.google.mlkit:barcode-scanning:17.3.0")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("com.google.zxing:core:3.5.2")

    // Coil (이미지 로딩)
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation("io.coil-kt:coil-gif:2.5.0")

    // Timber (로깅)
    implementation("com.jakewharton.timber:timber:5.0.1")

    // livekit
    implementation("io.livekit:livekit-android:2.9.0")

    // Accompanist (권한, 시스템 UI)
    implementation("com.google.accompanist:accompanist-permissions:0.34.0")
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.34.0")

    // Lottie (애니메이션)
    implementation("com.airbnb.android:lottie-compose:6.3.0")

    // Firebase (Push 알림)
    implementation(platform("com.google.firebase:firebase-bom:32.7.2"))
    implementation("com.google.firebase:firebase-messaging-ktx")

    // Google Maps for Compose
    val mapsComposeVersion = "4.4.1"
    implementation("com.google.maps.android:maps-compose:${mapsComposeVersion}")
    implementation("com.google.maps.android:maps-compose-utils:${mapsComposeVersion}")
    implementation("com.google.maps.android:maps-compose-widgets:${mapsComposeVersion}")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.56.2")
    kapt("com.google.dagger:hilt-android-compiler:2.56.2")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    kapt("androidx.hilt:hilt-compiler:1.2.0")

    // Gif 지원
    implementation("io.coil-kt:coil-compose:2.4.0")
    implementation("io.coil-kt:coil-gif:2.4.0")

    // CameraX - 카메라 기능을 위한 라이브러리들
    implementation("androidx.camera:camera-core:1.4.2")        // 핵심 기능
    implementation("androidx.camera:camera-camera2:1.4.2")     // Camera2 API 연결
    implementation("androidx.camera:camera-lifecycle:1.4.2")   // 생명주기 관리
    implementation("androidx.camera:camera-view:1.4.2")       // 프리뷰 화면

    // ML Kit - 머신러닝 기능
    implementation("com.google.mlkit:object-detection:17.0.2") // 객체 인식

    // Compose에서 권한 처리를 쉽게 해주는 라이브러리
    implementation("com.google.accompanist:accompanist-permissions:0.37.3")

    // EXIF 정보 처리 (이미지 회전 문제 해결용) - 필수 추가!
    implementation("androidx.exifinterface:exifinterface:1.3.7")

    // KaKao Login
    implementation("com.kakao.sdk:v2-user:2.23.2")

    // socket
    implementation("io.socket:socket.io-client:2.1.0")

    // Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // java.time desugaring
     coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
}