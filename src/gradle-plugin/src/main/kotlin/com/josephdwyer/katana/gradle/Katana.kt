package com.josephdwyer.katana.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class Katana : Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.create("katana", KatanaPluginExtension::class.java)
    }
}
