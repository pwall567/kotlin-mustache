/*
 * @(#) Template.kt
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

import java.io.File
import java.io.InputStream
import java.io.Reader
import java.math.BigDecimal
import java.math.BigInteger

import net.pwall.html.HTML
import net.pwall.mustache.parser.Parser

class Template(private val elements: List<Element>) {

    fun processToString(contextObject: Any?): String = StringBuilder().apply {
        processTo(this, contextObject)
    }.toString()

    fun processTo(appendable: Appendable, contextObject: Any?) {
        appendTo(appendable, Context(contextObject))
    }

    fun appendTo(appendable: Appendable, context: Context) {
        elements.forEach { it.appendTo(appendable, context) }
    }

    interface Element {
        fun appendTo(appendable: Appendable, context: Context)
    }

    class TextElement(private val text: String) : Element {

        override fun appendTo(appendable: Appendable, context: Context) {
            appendable.append(text)
        }

    }

    class Variable(private val name: String) : Element {

        override fun appendTo(appendable: Appendable, context: Context) {
            context.resolve(name)?.let { appendable.append(HTML.escape(it.toString())) }
        }

    }

    class LiteralVariable(private val name: String) : Element {

        override fun appendTo(appendable: Appendable, context: Context) {
            context.resolve(name)?.let { appendable.append(it.toString()) }
        }

    }

    abstract class ElementWithChildren(private val children: List<Element>) : Element {

        fun appendChildren(appendable: Appendable, context: Context) {
            children.forEach { child -> child.appendTo(appendable, context) }
        }

    }

    class Section(private val name: String, children: List<Element>) : ElementWithChildren(children) {

        override fun appendTo(appendable: Appendable, context: Context) {
            useValue(context.resolve(name), appendable, context)
        }

        private fun useValue(value: Any?, appendable: Appendable, context: Context) {
            when (value) {
                null -> {}
                is Iterable<*> -> iterate(appendable, context, value.iterator())
                is Array<*> -> iterate(appendable, context, value.iterator())
                is Map<*, *> -> iterate(appendable, context, value.entries.iterator())
                is Enum<*> -> appendChildren(appendable, context.enumChild(value))
                is CharSequence -> {
                    if (value.isNotEmpty())
                        appendChildren(appendable, context.child(value))
                }
                is Boolean -> {
                    if (value)
                        appendChildren(appendable, context.child(value))
                }
                is Int -> {
                    if (value != 0)
                        appendChildren(appendable, context.child(value))
                }
                is Long -> {
                    if (value != 0L)
                        appendChildren(appendable, context.child(value))
                }
                is Short -> {
                    if (value != 0)
                        appendChildren(appendable, context.child(value))
                }
                is Byte -> {
                    if (value != 0)
                        appendChildren(appendable, context.child(value))
                }
                is Double -> {
                    if (value != 0.0)
                        appendChildren(appendable, context.child(value))
                }
                is Float -> {
                    if (value != 0.0F)
                        appendChildren(appendable, context.child(value))
                }
                is BigInteger -> {
                    if (value != BigInteger.ZERO)
                        appendChildren(appendable, context.child(value))
                }
                is BigDecimal -> {
                    if (value != BigDecimal.ZERO)
                        appendChildren(appendable, context.child(value))
                }
                else -> { // any other types? callable?
                    appendChildren(appendable, context.child(value))
                }
            }
        }

        private fun iterate(appendable: Appendable, context: Context, iterator: Iterator<*>) {
            var index = 0
            while (iterator.hasNext()) {
                val item = iterator.next()
                val iteratorContext = context.iteratorChild(item, index == 0, !iterator.hasNext(), index, index + 1)
                appendChildren(appendable, iteratorContext)
                index++
            }
        }

    }

    class InvertedSection(private val name: String, children: List<Element>) : ElementWithChildren(children) {

        override fun appendTo(appendable: Appendable, context: Context) {
            context.resolve(name).let {
                when (it) {
                    null -> {
                        appendChildren(appendable, context)
                    }
                    is List<*> -> {
                        if (it.isEmpty())
                            appendChildren(appendable, context)
                    }
                    is Iterable<*> -> {
                        if (!it.iterator().hasNext())
                            appendChildren(appendable, context)
                    }
                    is Array<*> -> {
                        if (it.isEmpty())
                            appendChildren(appendable, context)
                    }
                    is Map<*, *> -> {
                        if (it.isEmpty())
                            appendChildren(appendable, context)
                    }
                    is CharSequence -> {
                        if (it.isEmpty())
                            appendChildren(appendable, context)
                    }
                    is Boolean -> {
                        if (!it)
                            appendChildren(appendable, context)
                    }
                    is Int -> {
                        if (it == 0)
                            appendChildren(appendable, context)
                    }
                    is Long -> {
                        if (it == 0)
                            appendChildren(appendable, context)
                    }
                    is Short -> {
                        if (it == 0)
                            appendChildren(appendable, context)
                    }
                    is Byte -> {
                        if (it == 0)
                            appendChildren(appendable, context)
                    }
                }
            }
        }

    }

    class Partial : Element {

        internal lateinit var template: Template

        override fun appendTo(appendable: Appendable, context: Context) {
            template.appendTo(appendable, context)
        }

    }

    companion object {

        val parser by lazy {
            Parser()
        }

        fun parse(file: File) = parser.parse(file)

        fun parse(inputStream: InputStream) = Parser().parse(inputStream)

        fun parse(reader: Reader) = Parser().parse(reader)

    }

}
