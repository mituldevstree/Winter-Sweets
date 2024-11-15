import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kotlinKapt)
    alias(libs.plugins.parcelize)
    alias(libs.plugins.navigation.safeargs)
}

//Data Properties

val dataPropertiesFile = rootProject.file("data.properties")
val dataProperties = Properties()
dataProperties.load(FileInputStream(dataPropertiesFile))

val facebookAppId = dataProperties["facebook_app_id"]
val facebookScheme = dataProperties["facebook_scheme"]
val facebookClientToken = dataProperties["facebook_client_token"]

// Load keystore
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
keystoreProperties.load(FileInputStream(keystorePropertiesFile))

android {
    namespace = "com.app.development.winter"
    compileSdk = 34

    signingConfigs {
        create("release") {
            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
        }
    }
    flavorDimensions.add("default")

    productFlavors {
        create("Development") {
            dimension = "default"
            applicationId = "com.app.development.winter.sweets"

        }
        create("Production") {
            dimension = "default"
            applicationId = "com.game.winter.sweets.adventure"
        }
    }

    defaultConfig {
        applicationId = "com.game.winter.sweets.adventure"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        android.buildFeatures.buildConfig = true

        manifestPlaceholders["facebookAppId"] = (facebookAppId as String?) ?: ""
        manifestPlaceholders["facebookScheme"] = (facebookScheme as String?) ?: ""
        manifestPlaceholders["facebookClientToken"] = (facebookClientToken as String?) ?: ""

        buildConfigField("long", "FACEBOOK_APP_ID", (facebookAppId) ?: "")

        buildConfigField(
            "String", "SERVER_URL", "\"https://zombie-app-9d968150fe39.herokuapp.com/\""
        )
        buildConfigField(
            "String", "GENERAL_URL", "\"https://givvy-general-config.herokuapp.com/\""
        )
        buildConfigField(
            "String", "NOTIFICATION_URL", "\"https://givvy-general-notification.herokuapp.com/\""
        )
    }

    buildTypes {
        create("staging") {
            signingConfig = signingConfigs.getByName("release")
            isDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false
        }
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
        getByName("debug") {
            signingConfig = signingConfigs.getByName("debug")
            manifestPlaceholders["enableCrashReporting"] = false
        }

        applicationVariants.all {
            val variant = this
            variant.outputs.map { it as BaseVariantOutputImpl }.forEach { output ->
                val outputFileName =
                    "Zombie-${variant.name}-v${variant.versionName}-.apk"
                output.outputFileName = outputFileName
            }
        }
    }

    packaging {
        resources {
            excludes += "META-INF/DEPENDENCIES"
        }
    }
    bundle { language { enableSplit = false } }

    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    implementation(libs.gson)
    implementation(libs.retrofit)
    implementation(libs.retrofit.scalar)
    implementation(libs.gson.convertor)
    implementation(libs.logging.interceptor)
    implementation(libs.coroutine)
    implementation(libs.coroutine.core)
    implementation(libs.flexbox)

    implementation(libs.activity.ktx)
    implementation(libs.fragment.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.lifecycle.ext)
    implementation(libs.androidx.paging)

    implementation(libs.shimmer.view)
    implementation(libs.facebook.sdk)

    implementation(libs.sdp.dimens)
    implementation(libs.ssp.dimens)
    implementation(libs.glide)
    implementation(libs.lottie)
    implementation(libs.graph)
    implementation(libs.joda.time)

    implementation(libs.applovin.sdk)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.givvy.invitefriend.lib)
    implementation(libs.givvy.monetization.lib)
    implementation(libs.givvy.offerwall.lib)
    implementation(libs.givvy.withdraw.lib)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.work.runtime.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.firebase.core)
}