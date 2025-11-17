# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# JSOUP HTML 解析库
-keep class org.jsoup.** { *; }

# RxHttp 网络请求库
-dontwarn rxhttp.**
-keep class rxhttp.** { *; }
-keep class androidx.lifecycle.** { *; }
-keepclassmembers class * {
    @rxhttp.wrapper.annotation.DefaultDomain <fields>;
}

# OkHttp 网络库
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }
-dontwarn okio.**

# Kotlin 协程
-keep class kotlin.coroutines.** { *; }
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**
-keepclassmembers class kotlinx.coroutines.android.AndroidDispatcherFactory {*;}

# DataStore
-keep class androidx.datastore.** {*;}
-keep class androidx.datastore.preferences.** {*;}

# Compose
-keep class androidx.compose.** { *; }
-keep class kotlin.jvm.functions.** { *; }
-dontwarn androidx.compose.**
-dontwarn kotlin.jvm.functions.**

# Lifecycle
-keep class androidx.lifecycle.** { *; }

# Navigation
-keep class androidx.navigation.** { *; }

# Koin 依赖注入
-keep class org.koin.** { *; }
-dontwarn org.koin.**

# Material3
-keep class com.google.android.material.** { *; }

# Salt UI
-keep class com.salt.ui.** { *; }

# ViewModel
-keep class androidx.lifecycle.ViewModel { *; }
-keep class androidx.lifecycle.ViewModelProvider { *; }
-keep class androidx.lifecycle.viewmodel.** { *; }

# 保持自定义的 ViewModel 类
-keep class com.lonx.ecjtu.calendar.** extends androidx.lifecycle.ViewModel { *; }

# 保持 BuildConfig 类
-keep class com.lonx.ecjtu.calendar.BuildConfig { *; }

# 保持所有数据类
-keep class com.lonx.ecjtu.calendar.**.model.** { *; }

# 保持所有网络请求相关的类
-keep class com.lonx.ecjtu.calendar.**.data.** { *; }
-keep class com.lonx.ecjtu.calendar.**.network.** { *; }
-keep class com.lonx.ecjtu.calendar.**.repository.** { *; }

# Glance App Widget
-keep class androidx.glance.** { *; }
-dontwarn androidx.glance.**

# Kotlin 序列化库
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class kotlinx.serialization.** {
    *** Companion;
}
-dontwarn kotlinx.serialization.**

# 保持使用@Serializable注解的类
-keep class com.lonx.ecjtu.calendar.domain.model.** {
    *** Companion;
}
-keepclasseswithmembers class com.lonx.ecjtu.calendar.domain.model.** {
    *** Companion;
}

-keep class com.lonx.ecjtu.calendar.data.dto.** { *; }

# 保持枚举类
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 保持注解
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses