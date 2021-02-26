package com.josephdwyer.katana.gradle

import org.gradle.api.Project

open class KatanaPluginExtension(project: Project) {
    /**
        The (fully qualified) file path to write the Katana data to, should be a JSON file
     */
    var outputFile: String = ""
}
