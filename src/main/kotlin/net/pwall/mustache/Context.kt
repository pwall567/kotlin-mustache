package net.pwall.mustache

import kotlin.reflect.full.memberExtensionProperties
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.staticProperties

open class Context private constructor(private val contextObject: Any?, private val parent: Context?) {

    constructor(contextObject: Any?) : this(contextObject, null)

    open fun resolve(name: String): Any? {
        if (name == ".")
            return contextObject
        when (contextObject) {
            null -> return null
            is Map<*, *> -> {
                if (contextObject.containsKey(name))
                    return contextObject[name]
            }
            else -> {
                val kClass = contextObject::class
                kClass.memberProperties.find { it.name == name }?.let { return it.call(contextObject) }
                kClass.memberExtensionProperties.find { it.name == name }?.let { return it.call(contextObject) }
                kClass.staticProperties.find { it.name == name }?.let { return it.call() }
            }
        }
        return parent?.resolve(name)
    }

    fun child(contextObject: Any?) = Context(contextObject, this)

    fun iteratorChild(contextObject: Any?, first: Boolean, last: Boolean, index: Int, index1: Int) =
            object : Context(contextObject, this) {
        override fun resolve(name: String): Any? {
            return when (name) {
                "first" -> first
                "last" -> last
                "index" -> index
                "index1" -> index1
                else -> super.resolve(name)
            }
        }
    }

}
