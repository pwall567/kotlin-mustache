package net.pwall.mustache

import java.io.File
import java.io.InputStream
import java.io.Reader

import net.pwall.html.HTML
import net.pwall.mustache.parser.Parser

class Template(private val elements: List<Element>) {

    fun processToString(contextObject: Any?): String = StringBuilder().apply {
        processTo(this, contextObject)
    }.toString()

    fun processTo(appendable: Appendable, contextObject: Any?) {
        val context = Context(contextObject)
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

    class Section(private val name: String, private val children: List<Element>) : Element {

        private fun iterate(appendable: Appendable, context: Context, iterator: Iterator<*>) {
            var index = 0
            while (iterator.hasNext()) {
                val item = iterator.next()
                val iteratorContext = context.iteratorChild(item, index == 0, !iterator.hasNext(), index, index + 1)
                children.forEach { child -> child.appendTo(appendable, iteratorContext) }
                index++
            }
        }

        override fun appendTo(appendable: Appendable, context: Context) {
            context.resolve(name)?.let {
                when (it) {
                    is Iterable<*> -> {
                        iterate(appendable, context, it.iterator())
//                        it.forEach { entry ->
//                            val childContext = context.child(entry)
//                            children.forEach { child -> child.appendTo(appendable, childContext) }
//                        }
                    }
                    is Array<*> -> {
                        it.forEach { entry ->
                            val childContext = context.child(entry)
                            children.forEach { child -> child.appendTo(appendable, childContext) }
                        }
                    }
                    is Map<*, *> -> {
                        it.entries.forEach { entry ->
                            val childContext = context.child(entry)
                            children.forEach { child -> child.appendTo(appendable, childContext) }
                        }
                    }
                    is CharSequence -> {
                        if (it.isNotEmpty())
                            children.forEach { child -> child.appendTo(appendable, context) }
                    }
                    is Boolean -> {
                        if (it)
                            children.forEach { child -> child.appendTo(appendable, context) }
                    }
                    is Int -> {
                        if (it != 0)
                            children.forEach { child -> child.appendTo(appendable, context) }
                    }
                    is Long -> {
                        if (it != 0)
                            children.forEach { child -> child.appendTo(appendable, context) }
                    }
                    is Short -> {
                        if (it != 0)
                            children.forEach { child -> child.appendTo(appendable, context) }
                    }
                    is Byte -> {
                        if (it != 0)
                            children.forEach { child -> child.appendTo(appendable, context) }
                    }
                    else -> { // any other types? callable?
                        val childContext = context.child(it)
                        children.forEach { child -> child.appendTo(appendable, childContext) }
                    }
                }
            }
        }

    }

    class InvertedSection(private val name: String, private val children: List<Element>) : Element {

        override fun appendTo(appendable: Appendable, context: Context) {
            context.resolve(name).let {
                when (it) {
                    null -> {
                        children.forEach { child -> child.appendTo(appendable, context) }
                    }
                    is List<*> -> {
                        if (it.isEmpty())
                            children.forEach { child -> child.appendTo(appendable, context) }
                    }
                    is Iterable<*> -> {
                        if (!it.iterator().hasNext())
                            children.forEach { child -> child.appendTo(appendable, context) }
                    }
                    is Array<*> -> {
                        if (it.isEmpty())
                            children.forEach { child -> child.appendTo(appendable, context) }
                    }
                    is Map<*, *> -> {
                        if (it.isEmpty())
                            children.forEach { child -> child.appendTo(appendable, context) }
                    }
                    is CharSequence -> {
                        if (it.isEmpty())
                            children.forEach { child -> child.appendTo(appendable, context) }
                    }
                    is Boolean -> {
                        if (!it)
                            children.forEach { child -> child.appendTo(appendable, context) }
                    }
                    is Int -> {
                        if (it == 0)
                            children.forEach { child -> child.appendTo(appendable, context) }
                    }
                    is Long -> {
                        if (it == 0)
                            children.forEach { child -> child.appendTo(appendable, context) }
                    }
                    is Short -> {
                        if (it == 0)
                            children.forEach { child -> child.appendTo(appendable, context) }
                    }
                    is Byte -> {
                        if (it == 0)
                            children.forEach { child -> child.appendTo(appendable, context) }
                    }
                }
            }
        }

    }

    class Partial(private val template: Template) : Element {

        override fun appendTo(appendable: Appendable, context: Context) {
            template.elements.forEach { it.appendTo(appendable, context) }
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
