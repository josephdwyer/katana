package com.josephdwyer.katana.gradle

import com.google.auto.service.AutoService
import com.sun.org.apache.xpath.internal.operations.Bool
import org.gradle.api.Project
import org.gradle.api.tasks.compile.AbstractCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonOptions
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinGradleSubplugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

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

        var outFilePath : String

        val byName = project.extensions.findByName("katana")
        if (byName is KatanaPluginExtension) {
            outFilePath = byName.outputFile
        }
        else {
            var extension = project.extensions.findByType(KatanaPluginExtension::class.java) ?: KatanaPluginExtension()
            outFilePath = extension.outputFile
        }

        println("Katana Output File: $outFilePath")

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
