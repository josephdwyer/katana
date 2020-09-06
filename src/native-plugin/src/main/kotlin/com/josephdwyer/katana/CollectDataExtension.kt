package com.josephdwyer.katana

import com.google.gson.GsonBuilder
import org.jetbrains.kotlin.backend.common.FunctionLoweringPass
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.ir.allParameters
import org.jetbrains.kotlin.backend.common.runOnFilePostfix
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.path
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.js.descriptorUtils.getJetTypeFqName
import org.jetbrains.kotlin.konan.file.File
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.types.typeUtil.nullability

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

private class OnFunction(
        val context: IrPluginContext
) :
        IrElementTransformerVoid(), FunctionLoweringPass {

    val collected = mutableListOf<FunctionJson>()

    override fun lower(irFunction: IrFunction) {

        val visibility = irFunction.visibility.toString()
        if (visibility != "public") {
            // we only care about public ones
            return
        }

        // add file section
        val startLine = irFunction.fileEntry.getLineNumber(irFunction.startOffset)
        val endLine = irFunction.fileEntry.getLineNumber(irFunction.endOffset)


        val file = FileJson(irFunction.file.path, startLine, endLine)

        val functionName = irFunction.name.toString()

        val parameters = irFunction.allParameters.map { parameter ->
            val type = parameter.type as IrSimpleType
            val typeAsClass = parameter.type.getClass()?.let { getClassInfo(it) }

            val typeParameters = type.toKotlinType()
                    .arguments
                    .map {
                        val typeNullability = it.type.nullability()
                        val typeName = it.type.getJetTypeFqName(true)
                        TypeParameterJson(typeName, typeNullability)
                    }

            FunctionParameterJson(
                    parameter.name.toString(),
                    TypeJson(type.classifier.descriptor.fqNameSafe.toString(), typeParameters, type.isNullable(), typeAsClass))
        }

        val returnTypeTypeParameters = irFunction.returnType.toKotlinType()
                .arguments
                .map {
                    val returnTypeNullability = it.type.nullability()
                    val typeName = it.type.getJetTypeFqName(true)
                    TypeParameterJson(typeName, returnTypeNullability)
                }

        val returnTypeClass = irFunction.returnType.getClass()?.let {
            getClassInfo(it)
        }

        val returnType = TypeJson(
                irFunction.returnType.classifierOrNull?.descriptor?.fqNameSafe?.toString() ?: "Unit",
                returnTypeTypeParameters,
                irFunction.returnType.isNullable(),
                returnTypeClass)

        val classInfo = irFunction.parentClassOrNull?.let { getClassInfo(it) }

        // maybe a simpler way to get the package?
        // irFunction.file.packageFragmentDescriptor

        // if this function is in a class, just take the class's package
        // otherwise it is the fully qualified name minus the function's name
        val packageName = classInfo?.let { it.packageName } ?:
        irFunction.fqNameWhenAvailable.toString()
                .removeSuffix(irFunction.name.toString())
                .removeSuffix(".")

        var parent: String = irFunction.parent.let {
            it.fqNameForIrSerialization.toString()
        }

        val function = FunctionJson(file, packageName, functionName,
                visibility, parameters, returnType,
                parent, classInfo, irFunction.isExpect)

        collected.add(function)
    }

    private fun getClassInfo(irClass: IrClass) : ClassJson {
        // we are trying to remove ".className" from "some.namespace.className"
        // but, if the class is not in a namespace (thus using the default "<root>" namespace
        // there won't be any leading content - it will just be "className", so we need to remove in 2 stages
        val packageName = irClass.fqNameWhenAvailable.toString()
                .removeSuffix(irClass.name.toString())
                .removeSuffix(".")

        // should we default it to "<root>" if it is now empty or leave it to the consumer?

        val superTypes = irClass.superTypes.map {
            (it.classifierOrNull?.descriptor?.fqNameSafe.toString())
        }

        return ClassJson(
                irClass.name.toString(),
                irClass.isCompanion,
                irClass.isData,
                irClass.isInline,
                irClass.isExpect,
                irClass.kind,
                superTypes,
                packageName)
    }
}
