# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/angcyo/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-ignorewarnings

-dontwarn com.angcyo.uiview.**
-dontwarn com.hn.d.valley.**

-dontwarn com.jcodeing.**
-dontwarn com.fasterxml.jackson.**
-dontwarn com.alipay.**
-dontwarn com.netease.nimlib.**
-dontwarn com.hwangjr.rxbus.**
-dontwarn com.umeng.**
-dontwarn okio.**
-dontwarn com.alipayzhima.**
-dontwarn retrofit2.**
-dontwarn com.xiaomi.push.**
-dontwarn com.tencent.**
-dontwarn com.amap.**
-dontwarn rx.internal.util.**
