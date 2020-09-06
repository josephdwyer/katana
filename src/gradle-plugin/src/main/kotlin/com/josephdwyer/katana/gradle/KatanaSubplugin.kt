package com.josephdwyer.katana.gradle

import com.google.auto.service.AutoService
import org.gradle.api.Project
import org.gradle.api.tasks.compile.AbstractCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonOptions
import org.jetbrains.kotlin.gradle.plugin.*

@AutoService(KotlinGradleSubplugin::class)
class KatanaSubplugin : KotlinGradleSubplugin<AbstractCompile> {

    override fun apply(
        project: Project,
        kotlinCompile: AbstractCompile,
        javaCompile: AbstractCompile?,
        variantData: Any?,
        androidProjectHandler: Any?,
        kotlinCompilation: KotlinCompilation<KotlinCommonOptions>?
    ): List<SubpluginOption> {

        // first, try to find our extension by name which seems reliable
        // but, some examples are finding by type, so we will fall back to that
        val byName = project.extensions.findByName("katana")
        val outFilePath = if (byName is KatanaPluginExtension) {
            byName.outputFile
        } else {
            var extension = project.extensions.findByType(KatanaPluginExtension::class.java) ?: KatanaPluginExtension()
            extension.outputFile
        }

        val outputFileOption = SubpluginOption(key = "outputFile", value = outFilePath)
        return listOf(outputFileOption)
    }

    override fun isApplicable(project: Project, task: AbstractCompile) : Boolean {
        return project.plugins.hasPlugin(Katana::class.java)
    }

    /**
     * Just needs to be consistent with the pluginId from native-plugin's NativeCommandLineProcessor
     */
    override fun getCompilerPluginId(): String = "com.josephdwyer.katana.plugin"


    // This points to our JVM plugin that is published on bintray
    // If you publish a new version, you need to manually bump the version here
    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = "com.josephdwyer.katana",
        artifactId = "jvm-katana-compiler-plugin",
        version = "0.0.2"
    )

    // This points to our native plugin that is published on bintray
    // If you publish a new version, you need to manually bump the version here
    override fun getNativeCompilerPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = "com.josephdwyer.katana",
        artifactId = "katana-compiler-plugin",
        version = "0.0.2"
    )
}

/*

This is supposed to be the new way to do sub-plugins, but it wasn't working out of the gate
so we will have to circle back on this later.

class KatanaSubplugin : KotlinCompilerPluginSupportPlugin {
}
 */

