/*
 * @(#) TemplateTest.kt
 *
 * kotlin-mustache  Kotlin implementation of Mustache templates
 * Copyright (c) 2020, 2024 Peter Wall
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

class TemplateTest {

    @Test fun `should output empty string from empty template`() {
        val template = Template(listOf())
        expect("") { template.processToString(null) }
    }

    @Test fun `should output text-only template`() {
        val template = Template(listOf(Template.TextElement("hello")))
        expect("hello") { template.processToString(null) }
    }

    @Test fun `should output template with variable`() {
        val template = Template(listOf(Template.TextElement("hello, "), Template.Variable("aaa")))
        val data = TestClass("world", "")
        expect("hello, world") { template.processToString(data) }
    }

    @Test fun `should output template variables escaped by default`() {
        val template = Template(listOf(Template.TextElement("hello, "), Template.Variable("aaa")))
        val data = TestClass("<world>", "")
        expect("hello, &lt;world&gt;") { template.processToString(data) }
    }

    @Test fun `should output literal variables unescaped`() {
        val template = Template(listOf(Template.TextElement("hello, "), Template.LiteralVariable("aaa")))
        val data = TestClass("<world>", "")
        expect("hello, <world>") { template.processToString(data) }
    }

    @Test fun `should output section for each member of list`() {
        val section = Template.Section("items", listOf(Template.TextElement("hello, "), Template.Variable("aaa"),
                Template.TextElement(";")))
        val template = Template(listOf(Template.TextElement("data: "), section))
        val data = mapOf("items" to listOf(TestClass("world", ""), TestClass("moon", "")))
        expect("data: hello, world;hello, moon;") { template.processToString(data) }
    }

    @Test fun `should output list size`() {
        val template = Template(listOf(Template.Variable("list.size")))
        val data = mapOf("list" to listOf(1, 2, 3))
        expect("3") { template.processToString(data) }
    }

    @Test fun `should output list entries`() {
        val template = Template(listOf(Template.Variable("list.2")))
        val data = mapOf("list" to listOf(111, 222, 333))
        expect("333") { template.processToString(data) }
    }

    @Test fun `should output string length`() {
        val template = Template(listOf(Template.Variable("string.length")))
        val data = mapOf("string" to "Hello!")
        expect("6") { template.processToString(data) }
    }

    @Test fun `should output to Appendable`() {
        val template = Template(listOf(Template.TextElement("hello")))
        val stringBuilder = StringBuilder()
        template.processTo(stringBuilder, null)
        expect("hello") { stringBuilder.toString() }
    }

}
