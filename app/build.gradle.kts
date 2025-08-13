import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone.getDefault

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("kotlin-parcelize")
}

fun gitVersionCode(): Int {
    val cmd = "git rev-list HEAD --first-parent --count"
    val process = ProcessBuilder(cmd.split(" "))
        .redirectErrorStream(true)
        .start()
    return try {
        process.waitFor()
        process.inputStream.bufferedReader().readText().trim().toInt()
    } catch (e: Exception) {
        e.printStackTrace()
        0
    } finally {
        process.destroy()
    }
}

fun gitVersionTag(): String {
    val cmd = "git describe --tags --match v*.*.*"
    val process = ProcessBuilder(cmd.split(" "))
        .redirectErrorStream(true)
        .start()
    val version = try {
        process.waitFor()
        process.inputStream.bufferedReader().readText().trim()
    } catch (e: Exception) {
        e.printStackTrace()
        "unknown"
    } finally {
        process.destroy()
    }

    val cleanVersion = if (version.startsWith("v")) version.substring(1) else version


    val pattern = """(\d+\.\d+)(\.\d+)?"""
    return when (val matcher = pattern.toRegex().find(cleanVersion)) {
        null -> "$cleanVersion.0"
        else -> {
            val majorMinor = matcher.groupValues[1]
            val patch = matcher.groupValues[2].takeIf { it.isNotEmpty() } ?: ".0"
            "$majorMinor$patch"
        }
    }
}


android {
    namespace = "com.lonx.ecjtu.hjcalendar"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.lonx.ecjtu.hjcalendar"
        minSdk = 26
        targetSdk = 36
        versionCode = gitVersionCode()
        versionName = gitVersionTag()

        val buildTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").apply {
            timeZone = getDefault()
        }.format(Date())

        // 设置输出文件名
        applicationVariants.all {
            val variant = this
            variant.outputs
                .map { it as com.android.build.gradle.internal.api.BaseVariantOutputImpl }
                .forEach { output ->
                    val outputFileName = "ECJTU-Calendar-${variant.versionName}.apk"
                    println("OutputFileName: $outputFileName")
                    output.outputFileName = outputFileName
                }
        }

        buildConfigField("String", "BUILD_TIME", "\"$buildTime\"")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            versionNameSuffix = ""
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            versionNameSuffix = "-debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
}

dependencies {
    implementation(libs.gson)
    implementation(libs.timelineview)
    implementation(libs.jsoup.jsoup)
    implementation(libs.mmkv)
    implementation(libs.okhttp)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.androidx.swiperefreshlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}



