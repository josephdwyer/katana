import org.jetbrains.kotlin.kapt3.base.Kapt.kapt

group = "com.josephdwyer.katana"
base.archivesBaseName = "gradle-plugin"
version = "0.0.1-SNAPSHOT"

repositories {
    maven { setUrl("https://dl.bintray.com/kotlin/kotlin-eap") }
    mavenCentral()
}

plugins {
    kotlin("jvm") version "1.3.72"
    id("java-gradle-plugin")

    kotlin("kapt") version "1.3.72"
    id("com.gradle.plugin-publish") version "0.12.0"
}

dependencies {
    implementation(kotlin("gradle-plugin", "1.3.72"))
    compileOnly("com.google.auto.service:auto-service:1.0-rc4")
    kapt("com.google.auto.service:auto-service:1.0-rc4")
}

pluginBundle {
    website = "https://github.com/josephdwyer/katana"
    vcsUrl = "https://github.com/josephdwyer/katana"
    tags = listOf("kotlin", "kotlin native", "kotlin/native")
}

gradlePlugin {
    plugins {
        create("katanaPlugin") {
            id = "com.josephdwyer.katana"
            displayName = "Katana"
            description = "A simple plugin that extracts information about classes and functions from the build"
            implementationClass = "com.josephdwyer.katana.gradle.Katana"
        }
    }
}