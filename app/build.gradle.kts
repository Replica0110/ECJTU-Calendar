import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone.getDefault

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

fun getGitCommitLog(versionName: String): String {
    // 查找当前版本（versionName）对应的 git tag
    val currentTag = "v$versionName"

    // 查找当前版本的上一个 git tag
    val getPreviousTagCmd = "git describe --abbrev=0 --tags $currentTag^"
    val previousTagProcess = ProcessBuilder(getPreviousTagCmd.split(" "))
        .redirectErrorStream(true)
        .start()
    println(previousTagProcess)
    val previousTag = try {
        val exitCode = previousTagProcess.waitFor()
        // 检查命令是否成功执行（退出码为0）
        if (exitCode == 0) {
            previousTagProcess.inputStream.bufferedReader().readText().trim()
        } else {
            // 命令执行失败，返回空字符串
            ""
        }
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    } finally {
        previousTagProcess.destroy()
    }

    // 如果没有上一个 tag，直接查当前 tag 的所有提交
    if (previousTag.isEmpty()) {
        val cmd = "git log $currentTag --pretty=format:%s --invert-grep --grep=^docs --grep=^build"
        val process = ProcessBuilder(cmd.split(" "))
            .redirectErrorStream(true)
            .start()
        return try {
            process.waitFor()
            val logs = process.inputStream.bufferedReader().readLines()
            logs.joinToString(separator = ", ", prefix = "[", postfix = "]") { "\"$it\"" }
        } catch (e: Exception) {
            e.printStackTrace()
            "[]"
        } finally {
            process.destroy()
        }
    }

    // 正常情况，查两个 tag 之间的提交
    val cmd = "git log $previousTag..$currentTag --pretty=format:%s --invert-grep --grep=^docs --grep=^build"
    val process = ProcessBuilder(cmd.split(" "))
        .redirectErrorStream(true)
        .start()
    return try {
        process.waitFor()
        val logs = process.inputStream.bufferedReader().readLines()
        logs.joinToString(separator = ", ", prefix = "[", postfix = "]") { "\"$it\"" }
    } catch (e: Exception) {
        e.printStackTrace()
        "[]"
    } finally {
        process.destroy()
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8)
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
    implementation(libs.okhttp)
    implementation(libs.appupdater)
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

tasks.register("generateMetadata") {
    doLast {
        val versionCode = android.defaultConfig.versionCode
        val versionName = android.defaultConfig.versionName
        val buildTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").apply {
            timeZone = getDefault()
        }.format(Date())

        // 传递 versionName 到 getGitCommitLog 获取更精确的 changelog
        val changelog = versionName?.let { getGitCommitLog(it) }

        // 获取release目录中的所有APK文件
        val outputDir = file("${project.projectDir}/release")
        val apkFiles = outputDir.listFiles { _, name -> name.endsWith(".apk") }

        // 如果存在APK文件，则选取第一个APK文件作为应用APK
        val outputFile = apkFiles?.firstOrNull()

        // 转换文件大小为MB并保留两位小数
        val fileSizeInMB = outputFile?.let {
            val sizeInMB = it.length() / (1024.0 * 1024.0) // 文件大小转换为MB
            val df = DecimalFormat("#.00") // 格式化为两位小数
            df.format(sizeInMB)
        } ?: "0.00" // 如果没有找到APK文件，大小为0.00MB

        // APK URL（假设存储在GitHub发布页）
        val apkUrl = outputFile?.let {
            "https://github.com/Replica0110/ECJTU-Calendar/releases/download/$versionName/${it.name}"
        } ?: ""

        // 准备元数据
        val metadata = """
            {
                "versionCode": $versionCode,
                "versionName": "$versionName",
                "buildTime": "$buildTime",
                "changelog": $changelog,
                "fileSize": "$fileSizeInMB MB",
                "apkUrl": "$apkUrl"
            }
        """.trimIndent()

        // 创建release目录（如果不存在的话）
        outputDir.mkdirs()
        val metadataFile = file("${outputDir}/metadata.json")
        metadataFile.writeText(metadata)
        println("Metadata file generated at: ${metadataFile.absolutePath}")
    }
}

