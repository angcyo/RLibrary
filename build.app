



#2018-2-8 使用方法
请在 key.gradle 文件中编写签名key, 并且复制到 app 模型的根目录,
之后
apply from: 'key.gradle'
即可





















#2018-1-24 只需要下面一行代码即可

apply from: '../RLibrary/app.gradle'


#2018-2-8 可能需要的签名配置
    signingConfigs {
        angcyo {
            keyAlias 'angcyo'
            keyPassword 'angcyo'
            storeFile file('../RLibrary/angcyo.jks')
            storePassword 'angcyo'
        }
    }


apply plugin: 'com.android.application'
apply plugin: 'kotlin-android'
//apply plugin: 'com.getkeepsafe.dexcount'
apply from: 'build_time.gradle'
apply from: '../RLibrary/android.gradle'

def apkTime() {
    return new Date().format("yyyy-MM-dd", TimeZone.getTimeZone("UTC"))
}

def apk_time = apkTime()

android {

    compileSdkVersion Integer.parseInt(rootProject.C_SDK)
    buildToolsVersion rootProject.B_TOOLS
    defaultConfig {
        applicationId "$application_id"
        minSdkVersion Integer.parseInt(rootProject.M_SDK)
        targetSdkVersion Integer.parseInt(rootProject.T_SDK)
        versionCode 1
        versionName "1.0"
        multiDexEnabled true

//        ndk {
//            // 设置支持的SO库架构
//            abiFilters 'armeabi', 'armeabi-v7a', 'x86', 'x86_64'//, 'arm64-v8a'
//            //, 'x86', 'armeabi-v7a', 'x86_64', 'arm64-v8a'
//        }
    }
//    sourceSets {
//        main {
//            res.srcDirs = [
//                    'src/main/res/'
//            ]
//        }
//    }
    flavorDimensions "type"
    productFlavors {
        //develop
        _dev {
            dimension "type"
            minSdkVersion 21
            buildConfigField "boolean", "SHOW_DEBUG", "true"
        }
        //preview
        pre {
            dimension "type"
            minSdkVersion Integer.parseInt(rootProject.M_SDK)
            buildConfigField "boolean", "SHOW_DEBUG", "true"
        }
        //apk
        apk {
            dimension "type"
            minSdkVersion Integer.parseInt(rootProject.M_SDK)
            buildConfigField "boolean", "SHOW_DEBUG", "false"
        }
    }
    buildTypes {
        release {
            //applicationIdSuffix ".release"
            zipAlignEnabled true
            shrinkResources false
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
            signingConfig signingConfigs.angcyo
        }
        debug {
            //applicationIdSuffix ".debug"
            zipAlignEnabled true
            shrinkResources false
            signingConfig signingConfigs.angcyo
        }
    }

    /*Gradle3.0 以下的方法*/
//    getApplicationVariants().all { variant ->
//        variant.outputs.each { output ->
//            def appName = "UIViewDemo-${variant.buildType.name}-${variant.versionName}"
//            def time = ""
//            if (variant.buildType.name.equalsIgnoreCase("release")) {
//                time = "_${new Date().format("yyyy-MM-dd_HH-mm")}"
//            }
//            output.outputFile = new File(output.outputFile.parent, "${appName}${time}.apk")
//        }
//    }

    /*Gradle3.0 以上的方法*/
    applicationVariants.all { variant ->
        if (variant.buildType.name != "debug") {
            variant.getPackageApplication().outputDirectory = new File(project.rootDir.absolutePath + "/apk")
        }

        variant.getPackageApplication().outputScope.apkDatas.forEach { apkData ->
            apkData.outputFileName = "UIViewDemo-" +
                    variant.versionName + "_" +
                    apk_time + "_" +
                    variant.flavorName + "_" +
                    variant.buildType.name + "_" +
                    variant.signingConfig.name +
                    ".apk"
        }
    }
}

dependencies {
    implementation fileTree(include: ['*.jar'], dir: 'libs')
    implementation project(':RLibrary:uiview')
    //implementation project(':RLibrary:imagepicker')
    //implementation 'com.android.support.constraint:constraint-layout:1.0.2'
    //annotationProcessor 'com.jakewharton:butterknife-compiler:8.4.0'
    implementation "org.jetbrains.kotlin:kotlin-stdlib-jre7:$kotlin_version"
    //FPS显示库 https://github.com/wasabeef/Takt
    //implementation 'jp.wasabeef:takt:1.0.4'
    //性能检测库 https://github.com/markzhai/AndroidPerformanceMonitor
    //_devCompile 'com.github.markzhai:blockcanary-android:1.5.0'
    //preCompile 'com.github.markzhai:blockcanary-android:1.5.0'
}