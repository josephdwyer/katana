package com.josephdwyer.katana.gradle

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.compile.AbstractCompile
import org.jetbrains.kotlin.gradle.plugin.*

class KatanaPlugin : KotlinCompilerPluginSupportPlugin {

    override fun apply(target: Project) {
        target.tasks.withType(AbstractCompile::class.java).configureEach { task ->
            task.extensions.create("katana", KatanaPluginExtension::class.java, target)
        }
    }

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = true

    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.target.project
        val taskExtension = kotlinCompilation.compileKotlinTask.extensions.getByType(KatanaPluginExtension::class.java)
        return project.provider {
            listOf(
                SubpluginOption(key = "outputFile", value = taskExtension.outputFile)
            )
        }
    }

    /**
     * Just needs to be consistent with the pluginId from native-plugin's NativeCommandLineProcessor
     */
    override fun getCompilerPluginId(): String = "com.josephdwyer.katana.plugin"

    // This points to our JVM plugin that is published on bintray
    // If you publish a new version, you need to manually bump the version here
    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = "com.joseph-dwyer.katana",
        artifactId = "jvm-katana-compiler-plugin",
        version = "0.0.3"
    )

    // This points to our native plugin that is published on bintray
    // If you publish a new version, you need to manually bump the version here
    override fun getPluginArtifactForNative(): SubpluginArtifact = SubpluginArtifact(
        groupId = "com.joseph-dwyer.katana",
        artifactId = "katana-compiler-plugin",
        version = "0.0.6"
    )
}
