package com.josephdwyer.katana

import com.google.auto.service.AutoService
import com.intellij.mock.MockProject
import org.jetbrains.kotlin.backend.common.FunctionLoweringPass
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.ir.allParameters
import org.jetbrains.kotlin.backend.common.runOnFilePostfix
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.classifierOrNull
import org.jetbrains.kotlin.ir.types.isNullable
import org.jetbrains.kotlin.ir.util.fileEntry
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.konan.file.File
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe


@AutoService(ComponentRegistrar::class)
class NativeTestComponentRegistrar : ComponentRegistrar {
    override fun registerProjectComponents(project: MockProject, configuration: CompilerConfiguration) {

        if (configuration[OUTPUT_FILE] == "") {
            return
        }

        println("Hey I am a compiler plugin")
        val messageCollector: MessageCollector = configuration.get(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)

        IrGenerationExtension.registerExtension(project, CollectDataExtension(messageCollector, configuration))
    }
}

open class CollectDataExtension(private val messageCollector: MessageCollector, private val configuration: CompilerConfiguration) : IrGenerationExtension {

    private val data = StringBuilder()

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext)    {
        val serializerClassLowering = OnFunction(data, pluginContext)
        for (file in moduleFragment.files) {
            serializerClassLowering.runOnFilePostfix(file)
        }

        configuration[OUTPUT_FILE]?.let {
            File(it).writeText(data.toString())
            println("Data written to $it")
        }

        messageCollector.report(
            CompilerMessageSeverity.WARNING,
            "*** Native Plugin: $data"
        )
    }
}

private class OnFunction(
    val data: StringBuilder,
    val context: IrPluginContext
) :
    IrElementTransformerVoid(), FunctionLoweringPass {

    override fun lower(irFunction: IrFunction) {
        val sb = StringBuilder()

        // we only care about public ones
        // function.visibility

        sb.appendln("{")

        // add file section
        val startLine = irFunction.fileEntry.getLineNumber(irFunction.startOffset)
        val endLine = irFunction.fileEntry.getLineNumber(irFunction.endOffset)
        sb.appendln("  \"file\": { ")
        //sb.appendln("    \"path\": \"${irFunction.file.path}\",")
        sb.appendln("    \"startLine\": $startLine,")
        sb.appendln("    \"endLine\": $endLine")
        sb.appendln("  }")

        sb.appendln("}")

        // TODO:
        // - Make the rest of this info into JSON format
        // - Save it to a file rather than writing to console
        // - Figure out how to hook this into PGFoundations build
        // - (improvement) Figure out how to get the interfaces that something implements
        // - Figure out some way to deterministically link to the header file entry (if possible)

        sb.append("${irFunction.fqNameWhenAvailable}")

        if (irFunction.typeParameters.any()) {
            sb.append("<")
            for (p in irFunction.typeParameters) {
                sb.append("${p.fqNameWhenAvailable}")
            }
            sb.append(">")
        }

        sb.append("(")
        for (p in irFunction.allParameters) {
            val type = p.type as IrSimpleType

            sb.append("${p.name}: ${type.classifier.descriptor.fqNameSafe} nullable: ${type.isNullable()}")
        }
        sb.append(")")

        sb.append(": ${irFunction.returnType.classifierOrNull?.descriptor?.fqNameSafe} nullable: ${irFunction.returnType.isNullable()}")

        irFunction.parent.let {
            if (it is IrClass) {
                it.superTypes.forEach {
                    sb.append(" ${it.classifierOrNull?.descriptor?.fqNameSafe}")
                }
            }
        }

        data.appendln(sb)
    }
}