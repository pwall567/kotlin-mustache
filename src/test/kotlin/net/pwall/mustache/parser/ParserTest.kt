/*
 * @(#) ParserTest.kt
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

import kotlin.test.Test
import kotlin.test.expect

import java.io.StringReader

import net.pwall.mustache.parser.Parser.Companion.readUntilDelimiter

class ParserTest {

    @Test fun `should read entire string when delimiter not present`() {
        val reader = StringReader("ABCD")
        val stringBuilder = StringBuilder()
        expect(false) { reader.readUntilDelimiter("//", stringBuilder) }
        expect("ABCD") { stringBuilder.toString() }
    }

    @Test fun `should read until delimiter`() {
        val reader = StringReader("ABCD//EFG")
        val stringBuilder = StringBuilder()
        expect(true) { reader.readUntilDelimiter("//", stringBuilder) }
        expect("ABCD") { stringBuilder.toString() }
    }

    @Test fun `should read until complex delimiter`() {
        val reader = StringReader("ABCDqrstuvEFG")
        val stringBuilder = StringBuilder()
        expect(true) { reader.readUntilDelimiter("qrstuv", stringBuilder) }
        expect("ABCD") { stringBuilder.toString() }
    }

    @Test fun `should read until delimiter when partial delimiter present`() {
        val reader = StringReader("AB/CD//EFG")
        val stringBuilder = StringBuilder()
        expect(true) { reader.readUntilDelimiter("//", stringBuilder) }
        expect("AB/CD") { stringBuilder.toString() }
    }

}
