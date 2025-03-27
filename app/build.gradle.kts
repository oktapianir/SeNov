
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-parcelize")
    id("androidx.navigation.safeargs.kotlin")
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.google.firebase.crashlytics)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.okta.senov"
    compileSdk = 35

    defaultConfig    {
        applicationId = "com.okta.senov"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField ("String", "BASE_URL", "\"https://api.apileague.com/\"")
        buildConfigField ("String", "API_KEY", "\"f444632ab8ee4d8ca4d44353aa0506b6\"")

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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {

    //glide
    implementation (libs.glide)
    annotationProcessor (libs.compiler)

    //firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.play.services.auth.v2030)
    implementation (libs.firebase.storage.ktx)

    //android libraries
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    //testing libraries
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //navigation
    implementation (libs.androidx.navigation.fragment.ktx)
    implementation (libs.androidx.navigation.ui.ktx)

    //lottie
    implementation (libs.lottie)

    //room database
    ksp(libs.androidx.room.compiler.v250)
    implementation (libs.androidx.room.ktx)
    implementation (libs.androidx.lifecycle.viewmodel.ktx)
    implementation (libs.androidx.lifecycle.livedata.ktx)
    implementation (libs.androidx.room.runtime)
    implementation (libs.androidx.legacy.support.v4)

    //hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    //retrofit
    implementation (libs.retrofit)
    implementation (libs.converter.gson)
    implementation (libs.okhttp)
    implementation (libs.logging.interceptor)

    //timber
    implementation (libs.timber)

    //chucker
    implementation (libs.library)
    releaseImplementation (libs.library.no.op)

    //pdf
    implementation (libs.itext7.core)
}