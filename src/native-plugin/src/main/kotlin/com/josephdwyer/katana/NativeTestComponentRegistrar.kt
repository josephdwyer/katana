package com.josephdwyer.katana

import com.google.auto.service.AutoService
import com.google.gson.GsonBuilder
import com.intellij.mock.MockProject
import com.sun.jna.platform.win32.WinDef
import com.sun.org.apache.xpath.internal.operations.Bool
import org.jetbrains.kotlin.backend.common.FunctionLoweringPass
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.ir.allParameters
import org.jetbrains.kotlin.backend.common.runOnFilePostfix
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.path
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.js.descriptorUtils.getJetTypeFqName
import org.jetbrains.kotlin.js.descriptorUtils.nameIfStandardType
import org.jetbrains.kotlin.konan.file.File
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.types.model.typeConstructor
import org.jetbrains.kotlin.types.typeUtil.TypeNullability
import org.jetbrains.kotlin.types.typeUtil.nullability

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

data class TypeParameter(
        val type: String,
        val nullability: TypeNullability
)

data class TypeInfo(
        val name: String,
        val typeParameters: List<TypeParameter>,
        val nullable: Boolean,
        val classInfo: ClassInfo?
)

data class ClassInfo(
        val name: String,
        val isCompanion: Boolean,
        val isData: Boolean,
        val isInline: Boolean,
        val isExpect: Boolean,
        val kind: ClassKind,
        val superClasses: List<String>,
        val packageName: String
)

data class FunctionInfo(
        val file: FileInfo,
        val packageName: String,
        val name: String,
        val visibility: String,
        val parameters: List<FunctionParameter>?,
        val returnType: TypeInfo,
        val parent: String,
        val classInfo: ClassInfo?,
        val isExpect: Boolean
)

private class OnFunction(
    val context: IrPluginContext
) :
    IrElementTransformerVoid(), FunctionLoweringPass {

    val collected = mutableListOf<FunctionInfo>()

    override fun lower(irFunction: IrFunction) {
        // we only care about public ones

        val visibility = irFunction.visibility.toString()

        if (visibility != "public") {
            // we only care about public ones
            return
        }

        // add file section
        val startLine = irFunction.fileEntry.getLineNumber(irFunction.startOffset)
        val endLine = irFunction.fileEntry.getLineNumber(irFunction.endOffset)


        // may be a simpler way to get the package?
        // irFunction.file.packageFragmentDescriptor

        val file = FileInfo(irFunction.file.path, startLine, endLine)

        val functionName = irFunction.name.toString()

        /*
        val typeParameters = irFunction.typeParameters
                .map {
                    val typeName = it.fqNameWhenAvailable.toString()
                    TypeParameter(typeName, TypeNullability.FLEXIBLE)

               }
         */

        val parameters = irFunction.allParameters.map { parameter ->
            val type = parameter.type as IrSimpleType
            val typeAsClass = parameter.type.getClass()?.let { getClassInfo(it) }

            val typeParameters = type.toKotlinType()
                    .arguments
                    .map {
                        val typeNullability = it.type.nullability()
                        val typeName = it.type.getJetTypeFqName(true)
                        TypeParameter(typeName, typeNullability)
                    }

            FunctionParameter(
                    parameter.name.toString(),
                    TypeInfo(type.classifier.descriptor.fqNameSafe.toString(), typeParameters, type.isNullable(), typeAsClass))
        }


        val returnTypeTypeParameters = irFunction.returnType.toKotlinType()
                .arguments
                .map {
                    val returnTypeNullability = it.type.nullability()
                    val typeName = it.type.getJetTypeFqName(true)
                    TypeParameter(typeName, returnTypeNullability)
                }

        val returnTypeClass = irFunction.returnType.getClass()?.let {
            getClassInfo(it)
        }

        val returnType = TypeInfo(irFunction.returnType.classifierOrNull?.descriptor?.fqNameSafe?.toString() ?: "Unit", returnTypeTypeParameters, irFunction.returnType.isNullable(), returnTypeClass)

        // - (improvement) Figure out how to get the interfaces that something implements
        // - Figure out some way to deterministically link to the header file entry (if possible)

        val classInfo = irFunction.parentClassOrNull?.let { getClassInfo(it) }

        // if this function is in a class, just take the class's package
        // otherwise it is the fully qualified name minus the function's name
        val packageName = classInfo?.let { it.packageName } ?:
                irFunction.fqNameWhenAvailable.toString()
                .removeSuffix(irFunction.name.toString())
                .removeSuffix(".")

        var parent: String = irFunction.parent.let {
            it.fqNameForIrSerialization.toString()
        }

        val function = FunctionInfo(file, packageName, functionName,
                visibility, parameters, returnType,
                parent, classInfo, irFunction.isExpect)

        collected.add(function)
    }

    private fun getClassInfo(irClass: IrClass) : ClassInfo {
        // we are trying to remove ".className" from "some.namespace.className"
        // but, if the class is not in a namespace (thus using the default "<root>" namespace
        // there won't be any leading content - it will just be "className", so we need to remove in 2 stages
        val packageName = irClass.fqNameWhenAvailable.toString()
                .removeSuffix(irClass.name.toString())
                .removeSuffix(".")
        // should we default it to "<root>" if it is now empty or leave it to the consumer

        val superTypes = irClass.superTypes.map {
            (it.classifierOrNull?.descriptor?.fqNameSafe.toString()) ?: ""
        }


        /*
        val typeParameters = irClass.typeParameters
                .mapNotNull {
                    it.fqNameWhenAvailable?.toString()
                }
         */

        return ClassInfo(irClass.name.toString(), irClass.isCompanion, irClass.isData, irClass.isInline, irClass.isExpect, irClass.kind, superTypes, packageName)
    }
}
/*
private class OnClass(
        val context: IrPluginContext
) : IrElementTransformerVoid(), ClassLoweringPass {

    override fun lower(irClass: IrClass) {

        if (irClass.isExpect) {
            // ignore expects because we will get the actually?
            return
        }

        // irClass.isCompanion
        // irClass.isData
        // irClass.isInline
        // if (irClass.kind == ClassKind.ENUM_CLASS)
        // irClass.typeParameters

        // irClass.parent

        // irClass.name

        irClass.superTypes.map {
            it.classifierOrNull?.isPublicApi
        }
    }
}

 */