/*
 * @(#) DelimiterTest.kt
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
import java.io.StringReader
import kotlin.test.Test
import kotlin.test.expect

class DelimiterTest {

    @Test
    fun `The equals sign (used on both sides) should permit delimiter changes`() {
        val ts = "{{=<% %>=}}(<%text%>)"
        val testMap = mapOf("text" to "Hey!")
        val out = "(Hey!)"
        expect(out) { Template.parse(StringReader(ts)).processToString(testMap) }
    }

    @Test
    fun `Characters with special meaning regexen should be valid delimiters`() {
        val ts = "({{=[ ]=}}[text])"
        val testMap = mapOf("text" to "It worked!")
        val out = "(It worked!)"
        expect(out) { Template.parse(StringReader(ts)).processToString(testMap) }
    }

    @Test
    fun `Delimiters set outside sections should persist`() {
        val ts = "|\n[\n" +
                "{{#section}}\n  {{data}}\n  |data|\n{{/section}}\n\n" +
                "{{= | | =}}\n" +
                "|#section|\n  {{data}}\n  |data|\n|/section|\n" +
                "]"
        val testMap = mapOf("section" to true, "data" to "I got interpolated.")
        val out = "|\n[\n  I got interpolated.\n  |data|\n\n  {{data}}\n  I got interpolated.\n]"
        expect(out) { Template.parse(StringReader(ts)).processToString(testMap) }
    }

    @Test
    fun `Delimiters set in a parent template should not affect a partial`() {
        val ts = "|\n" +
                "[ {{>include1}} ]\n" +
                "{{= | | =}}\n" +
                "[ |>include1| ]"
        val testMap = mapOf("value" to "yes")
        val out = "|\n[ .yes. ]\n[ .yes. ]"
        Template.parser.directory = File("src/test/resources")
        expect(out) { Template.parse(StringReader(ts)).processToString(testMap) }
    }

    @Test
    fun `Delimiters set in a partial should not affect the parent template`() {
        val ts = "|\n" +
                "[ {{>include2}} ]\n" +
                "[ .{{value}}.  .|value|. ]"
        val testMap = mapOf("value" to "yes")
        val out = "|\n[ .yes.  .yes. ]\n[ .yes.  .|value|. ]"
        Template.parser.directory = File("src/test/resources")
        expect(out) { Template.parse(StringReader(ts)).processToString(testMap) }
    }
}
