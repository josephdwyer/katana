package com.josephdwyer.katana.gradle

import com.google.auto.service.AutoService
import org.gradle.api.Project
import org.gradle.api.tasks.compile.AbstractCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonOptions
import org.jetbrains.kotlin.gradle.plugin.*

@AutoService(KotlinGradleSubplugin::class) // don't forget!
class KatanaSubplugin : KotlinGradleSubplugin<AbstractCompile> {
    override fun apply(
        project: Project,
        kotlinCompile: AbstractCompile,
        javaCompile: AbstractCompile?,
        variantData: Any?,
        androidProjectHandler: Any?,
        kotlinCompilation: KotlinCompilation<KotlinCommonOptions>?
    ): List<SubpluginOption> {
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
        var applicable = project.plugins.hasPlugin(Katana::class.java)
        return applicable
    }

    /**
     * Just needs to be consistent with the key for NativeCommandLineProcessor#pluginId
     */
    override fun getCompilerPluginId(): String = "katanaPlugin"


    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = "com.josephdwyer",
        artifactId = "not-supported-compiler-plugin",
        version = "0.0.1" // remember to bump this version before any release!
    )

    override fun getNativeCompilerPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = "com.josephdwyer.katana",
        artifactId = "katana-compiler-plugin",
        version = "0.0.1" // remember to bump this version before any release!
    )
}


/*

This is supposed to be the new way to do sub-plugins, but it wasn't working out of the gate
so we will have to circle back on this later.


class KatanaSubplugin : KotlinCompilerPluginSupportPlugin {


    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        println("KatanaSubplugin loaded")
        var outFilePath : String

        val byName = kotlinCompilation.target.project.extensions.findByName("katana")
        if (byName is KatanaPluginExtension) {
            outFilePath = byName.outputFile
        }
        else {
            var extension = kotlinCompilation.target.project.extensions.findByType(KatanaPluginExtension::class.java) ?: KatanaPluginExtension()
            outFilePath = extension.outputFile
        }

        val outputFileOption = SubpluginOption(key = "outputFile", value = outFilePath)

        return kotlinCompilation.target.project.provider {
            listOf(outputFileOption)
        }
    }

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = kotlinCompilation.target.project.plugins.hasPlugin(Katana::class.java)

    /**
     * Just needs to be consistent with the key for NativeCommandLineProcessor#pluginId
     */
    override fun getCompilerPluginId(): String = "katanaPlugin"


    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = "com.josephdwyer.katana",
        artifactId = "not-supported-compiler-plugin",
        version = "0.0.1" // remember to bump this version before any release!
    )

    override fun getPluginArtifactForNative(): SubpluginArtifact = SubpluginArtifact(
        groupId = "com.josephdwyer.katana",
        artifactId = "katana-compiler-plugin",
        version = "0.0.1" // remember to bump this version before any release!
    )
}
 */

