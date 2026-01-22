import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone.getDefault
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

// ===== Configuration Cache 兼容的辅助函数 =====

fun currentBuildTime(): Provider<String> = provider {
    SimpleDateFormat("yyyy-MM-dd HH:mm:ss").apply {
        timeZone = getDefault()
    }.format(Date())
}

fun gitCommitHash(): Provider<String> = providers.exec {
    commandLine("git", "rev-parse", "--short", "HEAD")
    isIgnoreExitValue = true // 防止没有 git 时构建崩溃
}.standardOutput.asText.map { it.trim().ifEmpty { "unknown" } }
    .orElse("unknown")

fun gitVersionCount(): Provider<Int> = providers.exec {
    commandLine("git", "rev-list", "HEAD", "--first-parent", "--count")
    isIgnoreExitValue = true
}.standardOutput.asText.map { it.trim().toIntOrNull() ?: 1 }
    .orElse(1)

fun gitVersionTag(): Provider<String> = providers.exec {
    commandLine("git", "tag", "--list", "v*", "--sort=-v:refname")
    isIgnoreExitValue = true
}.standardOutput.asText.map { tagsOutput ->
    val latestTag = tagsOutput.trim().lines().firstOrNull()
    latestTag?.removePrefix("v")?.let { version ->
        val parts = version.split(".")
        val major = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val minor = parts.getOrNull(1)?.toIntOrNull() ?: 0
        val patch = parts.getOrNull(2)?.toIntOrNull() ?: 0
        "$major.$minor.$patch"
    } ?: "1.0.0"
}.orElse("1.0.0")

android {
    namespace = "com.lonx.ecjtu.calendar"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.lonx.ecjtu.calendar"
        minSdk = 28
        targetSdk = 36
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        versionCode = gitVersionCount().get()
        versionName = gitVersionTag().get()

        buildConfigField("String", "BUILD_TIME", "\"${currentBuildTime().get()}\"")
    }

    buildTypes {
        release {
            isShrinkResources = true
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

    packaging {
        jniLibs {
            keepDebugSymbols += setOf(
                "**/libandroidx.graphics.path.so",
                "**/libdatastore_shared_counter.so"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        }
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    composeCompiler {
        reportsDestination = layout.buildDirectory.dir("compose_compiler")
        metricsDestination = layout.buildDirectory.dir("compose_compiler")
    }
}

// 使用 androidComponents API 注册构建信息任务

// 存储任务名称而不是 TaskProvider 对象，避免内存泄漏风险
val buildInfoTaskNames = mutableMapOf<String, String>()

androidComponents {
    onVariants { variant ->
        val currentBuildType = variant.buildType ?: "debug"
        val variantName = variant.name

        // 创建任务名称
        val taskName = "${variantName}BuildInfo"
        buildInfoTaskNames[variantName] = taskName

        // 注册任务
        tasks.register(taskName, BuildInfoTask::class.java) {
            buildTime.set(currentBuildTime())
            commitHash.set(gitCommitHash())
            this.variantName.set(variantName)
            applicationId.set(variant.applicationId.get())
            buildType.set(currentBuildType)
            // 处理可能的版本号为空的情况
            versionCode.set(variant.outputs.firstOrNull()?.versionCode?.map { it.toString() } ?: provider { "1" })
            versionName.set(variant.outputs.firstOrNull()?.versionName ?: provider { "1.0.0" })
            outputDir.set(layout.buildDirectory.dir("outputs/apk/${currentBuildType}"))
        }
    }
}

// 修复：使用 configureEach 替代 projectsEvaluated 以支持 Configuration Cache
// 并在任务创建时懒加载匹配
tasks.configureEach {
    val taskName = this.name
    // 检查此任务是否为某个变体的 assemble 任务
    // 格式通常为 assembleDebug, assembleRelease
    if (taskName.startsWith("assemble")) {
        // 尝试反向匹配变体名称
        buildInfoTaskNames.forEach { (variantName, infoTaskName) ->
            val capitalizedVariant = variantName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            if (taskName == "assemble$capitalizedVariant") {
                // 建立依赖关系
                finalizedBy(infoTaskName)
            }
        }
    }
}

// 设置 APK 输出文件名
android.applicationVariants.all {
    val variantName = this.name
    val buildType = if (variantName.contains("debug", ignoreCase = true)) "debug" else "release"

    outputs.all {
        if (this is com.android.build.gradle.internal.api.ApkVariantOutputImpl) {
            outputFileName = "ECJTU-Calendar-${buildType}.apk"
        }
    }
}

/**
 * 自定义任务：输出构建信息
 */
abstract class BuildInfoTask : DefaultTask() {
    @get:Input
    abstract val buildTime: Property<String>

    @get:Input
    abstract val commitHash: Property<String>

    @get:Input
    abstract val variantName: Property<String>

    @get:Input
    abstract val applicationId: Property<String>

    @get:Input
    abstract val buildType: Property<String>

    @get:Input
    abstract val versionCode: Property<String>

    @get:Input
    abstract val versionName: Property<String>

    @get:Internal // 输出目录是用来读取文件的，不是生成文件的，所以用 Internal 避免缓存干扰
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun printBuildInfo() {
        val type = buildType.get()

        println("\n========== Build Artifact Info ==========")
        println("Build Time   : ${buildTime.get()}")
        println("Commit Hash  : ${commitHash.get()}")
        println("Variant      : ${variantName.get()}")
        println("Build Type   : $type")
        println("App ID       : ${applicationId.get()}")
        println("Version Code : ${versionCode.get()}")
        println("Version Name : ${versionName.get()}")

        val outputFile = outputDir.get()
            .file("ECJTU-Calendar-${type}.apk")
            .asFile

        if (outputFile.exists()) {
            val fileSizeBytes = outputFile.length()
            val fileSizeKB = fileSizeBytes / 1024.0
            val fileSizeMB = fileSizeKB / 1024.0
            println("Output File : ${outputFile.absolutePath}")
            println("File Size   : ${String.format("%.2f", fileSizeMB)} MB (${String.format("%.2f", fileSizeKB)} KB)")
        } else {
            println("Output File : Not found at expected location (Check: ${outputFile.path})")
        }
        println("=========================================\n")
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
    // miuix库
    implementation(libs.miuix.android)
    implementation(libs.miuix.icons)
    // room 数据库
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    // 导航
    implementation(libs.core)
    ksp(libs.ksp)  // KSP 处理器用于生成导航代码（Compose Destinations）
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