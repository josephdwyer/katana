package com.josephdwyer.katana.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class Katana : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("katanaPlugin", KatanaPluginExtension::class.java)

        project.extensions.create(
            "katanaPlugin",
            KatanaPluginExtension::class.java
        )
    }
}
