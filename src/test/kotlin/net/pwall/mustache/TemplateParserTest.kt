/*
 * @(#) TemplateParserTest.kt
 *
 * kotlin-mustache Minimal Kotlin implementation of Mustache templates
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

import kotlin.test.Test
import kotlin.test.expect

import java.io.File
import java.io.StringReader

class TemplateParserTest {

    @Test fun `should output empty string from empty template`() {
        val template = Template.parse(StringReader(""))
        expect("") { template.processToString(null) }
    }

    @Test fun `should output text-only template`() {
        val template = Template.parse(StringReader("hello"))
        expect("hello") { template.processToString(null) }
    }

    @Test fun `should output template with variable`() {
        val template = Template.parse(StringReader("hello, {{aaa}}"))
        val data = TestClass("world", "")
        expect("hello, world") { template.processToString(data) }
    }

    @Test fun `should output template variables escaped by default`() {
        val template = Template.parse(StringReader("hello, {{aaa}}"))
        val data = TestClass("<\u20ACeuro>", "")
        expect("hello, &lt;&euro;euro&gt;") { template.processToString(data) }
    }

    @Test fun `should output literal variables unescaped`() {
        val template = Template.parse(StringReader("hello, {{&aaa}}"))
        val data = TestClass("<world>", "")
        expect("hello, <world>") { template.processToString(data) }
    }

    @Test fun `should output section for each member of list`() {
        val template = Template.parse(StringReader("data: {{#items}}hello, {{aaa}};{{/items}}"))
        val data = mapOf("items" to listOf(TestClass("world", ""), TestClass("moon", "")))
        expect("data: hello, world;hello, moon;") { template.processToString(data) }
    }

    @Test fun `should output section for each member of list using dot`() {
        val template = Template.parse(StringReader("data: {{#items}}hello, {{.}};{{/items}}"))
        val data = mapOf("items" to listOf("world", "moon"))
        expect("data: hello, world;hello, moon;") { template.processToString(data) }
    }

    @Test fun `should output section with iterable variables`() {
        val template = Template.parse(StringReader(
                "data: {{#items}}{{index1}}.hello, {{.}}{{^last}}; {{/last}}{{/items}}"))
        val data = mapOf("items" to listOf("world", "moon", "mars"))
        expect("data: 1.hello, world; 2.hello, moon; 3.hello, mars") { template.processToString(data) }
    }

    @Test fun `should output list using partial`() {
        val template = Template.parse(File("src/test/resources/list.mustache"))
        val data = mapOf("list" to listOf("abc", "def", "ghi"))
        expect("abc,\ndef,\nghi\n\n") { template.processToString(data) }
    }

    @Test fun `should output section conditionally depending on enum`() {
        val template = Template.parse(StringReader("data: {{#eee}}{{#A}}Q{{/A}}{{#B}}R{{/B}}{{#C}}S{{/C}}{{/eee}}"))
        val data = mapOf("eee" to TestEnum.A)
        expect("data: Q") { template.processToString(data) }
    }

    @Test fun `should output inverted section conditionally depending on enum`() {
        val template = Template.parse(StringReader("data: {{#eee}}{{^A}}Q{{/A}}{{^B}}R{{/B}}{{^C}}S{{/C}}{{/eee}}"))
        val data = mapOf("eee" to TestEnum.A)
        expect("data: RS") { template.processToString(data) }
    }

    enum class TestEnum { A, B, C }

}
