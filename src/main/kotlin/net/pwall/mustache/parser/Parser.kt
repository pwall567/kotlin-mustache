package net.pwall.mustache.parser

import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.Reader
import java.lang.Appendable

import net.pwall.mustache.Template

class Parser {

    private val openDelimiter = "{{"
    private val closeDelimiter = "}}"

    var directory = File(".")
    var extension = "mustache"

    var resolvePartial: (String) -> Reader = { name -> File(directory, "$name.$extension").reader() }

    private val partialCache = mutableMapOf<String, Template>()

    fun parse(file: File): Template {
        directory = file.parentFile
        return parse(file.inputStream())
    }

    fun parse(inputStream: InputStream): Template = parse(inputStream.reader())

    fun parse(reader: Reader) = Template(parseNested(if (reader is BufferedReader) reader else reader.buffered()))

    private fun parseNested(reader: Reader, stopper: String? = null): List<Template.Element> {
        val elements = mutableListOf<Template.Element>()
        val sb = StringBuilder()
        while (reader.readUntilDelimiter(openDelimiter, sb)) {
            if (sb.isNotEmpty())
                elements.add(Template.TextElement(sb.toString()))
            sb.setLength(0)
            if (!reader.readUntilDelimiter(closeDelimiter, sb))
                throw MustacheParserException("Unclosed tag at end of document")
            val tag = sb.toString().trim()
            when {
                tag.startsWith('{') && tag.endsWith('}') ->
                    elements.add(Template.LiteralVariable(tag.substring(1, tag.length - 1).trim()))
                tag.startsWith('&') -> elements.add(Template.LiteralVariable(tag.substring(1).trim()))
                tag.startsWith('#') -> {
                    val section = tag.substring(1).trim()
                    elements.add((Template.Section(section, parseNested(reader, section))))
                }
                tag.startsWith('^') -> {
                    val section = tag.substring(1).trim()
                    elements.add((Template.InvertedSection(section, parseNested(reader, section))))
                }
                tag.startsWith('/') -> {
                    val section = tag.substring(1).trim()
                    if (section != stopper)
                        throw MustacheParserException("Unmatched section close tag - $section")
                    return elements
                }
                tag.startsWith('>') -> {
                    val name = tag.substring(1).trim()
                    val partial = getPartial(name)
                    elements.add(Template.Partial(partial))
                }
                else -> elements.add(Template.Variable(tag))
            }
            sb.setLength(0)
        }
        if (stopper != null)
            throw MustacheParserException("Unclosed section at end of document")
        if (sb.isNotEmpty())
            elements.add(Template.TextElement(sb.toString()))
        return elements
    }

    private fun getPartial(name: String): Template {
        partialCache[name]?.let { return it }
        val template = parse(resolvePartial(name))
        partialCache[name] = template
        return template
    }

    companion object {

        fun Reader.readUntilDelimiter(delimiter: String, appendable: Appendable): Boolean {
            val stopper = delimiter[0]
            outerLoop@ while (true) {
                var ch = read()
                if (ch < 0)
                    return false
                if (ch.toChar() == stopper) {
                    for (i in 1 until delimiter.length) {
                        ch = read()
                        if (ch < 0) {
                            appendable.append(delimiter.subSequence(0, i))
                            return false
                        }
                        if (ch.toChar() != delimiter[i]) {
                            appendable.append(delimiter.subSequence(0, i))
                            appendable.append(ch.toChar())
                            continue@outerLoop
                        }
                    }
                    return true
                }
                appendable.append(ch.toChar())
            }
        }

    }

}
