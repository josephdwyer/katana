import org.jetbrains.kotlin.kapt3.base.Kapt.kapt
import java.util.Date

group = "com.josephdwyer.katana"
version = "0.0.1"
base.archivesBaseName = "katana-compiler-plugin"

repositories {
    mavenCentral()
    jcenter()
}

plugins {
    kotlin("jvm") version "1.3.72"
    kotlin("kapt") version "1.3.72"
    id("maven-publish")
    id("com.jfrog.bintray") version "1.8.5"
}

dependencies {
    //implementation(kotlin("stdlib", "1.4.0"))
    //testCompile(gradleTestKit())
    //testCompile("junit:junit:4+")
    compileOnly("org.jetbrains.kotlin:kotlin-compiler")
    compileOnly("com.google.auto.service:auto-service:1.0-rc4")
    kapt("com.google.auto.service:auto-service:1.0-rc4")
    compileOnly("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.5")
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
