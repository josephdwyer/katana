import java.net.URI

group = "com.joseph-dwyer.katana"
version = "0.0.6"
base.archivesBaseName = "katana-compiler-plugin"

repositories {
    mavenCentral()
    jcenter()
    maven {
        url = uri("https://plugins.gradle.org/m2/")
    }
}

plugins {
    kotlin("jvm") version "1.5.21"
    kotlin("kapt") version "1.5.21"
    id("maven-publish")
    id("com.github.johnrengelman.shadow") version "6.0.0"
    id("java")
    id("signing")
    id("org.jetbrains.dokka") version "1.5.0"
}

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-compiler")
    compileOnly("com.google.auto.service:auto-service:1.0-rc4")
    kapt("com.google.auto.service:auto-service:1.0-rc4")
    implementation("com.google.code.gson:gson:2.8.6")
}


tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>().configureEach {
    // by default the output jar is {name}-all we want it to replace the main jar
    classifier = ""

    // relocate will avoid conflicts if the project importing this is also using gson
    relocate("com.google.code.gson", "com.josephdwyer.katana.gson")
}


java {
    withSourcesJar()
}

tasks.withType<org.jetbrains.dokka.gradle.DokkaTask>().configureEach {

}

val javadocsJarProvider = tasks.register("javadocsJar", Jar::class) {
    dependsOn(tasks.named("dokkaJavadoc"))

    archiveClassifier.set("javadoc")
    from(tasks.withType(org.jetbrains.dokka.gradle.DokkaTask::class).first().outputDirectory)
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

val artifactName = "katana-compiler-plugin"
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

            artifact(tasks.named("javadocsJar").get())
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