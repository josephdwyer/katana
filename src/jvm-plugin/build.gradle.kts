import java.util.Date

// this project does not actually do anything, but we have to have something published
// so that compiling projects with JVM things will work

group = "com.josephdwyer.katana"
version = "0.0.3"
base.archivesBaseName = "jvm-katana-compiler-plugin"

repositories {
    mavenCentral()
    jcenter()
    maven {
        url = uri("https://plugins.gradle.org/m2/")
    }
}

plugins {
    kotlin("jvm") version "1.4.10"
    id("maven-publish")
    id("com.jfrog.bintray") version "1.8.5"
    id("java")
}

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-compiler")
    compileOnly("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.5")
}

val artifactName = "jvm-katana-compiler-plugin"
val artifactGroup = project.group.toString()
val artifactVersion = project.version.toString()

val pomUrl ="https://github.com/josephdwyer/katana"
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

            pom {
                name.set(rootProject.name)
                description.set(pomDesc)
                url.set(pomUrl)

                licenses {
                    license {
                        name.set(pomLicenseName)
                        url.set(pomLicenseUrl)
                        distribution.set(pomLicenseDist)
                    }
                }

                developers {
                    developer {
                        id.set(pomDeveloperId)
                        name.set(pomDeveloperName)
                    }
                }

                scm {
                    url.set(pomScmUrl)
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
