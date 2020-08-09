/*
 * @(#) Context.kt
 *
 * kotlin-mustache  Kotlin implementation of Mustache templates
 * Copyright (c) 2020 Peter Wall
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.pwall.mustache

import kotlin.reflect.full.memberExtensionProperties
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.staticFunctions
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

    @Suppress("UNCHECKED_CAST")
    fun enumChild(contextObject: Enum<*>) = object : Context(contextObject, this) {
        val values = (contextObject::class.staticFunctions.find { it.name == "values" }?.call() as? Array<Enum<*>>?)?.
                map { it.name }
        override fun resolve(name: String): Any? {
            if (contextObject.name == name)
                return true
            if (values != null && name in values)
                return false
            return super.resolve(name)
        }
    }

}
