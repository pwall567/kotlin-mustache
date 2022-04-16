/*
 * @(#) MustacheReader.kt
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
 * A Reader that ignores whitespace surrounding standalone tags.
 *
 * Comments are also entirely dropped.
 */
class MustacheReader(
        val input: Reader,
        val maxLineSize: Int = DEFAULT_MAX_LINE_SIZE
) : Reader() {

    private val lineBuffer = CharArray(maxLineSize)
    private var pos = 0
    private var end = 0
    private var openDelimiter = defaultOpenDelimiter
    private var closeDelimiter = defaultCloseDelimiter

    var indent: String = ""

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

    fun setDelimiter(openDelimiter: String, closeDelimiter: String) {
        this.openDelimiter = openDelimiter
        this.closeDelimiter = closeDelimiter
    }

    private fun loadLineBuffer() {
        pos = 0
        end = 0
        var inComment = false
        val commentStart = "$openDelimiter!"
        do {
            val st = input.read(lineBuffer, end, 1)
            if (st == 1) {
                if (!inComment) {
                    end++
                    if (end >= commentStart.length &&
                            String(lineBuffer, end - commentStart.length, commentStart.length) == commentStart) {
                        inComment = true
                    }
                } else if (lineBuffer[end] == closeDelimiter[0]) {
                    input.read(lineBuffer, end + 1, closeDelimiter.length - 1)
                    if (String(lineBuffer, end, closeDelimiter.length) == closeDelimiter) {
                        inComment = false
                        end += closeDelimiter.length
                    }
                }
            }
        } while (st != -1 && (end == 0 || lineBuffer[end - 1] != '\n') && end < maxLineSize)

        if (end == maxLineSize && lineBuffer[maxLineSize - 1] != '\n')
            throw MustacheParserException("Line buffer overflow")

        val line = String(lineBuffer, 0, end)
        val trimmed = line.trim()
        val unwrapped = trimmed.removeSurrounding(openDelimiter, closeDelimiter)
        val isWrapped = unwrapped.length != trimmed.length
        val isSingleTag = isWrapped && unwrapped[0] in tagType && closeDelimiter !in unwrapped
        if (isSingleTag) {
            trimmed.forEachIndexed { idx, c -> lineBuffer[idx] = c }
            end = trimmed.length
            indent = line.split(openDelimiter, limit = 2).first()
        }
    }

    companion object {
        const val DEFAULT_MAX_LINE_SIZE = 1024

        private const val tagType = "#^/>=!"
        private const val defaultOpenDelimiter = "{{"
        private const val defaultCloseDelimiter = "}}"
    }
}
