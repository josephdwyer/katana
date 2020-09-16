package com.josephdwyer.katana

import com.google.auto.service.AutoService
import com.google.gson.GsonBuilder
import com.intellij.mock.MockProject
import org.jetbrains.kotlin.backend.common.FunctionLoweringPass
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.ir.allParameters
import org.jetbrains.kotlin.backend.common.runOnFilePostfix
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.path
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.classifierOrNull
import org.jetbrains.kotlin.ir.types.isNullable
import org.jetbrains.kotlin.ir.util.file
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

        IrGenerationExtension.registerExtension(project, CollectDataExtension(configuration))
    }
}

open class CollectDataExtension(private val configuration: CompilerConfiguration) : IrGenerationExtension {

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext)    {
        val visitor = OnFunction(pluginContext)

        for (file in moduleFragment.files) {
            visitor.runOnFilePostfix(file)
        }

        // for some reason this is called 2 times, one without any data
        if (visitor.collected.any()) {
            configuration[OUTPUT_FILE]?.let {
                val gson = GsonBuilder().setPrettyPrinting().create()
                val data = gson.toJson(visitor.collected)
                File(it).writeText(data)
                println("Katana data written to $it")
            }
        }
    }

}

data class FileInfo(
        val filePath: String,
        val startLine: Int?,
        val endLine: Int?
)

data class FunctionParameter(
        val name: String,
        val type: TypeInfo
)

data class TypeInfo(
        val name: String,
        val nullable: Boolean
)

data class FunctionInfo(
        val file: FileInfo,
        val name: String,
        val typeParameters: Array<String?>?,
        val parameters: Array<FunctionParameter>?,
        val returnType: TypeInfo
)

private class OnFunction(
    val context: IrPluginContext
) :
    IrElementTransformerVoid(), FunctionLoweringPass {

    val collected = mutableListOf<FunctionInfo>()

    override fun lower(irFunction: IrFunction) {
        val sb = StringBuilder()

        // we only care about public ones
        // function.visibility

        val data = mutableListOf<String?>()

        // add file section
        val startLine = irFunction.fileEntry.getLineNumber(irFunction.startOffset)
        val endLine = irFunction.fileEntry.getLineNumber(irFunction.endOffset)

        val file = FileInfo(irFunction.file.path, startLine, endLine)

        // - (improvement) Figure out how to get the interfaces that something implements
        // - Figure out some way to deterministically link to the header file entry (if possible)
        val functionName = irFunction.fqNameWhenAvailable?.toString() ?: return

        val typeParameters = irFunction.typeParameters.map { it.fqNameWhenAvailable?.toString() }.toTypedArray()

        val parameters = irFunction.allParameters.map {
            val type = it.type as IrSimpleType
            FunctionParameter(it.name.toString(), TypeInfo(type.classifier.descriptor.fqNameSafe.toString(), type.isNullable()))
        }.toTypedArray()

        val returnType = TypeInfo(irFunction.returnType.classifierOrNull?.descriptor?.fqNameSafe?.toString() ?: "Unit", irFunction.returnType.isNullable())

        val function = FunctionInfo(file, functionName, typeParameters, parameters, returnType)

        collected.add(function)

        /*
        irFunction.parent.let {
            if (it is IrClass) {
                it.superTypes.forEach {
                    sb.append(" ${it.classifierOrNull?.descriptor?.fqNameSafe}")
                }
            }
        }
         */
    }
}