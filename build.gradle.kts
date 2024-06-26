buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.4.0")
        
    }
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://jitpack.io")
    }
}



// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.3.1" apply false
    id("org.jetbrains.kotlin.android") version "1.8.10" apply false
    // Add the dependency for the App Distribution Gradle plugin
}