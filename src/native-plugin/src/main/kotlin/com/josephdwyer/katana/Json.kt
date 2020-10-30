package com.josephdwyer.katana

import org.jetbrains.kotlin.descriptors.ClassKind

data class KatanaJson(
        val classes: Map<String, ClassJson>,
        val functions: List<FunctionJson>
)

data class FileJson(
        val filePath: String,
        val startLine: Int?,
        val endLine: Int?
)

data class FunctionParameterJson(
        val name: String,
        val type: TypeJson
)

// Type arguments for specific references to classes, e.g. Int for List<Int> or * for List<*>
data class TypeArgumentJson(
        val isStar: Boolean,
        val type: TypeJson?,
        val variance: String?
)

// Concrete types, e.g. List<Int>
data class TypeJson(
        val name: String,
        val typeArguments: List<TypeArgumentJson>,
        val nullable: Boolean
)

data class ClassJson(
        val name: String,
        val isCompanion: Boolean,
        val isData: Boolean,
        val isInline: Boolean,
        val kind: ClassKind,
        val superClasses: List<TypeJson>,
        val packageName: String
)

data class FunctionJson(
        val file: FileJson,
        val packageName: String,
        val name: String,
        val visibility: String,
        val parameters: List<FunctionParameterJson>?,
        val returnType: TypeJson,
        val parentClass: String?,
        val isConstructor: Boolean
)
