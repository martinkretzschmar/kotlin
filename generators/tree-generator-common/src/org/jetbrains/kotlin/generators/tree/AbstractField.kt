/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.generators.tree

abstract class AbstractField<Field : AbstractField<Field>> {

    abstract val name: String

    abstract val typeRef: TypeRefWithNullability

    val nullable: Boolean
        get() = typeRef.nullable

    var kDoc: String? = null

    abstract val isVolatile: Boolean

    abstract val isFinal: Boolean

    abstract val isLateinit: Boolean

    abstract val isParameter: Boolean

    open val arbitraryImportables: MutableList<Importable> = mutableListOf()

    open var optInAnnotation: ClassRef<*>? = null

    abstract var isMutable: Boolean
    open val withGetter: Boolean get() = false
    open val customSetter: String? get() = null

    var deprecation: Deprecated? = null

    var visibility: Visibility = Visibility.PUBLIC

    var fromParent: Boolean = false

    /**
     * Whether this field can contain an element either directly or e.g. in a list.
     */
    open val containsElement: Boolean
        get() = typeRef is ElementOrRef<*, *> || this is ListField && baseType is ElementOrRef<*, *>

    open val defaultValueInImplementation: String? get() = null

    /**
     * @see org.jetbrains.kotlin.generators.tree.detectBaseTransformerTypes
     */
    var useInBaseTransformerDetection = true

    /**
     * Whether a visitor should be run on this field in the generated `acceptChildren` in `transformChildren` method.
     *
     * Only has effect if [containsElement] is `true`.
     */
    var needAcceptAndTransform: Boolean = true

    open val overriddenTypes: MutableSet<TypeRefWithNullability> = mutableSetOf()

    open fun updatePropertiesFromOverriddenField(parentField: Field, haveSameClass: Boolean) {}

    override fun toString(): String {
        return name
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        if (javaClass != other.javaClass) return false
        other as AbstractField<*>
        return name == other.name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    /**
     * Returns a copy of this field with its [typeRef] set to [newType] (if it's possible).
     */
    abstract fun replaceType(newType: TypeRefWithNullability): Field

    /**
     * Returns a copy of this field.
     */
    abstract fun copy(): Field

    protected open fun updateFieldsInCopy(copy: Field) {
        copy.kDoc = kDoc
        copy.arbitraryImportables += arbitraryImportables
        copy.optInAnnotation = optInAnnotation
        copy.isMutable = isMutable
        copy.deprecation = deprecation
        copy.visibility = visibility
        copy.fromParent = fromParent
        copy.useInBaseTransformerDetection = useInBaseTransformerDetection
        copy.needAcceptAndTransform = needAcceptAndTransform
        copy.overriddenTypes += overriddenTypes
    }
}
