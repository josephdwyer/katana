import org.jetbrains.kotlin.kapt3.base.Kapt.kapt

group = "com.josephdwyer.katana"
version = "1.0.0-SNAPSHOT"
base.archivesBaseName = "native-plugin"

repositories {
    maven { setUrl("https://dl.bintray.com/kotlin/kotlin-eap") }
    mavenCentral()
}

plugins {
    id("java-gradle-plugin")
    kotlin("jvm") version "1.3.72"
    id("com.gradle.plugin-publish") version "0.12.0"
    kotlin("kapt") version "1.3.72"
}

dependencies {
    //implementation(kotlin("stdlib", "1.4.0"))
    testCompile(gradleTestKit())
    testCompile("junit:junit:4+")
    compileOnly("org.jetbrains.kotlin:kotlin-compiler")
    compileOnly("com.google.auto.service:auto-service:1.0-rc4")
    kapt("com.google.auto.service:auto-service:1.0-rc4")
}