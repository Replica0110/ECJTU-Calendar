import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone.getDefault
import kotlin.apply

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)

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


android {
    namespace = "com.lonx.ecjtu.calendar"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.lonx.ecjtu.calendar"
        minSdk = 28
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
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
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
        compose = true
    }
}

dependencies {
    // Core & Compose - 核心与UI框架
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.material3)
    // ViewModel & Lifecycle for Compose - MVVM 架构支持
    implementation(libs.androidx.lifecycle.viewmodel.ktx) // 提供 viewModelScope
    implementation(libs.androidx.lifecycle.viewmodel.compose) // 提供 viewModel() Composable

    // Koin - 依赖注入
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    // Coroutines - 协程支持
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // DataStore - 轻量级数据存储 (替代 SharedPreferences 和 Room)
    implementation(libs.androidx.datastore.preferences)

    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)

    // Network - 网络请求
    implementation(libs.rxhttp)

//    implementation(libs.rxhttp.coroutines)
    ksp(libs.rxhttp.compiler)
    implementation(libs.okhttp)
    // saltui库
    implementation(libs.salt.ui.android)
    // miuix库
    implementation(libs.miuix.android)

    // 导航
    implementation(libs.core)
    ksp(libs.ksp)
    // markdown 渲染库
    implementation(libs.compose.markdown)
    // 图片加载库
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.zoomable.image.coil3)
    // HTML Parsing - HTML 解析
    implementation(libs.jsoup)
    // 序列化
    implementation(libs.kotlinx.serialization.json)
    // Testing - 测试依赖
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}