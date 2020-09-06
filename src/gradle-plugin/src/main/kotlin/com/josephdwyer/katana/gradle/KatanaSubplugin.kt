package com.josephdwyer.katana.gradle

import com.google.auto.service.AutoService
import org.gradle.api.Project
import org.gradle.api.tasks.compile.AbstractCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonOptions
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinGradleSubplugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

@AutoService(KatanaGradleSubplugin::class) // don't forget!
class KatanaGradleSubplugin : KotlinGradleSubplugin<AbstractCompile> {
    override fun apply(
        project: Project,
        kotlinCompile: AbstractCompile,
        javaCompile: AbstractCompile?,
        variantData: Any?,
        androidProjectHandler: Any?,
        kotlinCompilation: KotlinCompilation<KotlinCommonOptions>?
    ): List<SubpluginOption> {
        val extension = project.extensions.findByType(KatanaPluginExtension::class.java)
            ?: KatanaPluginExtension()

        val outputFileOption = SubpluginOption(key = "outputFile", value = extension.outputFile)
        return listOf(outputFileOption)
    }

    override fun isApplicable(project: Project, task: AbstractCompile) =
        project.plugins.hasPlugin(Katana::class.java)


    //override fun getArtifactName() = "native-plugin"
    //override fun getGroupName() = "com.josephdwyer"

    /**
     * Just needs to be consistent with the key for NativeCommandLineProcessor#pluginId
     */
    override fun getCompilerPluginId(): String = "katanaPlugin"


    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = "com.josephdwyer",
        artifactId = "kotlin-compiler-plugin",
        version = "0.0.1" // remember to bump this version before any release!
    )

    override fun getNativeCompilerPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = "com.josephdwyer",
        artifactId = "native-plugin",
        version = "0.0.1" // remember to bump this version before any release!
    )
}
