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

# RxHttp 网络请求库
-keepclassmembers class * {
    @rxhttp.wrapper.annotation.DefaultDomain <fields>;
}

# 保持行号和源文件信息（Crashlytics 等工具需要）
-keepattributes SourceFile,LineNumberTable
# 保持泛型、内部类、注解信息（Gson/Retrofit/Kotlin需要）
-keepattributes Signature,InnerClasses,*Annotation*

# 项目中所有 DTO 类都已正确添加 @Serializable 注解
# 保留 <fields> 是因为 UpdateDTO 没有指定别名，必须依靠字段名匹配 JSON
-keep @kotlinx.serialization.Serializable class * {
    <fields>;
}

# -keep class com.lonx.ecjtu.calendar.**.data.** { *; }
# -keep class com.lonx.ecjtu.calendar.**.model.** { *; }
# -keep class com.lonx.ecjtu.calendar.**.network.** { *; }
# -keep class com.lonx.ecjtu.calendar.**.repository.** { *; }
