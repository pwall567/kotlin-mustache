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

}
