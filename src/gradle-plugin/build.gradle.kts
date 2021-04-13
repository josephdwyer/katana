import org.jetbrains.kotlin.kapt3.base.Kapt.kapt

group = "com.josephdwyer.katana"
base.archivesBaseName = "gradle-plugin"
version = "0.0.27"

repositories {
    mavenCentral()
}

plugins {
    kotlin("jvm") version "1.4.30"
    id("java-gradle-plugin")
    kotlin("kapt") version "1.4.30"
    id("com.gradle.plugin-publish") version "0.12.0"
}

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin-api:1.4.10")
    runtimeOnly("org.jetbrains.kotlin", "kotlin-reflect", "1.4.10")
    implementation(kotlin("gradle-plugin", "1.4.10"))
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
        create("katana") {
            id = "com.josephdwyer.katana"
            displayName = "Katana"
            description = "A simple plugin that extracts information about classes and functions from the build"
            implementationClass = "com.josephdwyer.katana.gradle.KatanaPlugin"
        }
    }
}