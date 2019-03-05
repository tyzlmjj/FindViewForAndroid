import java.util.*

buildscript {
    repositories { jcenter() }
    dependencies { classpath(kotlin("gradle-plugin", "1.3.21")) }
}

plugins {
    id("org.jetbrains.intellij") version "0.3.12"
    kotlin("jvm") version "1.3.21"
}

group = "me.majiajie"
version= "1.2.0"

repositories {
    jcenter()
}

val properties = Properties()
properties.load(project.rootProject.file("local.properties").inputStream())

intellij {
    pluginName = "FindViewForAndroid"
    updateSinceUntilBuild = false
    setPlugins("Android")
    localPath = properties.getProperty("ANDROID_STUDIO_PATH")
}

dependencies {
    implementation(kotlin("compiler"))
    testImplementation("junit:junit:4.11")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}
