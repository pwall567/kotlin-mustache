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

}
