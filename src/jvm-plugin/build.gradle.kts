import java.net.URI

// this project does not actually do anything, but we have to have something published
// so that compiling projects with JVM things will work

group = "com.joseph-dwyer.katana"
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
    kotlin("jvm") version "1.5.21"
    id("maven-publish")
    id("java")
    id("signing")
    id("org.jetbrains.dokka") version "0.9.18"
}

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-compiler")
}

fun isReleaseBuild(): Boolean {
    return !project.version.toString().contains("SNAPSHOT")
}

fun getReleaseRepositoryUrl(): URI {
    return URI("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
}

fun getSnapshotRepositoryUrl(): URI {
    return URI("https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

fun getRepositoryUsername(): String {
    return System.getenv("SONATYPE_USERNAME") ?: ""
}

fun getRepositoryPassword() : String {
    return System.getenv("SONATYPE_PASSWORD") ?: ""
}

java {
    withSourcesJar()
}

tasks.dokka {
}

val javadocsJarProvider = tasks.register("javadocsJar", Jar::class) {
    dependsOn(tasks.named("dokka"))

    archiveClassifier.set("javadoc")
    from(tasks.withType(org.jetbrains.dokka.gradle.DokkaTask::class).first().outputDirectory)
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

            artifact(javadocsJarProvider.get())

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
        repositories {
            maven {
                url = if (isReleaseBuild()) getReleaseRepositoryUrl() else getSnapshotRepositoryUrl()
                credentials {
                    username = getRepositoryUsername()
                    password = getRepositoryPassword()
                }
            }
        }
    }
}


signing {
    setRequired({ isReleaseBuild() })
    val signingKeyId = System.getenv("SIGNING_KEYID")
    val signingKey = System.getenv("SIGNING_KEY")
    val signingPassword = System.getenv("SIGNING_PASSWORD")
    useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
    sign(publishing.publications)
}

tasks.withType<Sign>().configureEach {
    onlyIf { isReleaseBuild() }
}