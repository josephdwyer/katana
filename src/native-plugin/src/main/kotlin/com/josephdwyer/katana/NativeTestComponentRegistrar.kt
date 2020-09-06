package com.josephdwyer.katana

import com.google.auto.service.AutoService
import com.intellij.mock.MockProject
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration

@AutoService(ComponentRegistrar::class)
class NativeTestComponentRegistrar : ComponentRegistrar {
    override fun registerProjectComponents(project: MockProject, configuration: CompilerConfiguration) {

        if (configuration[OUTPUT_FILE] == "") {
            return
        }

        println("Hi, I am your friendly neighborhood compiler plugin Katana!")

        IrGenerationExtension.registerExtension(project, CollectDataExtension(configuration))
    }
}