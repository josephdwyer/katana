package com.josephdwyer.katana

import com.google.auto.service.AutoService
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

@AutoService(CommandLineProcessor::class) // don't forget!
class NativeCommandLineProcessor : CommandLineProcessor {

    override val pluginId: String = "katanaPlugin"

    override val pluginOptions: Collection<CliOption> = listOf(
        CliOption(
            optionName = "outputFile", valueDescription = "path/to/file.json",
            description = "the file to output the compiler information to"
        )
    )

    override fun processOption(
        option: AbstractCliOption,
        value: String,
        configuration: CompilerConfiguration
    ) {
        return when (option.optionName) {
            "outputFile" -> configuration.put(OUTPUT_FILE, value)
            else -> configuration.put(OUTPUT_FILE, "")
        }
    }
}

val OUTPUT_FILE = CompilerConfigurationKey<String>("the file to output the compiler information to")
