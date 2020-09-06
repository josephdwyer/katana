import org.jetbrains.kotlin.kapt3.base.Kapt.kapt

group = "com.josephdwyer.katana"
base.archivesBaseName = "gradle-plugin"
version = "1.0.0-SNAPSHOT"

repositories {
    maven { setUrl("https://dl.bintray.com/kotlin/kotlin-eap") }
    mavenCentral()
}

plugins {
    id("java-gradle-plugin")
    kotlin("jvm") version "1.4.0"
    kotlin("kapt") version "1.3.72"
    id("com.gradle.plugin-publish") version "0.12.0"
}

/*
install {
    repositories.mavenInstaller {
        pom.artifactId = 'gradle-plugin'
    }
}*/

dependencies {
    implementation(kotlin("stdlib", "1.4.0"))
    testCompile(gradleTestKit())
    testCompile("junit:junit:4+")
    compileOnly("com.google.auto.service:auto-service:1.0-rc4")
    kapt("com.google.auto.service:auto-service:1.0-rc4")
}

// NativeStrings plugin configuration.

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