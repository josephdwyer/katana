import org.jetbrains.kotlin.kapt3.base.Kapt.kapt
import java.util.Date

group = "com.josephdwyer.katana"
version = "0.0.1"
base.archivesBaseName = "katana-compiler-plugin"

repositories {
    mavenCentral()
    jcenter()
    maven {
        url = uri("https://plugins.gradle.org/m2/")
    }
}

plugins {
    kotlin("jvm") version "1.4.10"
    kotlin("kapt") version "1.4.10"
    id("maven-publish")
    id("com.jfrog.bintray") version "1.8.5"
    id("com.github.johnrengelman.shadow") version "6.0.0"
    id("java")
}

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-compiler")
    compileOnly("com.google.auto.service:auto-service:1.0-rc4")
    kapt("com.google.auto.service:auto-service:1.0-rc4")
    compileOnly("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.5")
    implementation("com.google.code.gson:gson:2.8.6")
}

// by default the output jar is {name}-all we want it to replace the main jar
tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>().configureEach {
    classifier = ""
}

val artifactName = "katana-compiler-plugin"
val artifactGroup = project.group.toString()
val artifactVersion = project.version.toString()

val pomUrl = "https://github.com/josephdwyer/katana"
val pomScmUrl = "https://github.com/josephdwyer/katana"
val pomIssueUrl = "https://github.com/josephdwyer/katana/issues"
val pomDesc = "https://github.com/josephdwyer/katana"

val githubRepo = "josephdwyer/katana"
val githubReadme = "README.md"

val pomLicenseName = "MIT"
val pomLicenseUrl = "https://opensource.org/licenses/mit-license.php"
val pomLicenseDist = "repo"

val pomDeveloperId = "josephdwyer"
val pomDeveloperName = "Joseph Dwyer"


publishing {
    publications {
        create<MavenPublication>("katana") {
            groupId = artifactGroup
            artifactId = artifactName
            version = artifactVersion
            from(components["java"])

            pom.withXml {
                asNode().apply {
                    appendNode("description", pomDesc)
                    appendNode("name", rootProject.name)
                    appendNode("url", pomUrl)
                    appendNode("licenses").appendNode("license").apply {
                        appendNode("name", pomLicenseName)
                        appendNode("url", pomLicenseUrl)
                        appendNode("distribution", pomLicenseDist)
                    }
                    appendNode("developers").appendNode("developer").apply {
                        appendNode("id", pomDeveloperId)
                        appendNode("name", pomDeveloperName)
                    }
                    appendNode("scm").apply {
                        appendNode("url", pomScmUrl)
                    }
                }
            }
        }
    }
}

bintray {
    user = System.getenv("BINTRAY_USER")
    key = System.getenv("BINTRAY_API_KEY")
    publish = true

    setPublications("katana")

    pkg.apply {
        repo = "maven"
        name = artifactName
        userOrg = "josephdwyer"
        githubRepo = githubRepo
        vcsUrl = pomScmUrl
        description = "Kotlin compiler plugin that dumps out type information"
        setLabels("kotlin/native", "compiler")
        setLicenses("MIT")
        desc = description
        websiteUrl = pomUrl
        issueTrackerUrl = pomIssueUrl

        version.apply {
            name = artifactVersion
            desc = pomDesc
            released = Date().toString()
            vcsTag = artifactVersion
        }
    }
}
