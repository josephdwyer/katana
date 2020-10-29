package com.josephdwyer.katana

import com.google.gson.GsonBuilder
import org.jetbrains.kotlin.backend.common.FunctionLoweringPass
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.ir.allParameters
import org.jetbrains.kotlin.backend.common.runOnFilePostfix
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.konan.file.File

open class CollectDataExtension(private val configuration: CompilerConfiguration) : IrGenerationExtension {

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        val visitor = OnFunction(pluginContext)

        for (file in moduleFragment.files) {
            visitor.runOnFilePostfix(file)
        }

        // for some reason this is called 2 times, one without any data
        if (visitor.functions.any()) {
            configuration[OUTPUT_FILE]?.let {
                val katanaJson = KatanaJson(visitor.classes, visitor.functions)
                val gson = GsonBuilder().setPrettyPrinting().create()
                val data = gson.toJson(katanaJson)
                File(it).writeText(data)
                println("Katana data written to $it")
            }
        }
    }
}

private class OnFunction(val context: IrPluginContext) : IrElementTransformerVoid(), FunctionLoweringPass {

    val functions = mutableListOf<FunctionJson>()
    val classes = mutableMapOf<String, ClassJson>()

    override fun lower(irFunction: IrFunction) {

        if (!shouldIncludeFunction(irFunction)) {
            return
        }

        // add file section
        val startLine = irFunction.fileEntry.getLineNumber(irFunction.startOffset)
        val endLine = irFunction.fileEntry.getLineNumber(irFunction.endOffset)

        val file = FileJson(irFunction.file.path, startLine, endLine)

        val functionName = irFunction.name.toString()

        val parameters = irFunction.allParameters.map { parameter ->
            tryAddClassJson(parameter.type.getClass())

            FunctionParameterJson(
                    parameter.name.toString(),
                    getTypeJson(parameter.type as IrSimpleType))
        }

        val returnType = getTypeJson(irFunction.returnType as IrSimpleType)

        val parentClass = irFunction.parentClassOrNull

        tryAddClassJson(irFunction.returnType.getClass())
        tryAddClassJson(parentClass)

        val function = FunctionJson(file, getPackage(irFunction), functionName,
                irFunction.visibility.toString(), parameters, returnType,
                parentClass?.fqNameForIrSerialization?.toString(), irFunction is IrConstructor)

        functions.add(function)
    }

    private fun getClassJson(irClass: IrClass): ClassJson {
        val superTypes = irClass.superTypes.map {
            getTypeJson(it as IrSimpleType)
        }

        return ClassJson(
                irClass.name.toString(),
                irClass.isCompanion,
                irClass.isData,
                irClass.isInline,
                irClass.kind,
                superTypes,
                getPackage(irClass))
    }

    private fun tryAddClassJson(irClass: IrClass?) {
        if (irClass != null && shouldIncludeClass(irClass)) {
            classes.putIfAbsent(irClass.fqNameWhenAvailable.toString(), getClassJson(irClass))

            for (superType in irClass.superTypes) {
                tryAddClassJson(superType.getClass())
            }
        }
    }

    private fun shouldIncludeClass(irClass: IrClass): Boolean {
        // we only care about public actual ones
        return !irClass.isExpect && irClass.visibility.toString() == "public"
    }

    private fun shouldIncludeFunction(irFunction: IrFunction): Boolean {
        // we only care about public actual ones whose classes are also public and actual
        val parentClass = irFunction.parentClassOrNull
        return !irFunction.isExpect &&
                irFunction.visibility.toString() == "public" &&
                (parentClass == null || shouldIncludeClass(parentClass))
    }

    private fun getPackage(irElement: IrElement): String {
        return irElement.getPackageFragment()?.fqNameForIrSerialization.toString()
    }

    private fun getFullyQualifiedName(irSimpleType: IrSimpleType): String {
        return (irSimpleType.classifier.owner as IrDeclarationWithName).fqNameWhenAvailable.toString()
    }

    private fun getTypeArgumentJson(type: IrTypeArgument): TypeArgumentJson {
        return when (type) {
            is IrStarProjection -> TypeArgumentJson(isStar = true, null, null)
            is IrTypeProjection -> {
                val projectedType = type.type as IrSimpleType
                TypeArgumentJson(isStar = false, getTypeJson(projectedType), type.variance.label)
            }
            else -> throw AssertionError("Unexpected type argument $type ${type.render()}")
        }
    }

    private fun getTypeArguments(type: IrSimpleType): List<TypeArgumentJson> {
        return type.arguments.map { getTypeArgumentJson(it) }
    }

    private fun getTypeJson(type: IrSimpleType): TypeJson {
        val nullable = type.isNullable()
        val typeName = getFullyQualifiedName(type)
        return TypeJson(typeName, getTypeArguments(type), nullable)
    }
}
