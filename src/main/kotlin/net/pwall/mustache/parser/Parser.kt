/*
 * @(#) Parser.kt
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

package net.pwall.mustache.parser

import java.io.File
import java.io.InputStream
import java.io.Reader
import java.io.StringReader

import net.pwall.mustache.Template

class Parser(
        var directory: File = File("."),
        var extension: String = "mustache",
        var resolvePartial: Parser.(String) -> Reader =
                { name -> File(this.directory, "$name.${this.extension}").reader() }
) {

    private var openDelimiter = "{{"
    private var closeDelimiter = "}}"

    private val partialCache = mutableMapOf<String, Template.Partial>()

    fun parse(file: File): Template {
        directory = file.parentFile
        extension = file.extension
        return parse(file.inputStream())
    }

    fun parse(inputStream: InputStream): Template = parse(inputStream.reader())

    fun parse(reader: Reader): Template {
        openDelimiter = defaultOpenDelimiter
        closeDelimiter = defaultCloseDelimiter
        return Template(parseNested(MustacheReader(reader.buffered())))
    }

    fun parse(string: String): Template = parse(StringReader(string))

    private fun parseNested(reader: MustacheReader, stopper: String? = null): List<Template.Element> {
        val saveOpenDelimiter = openDelimiter
        val saveCloseDelimiter = closeDelimiter
        try {
            val elements = mutableListOf<Template.Element>()
            val sb = StringBuilder()
            while (reader.readUntilDelimiter(openDelimiter, sb)) {
                if (sb.isNotEmpty())
                    elements.add(Template.TextElement(sb.toString()))
                sb.setLength(0)
                reader.read().let {
                    when {
                        it < 0 -> throw MustacheParserException("Unclosed tag at end of document")
                        it == '{'.code -> {
                            if (!reader.readUntilDelimiter("}$closeDelimiter", sb))
                                throw MustacheParserException("Unclosed literal tag at end of document")
                            val tag = sb.toString().trim()
                            if (tag.isEmpty())
                                throw MustacheParserException("Illegal empty literal tag")
                            elements.add(Template.LiteralVariable(tag))
                        }
                        else -> {
                            sb.append(it.toChar())
                            if (!reader.readUntilDelimiter(closeDelimiter, sb))
                                throw MustacheParserException("Unclosed tag at end of document")
                            val tag = sb.toString().trim()
                            if (tag.isEmpty())
                                throw MustacheParserException("Illegal empty tag")
                            when (tag[0]) {
                                '&' -> elements.add(Template.LiteralVariable(tag.substring(1).trim()))
                                '#' -> {
                                    val section = tag.substring(1).trim()
                                    elements.add((Template.Section(section, parseNested(reader, section))))
                                }
                                '^' -> {
                                    val section = tag.substring(1).trim()
                                    elements.add((Template.InvertedSection(section, parseNested(reader, section))))
                                }
                                '/' -> {
                                    val section = tag.substring(1).trim()
                                    if (section != stopper)
                                        throw MustacheParserException("Unmatched section close tag - $section")
                                    return elements
                                }
                                '>' -> {
                                    val name = tag.substring(1).trim()
                                    val partial = getPartial(name)
                                    elements.add(partial)
                                }
                                '=' -> {
                                    setDelimiters(tag)
                                    reader.setDelimiter(openDelimiter, closeDelimiter)
                                }
                                else -> elements.add(Template.Variable(tag))
                            }
                        }
                    }
                }
                sb.setLength(0)
            }
            if (stopper != null)
                throw MustacheParserException("Unclosed section at end of document")
            if (sb.isNotEmpty())
                elements.add(Template.TextElement(sb.toString()))
            return elements
        }
        finally {
            openDelimiter = saveOpenDelimiter
            closeDelimiter = saveCloseDelimiter
        }
    }

    private fun setDelimiters(tag: String) {
        if (tag.endsWith('=') && tag.contains(' ')) {
            val strippedTag = tag.substring(1, tag.length - 1).trim()
            openDelimiter = strippedTag.substringBefore(' ')
            closeDelimiter = strippedTag.substringAfter(' ').trim()
            if (openDelimiter.isNotEmpty() && closeDelimiter.isNotEmpty())
                return
        }
        throw MustacheParserException("Incorrect delimiter tag")
    }

    private fun getPartial(name: String): Template.Partial {
        return partialCache[name] ?: Template.Partial().also {
            partialCache[name] = it
            it.template = parse(resolvePartial(name))
        }
    }

    companion object {

        const val defaultOpenDelimiter = "{{"
        const val defaultCloseDelimiter = "}}"

        fun Reader.readUntilDelimiter(delimiter: String, sb: StringBuilder): Boolean {
            val n = delimiter.length - 1
            val stopper = delimiter.last().code
            while (true) {
                val ch = read()
                if (ch < 0)
                    return false
                if (ch == stopper && sb.length >= n && delimiterMatches(delimiter, sb)) {
                    sb.setLength(sb.length - n)
                    return true
                }
                sb.append(ch.toChar())
            }
        }

        private fun delimiterMatches(delimiter: String, sb: StringBuilder): Boolean {
            var i = delimiter.length - 1
            var j = sb.length
            while (i > 0) {
                if (delimiter[--i] != sb[--j])
                    return false
            }
            return true
        }

    }

}
