// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.googleService) apply false
    alias(libs.plugins.firebasePerf) apply false
    alias(libs.plugins.firebaseCrashlytics) apply false
}

buildscript {
    dependencies {
        classpath(libs.firebase.crashlytics.gradle)
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}