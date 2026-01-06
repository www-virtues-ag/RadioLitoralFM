plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.googleService)
    alias(libs.plugins.firebasePerf)
    alias(libs.plugins.firebaseCrashlytics)
}

android {
    namespace = "br.com.fivecom.litoralfm"
    compileSdk = 36

    defaultConfig {
        buildConfigField("String", "PACKAGE", "\"br_com_fivecom_litoralfm\"")

        applicationId = "br.com.fivecom.litoralfm"
        minSdk = 24
        targetSdk = 36
        versionCode = 36
        versionName = "2025.12.31"
        multiDexEnabled = true

        vectorDrawables.useSupportLibrary = true
        setProperty("archivesBaseName", "$applicationId-version-$versionCode-$versionName")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    signingConfigs {
        create("release") {
            keyAlias = "litoral fm"
            keyPassword = "00203133"
            storePassword = "123456"
            storeFile = file("${rootDir}/APKs/chave.jks")
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        buildConfig = true
        dataBinding = true
        viewBinding = true
        resValues = true
    }
    applicationVariants.all {
        val variant = this
        variant.outputs
            .map { it as com.android.build.gradle.internal.api.BaseVariantOutputImpl }
            .forEach { output ->
                val outputFileName =
                    "${variant.baseName}_${variant.versionName}_${variant.versionCode}.apk"
                println("OutputFileName: $outputFileName")
                output.outputFileName = outputFileName
            }
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    //DEX
    implementation(libs.multidex)

    //UI
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.swiperefreshlayout)
    implementation(libs.constraintlayout)
    implementation(libs.browser)
    implementation(libs.intuit)
    implementation(libs.appdimens)
    implementation(libs.mpandroidchart)

    //FIREBASE
    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.firebase)

    //MEDIA
    implementation(libs.media)
    implementation(libs.exomedia)
//    api(libs.androidx.media3.exoplayer.rtsp)
//    api(libs.androidx.media3.datasource.rtmp)
//    api(libs.androidx.media3.cast)
//    api(libs.androidx.media3.session)
    implementation(libs.chromecast)
    implementation(libs.mediarouter)

    //NOTIFICATION
    implementation(libs.onesignal)

    //IMAGE
    implementation(libs.glide)
    annotationProcessor(libs.glide.compiler)
    implementation(libs.lottie)

    //CORE
    implementation(libs.update)
    implementation(libs.review)

    //REQUEST
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
    implementation(libs.retrofit)
    implementation(libs.gson)
    implementation(libs.retrofit.gson)
    implementation(libs.retrofit.scalars)

    //implementation(libs.play.services.location)

    implementation(libs.xxpermissions)

    implementation(libs.shimmer)
    implementation(libs.cutetoast)
    implementation(libs.dotsindicator)

    //implementation(libs.android.image.cropper)
    //implementation(libs.wasabeef.blurry)
    ///implementation(libs.androidyoutubeplayer.core)
    //implementation(libs.chromecast.sender)
    //implementation(libs.glidetovectoryou)
    //implementation(libs.shaperipplelibrary)
    //implementation(libs.luckycatlabs.sunrisesunsetcalculator)
    //implementation(libs.extensionokhttp)
    //implementation(libs.persistentcookiejar)
    //implementation (libs.verticalseekbar)
    //implementation(libs.android.database.sqlcipher)
    //implementation(libs.palette)
    //implementation(libs.library)
    //implementation(libs.colorpicker)
    //implementation(libs.commons)
    //implementation(libs.core)
    //implementation(libs.bottom.bar)
    //implementation("com.soundcloud.android:android-crop:1.0.1@aar")
    //implementation(libs.searchablespinnerlibrary)
    //implementation(libs.roundablelayout)
    //implementation(libs.circularseekbar)
    //implementation(libs.androidyoutubeplayer.core)
    //implementation(libs.billing)
    //implementation(libs.circularseekbar)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}