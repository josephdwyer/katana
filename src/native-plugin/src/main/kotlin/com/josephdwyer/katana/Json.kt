package com.josephdwyer.katana

import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.types.typeUtil.TypeNullability


data class FileJson(
        val filePath: String,
        val startLine: Int?,
        val endLine: Int?
)

data class FunctionParameterJson(
        val name: String,
        val type: TypeJson
)

data class TypeParameterJson(
        val type: String,
        val nullability: TypeNullability
)

data class TypeJson(
        val name: String,
        val typeParameters: List<TypeParameterJson>,
        val nullable: Boolean,
        val classInfo: ClassJson?
)

data class ClassJson(
        val name: String,
        val isCompanion: Boolean,
        val isData: Boolean,
        val isInline: Boolean,
        val isExpect: Boolean,
        val kind: ClassKind,
        val superClasses: List<String>,
        val packageName: String
)

data class FunctionJson(
        val file: FileJson,
        val packageName: String,
        val name: String,
        val visibility: String,
        val parameters: List<FunctionParameterJson>?,
        val returnType: TypeJson,
        val parent: String,
        val classInfo: ClassJson?,
        val isExpect: Boolean
)
