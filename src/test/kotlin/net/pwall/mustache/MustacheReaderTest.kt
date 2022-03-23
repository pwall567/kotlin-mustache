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

package net.pwall.mustache

import net.pwall.mustache.parser.MustacheParserException
import net.pwall.mustache.parser.MustacheReader
import java.io.StringReader
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.expect

class MustacheReaderTest {

    private val buffer = CharArray(1024)

    @Test
    fun `reader on empty data is eof`() {
        val reader = MustacheReader(StringReader(""))
        expect(-1) { reader.read(buffer, 0, 1) }
    }

    @Test
    fun `reader for text is transparent and keeps LF`() {
        val text = "xxx\n"
        val reader = MustacheReader(StringReader(text))
        expect(4) { reader.read(buffer, 0, 4) }
        expect(-1) { reader.read(buffer, 0, 1) }
        expect(text) { String(buffer, 0, 4) }
    }

    @Test
    fun `reader for text is transparent and keeps CRLF`() {
        val text = "xxx\r\n"
        val reader = MustacheReader(StringReader(text))
        expect(1) { reader.read(buffer, 0, 1) }
        expect(1) { reader.read(buffer, 1, 1) }
        expect(1) { reader.read(buffer, 2, 1) }
        expect(1) { reader.read(buffer, 3, 1) }
        expect(1) { reader.read(buffer, 4, 1) }
        expect(-1) { reader.read(buffer, 0, 1) }
        expect(text) { String(buffer, 0, 5) }
    }

    @Test
    fun `reader reads less than requested on EOF`() {
        val text = "12345678"
        val reader = MustacheReader(StringReader(text))
        expect(8) { reader.read(buffer, 0, 10) }
        expect(-1) { reader.read(buffer, 0, 1) }
        expect(text) { String(buffer, 0, 8) }
    }

    @Test
    fun `section tag after data keeps whitespaces`() {
        val text = "#  {{#xxx}}  \n"
        val reader = MustacheReader(StringReader(text))
        val len = reader.read(buffer, 0, 1024)
        expect(text) { String(buffer, 0, len) }
    }

    @Test
    fun `section tag before data keeps whitespaces`() {
        val text = "  {{#xxx}}  #\n"
        val reader = MustacheReader(StringReader(text))
        val len = reader.read(buffer, 0, 1024)
        expect(text) { String(buffer, 0, len) }
    }

    @Test
    fun `standalone section tag in regular line is trimmed`() {
        val tag = "{{#xxx}}"
        val text = "  $tag  \n"
        val reader = MustacheReader(StringReader(text))
        val len = reader.read(buffer, 0, 1024)
        expect(tag) { String(buffer, 0, len) }
    }

    @Test
    fun `standalone section tag on eof is trimmed`() {
        val tag = "{{/xxx}}"
        val text = "  $tag  "
        val reader = MustacheReader(StringReader(text))
        val len = reader.read(buffer, 0, 1024)
        expect(tag) { String(buffer, 0, len) }
    }

    @Test
    fun `reader accepts single closing curly brackets in comments`() {
        val text = "{{! xxx}yyy}zzz }}"
        val reader = MustacheReader(StringReader(text))
        val len = reader.read(buffer, 0, 1024)
        expect(text) { String(buffer, 0, len) }
    }

    @Test
    fun `line buffer overflow triggers exception`() {
        // second line causes overflow
        val text = "12345\n123456\n123"
        val reader = MustacheReader(StringReader(text), 6)
        assertFailsWith<MustacheParserException> {
            reader.read(buffer, 0, 50)
        }
    }

    @Test
    fun `standalone data item keeps whitespace`() {
        val text = "  {{{xxx}}}  "
        val reader = MustacheReader(StringReader(text))
        val len = reader.read(buffer, 0, 1024)
        expect(text) { String(buffer, 0, len) }
    }

    @Test
    fun `two tags on same line are not collapsed`() {
        val text = "{{#xxx}} {{/xxx}}"
        val reader = MustacheReader(StringReader(text))
        val len = reader.read(buffer, 0, 1024)
        expect(text) { String(buffer, 0, len) }
    }
}
