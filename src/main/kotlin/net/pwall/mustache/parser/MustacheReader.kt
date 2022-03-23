/*
 * @(#) TestClass.kt
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

import java.io.Reader

/**
 * A Reader that ignores whitespaces surrounding standalone tags.
 */
class MustacheReader(
    val input: Reader,
    val maxLineSize: Int = DEFAULT_MAX_LINE_SIZE
) : Reader() {

    private val lineBuffer = CharArray(maxLineSize)
    private var pos = 0
    private var end = 0
    private var tagRe = DEFAULT_TAG_RE

    override fun read(cbuf: CharArray, off: Int, len: Int): Int {
        var cnt = 0
        do {
            if (pos == end) {
                loadLineBuffer()
            }
            while (cnt < len && pos < end) {
                cbuf[off + cnt++] = lineBuffer[pos++]
            }
        } while (end > 0 && cnt < len)
        return if (cnt == 0) -1 else cnt
    }

    override fun close() {
        input.close()
    }

    fun setDelimiter(sd: String, ed: String) {
        tagRe = Regex("""\s*(${escape(sd)}$tagChar$tagContent${escape(ed)})\s*""")
    }

    private fun escape(str: String) =
        str.fold(StringBuilder()) { sb, c ->
            sb.append(if (c in reSpecial) "\\$c" else c)
        }.toString()

    private fun loadLineBuffer() {
        pos = 0
        end = 0
        do {
            val st = input.read(lineBuffer, end, 1)
            if (st == 1) end++
        } while (st != -1 && lineBuffer[end - 1] != '\n' && end < maxLineSize)

        if (end == maxLineSize && lineBuffer[maxLineSize - 1] != '\n')
            throw MustacheParserException("Line buffer overflow")

        tagRe.matchEntire(String(lineBuffer, 0, end))?.let { match ->
            match.groupValues[1].run {
                forEachIndexed { idx, c -> lineBuffer[idx] = c }
                end = length
            }
        }
    }

    companion object {
        const val DEFAULT_MAX_LINE_SIZE = 1024

        private const val reSpecial = "/.*+?|()[]{}\\"
        private const val tagChar = "[!#^/>=]"
        private const val tagContent = "[^}]+(}[^}]+)*"

        private val DEFAULT_TAG_RE = Regex("""\s*(\{{2,3}$tagChar$tagContent}{2,3})\s*$""")
    }
}
