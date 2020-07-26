package net.pwall.mustache

import kotlin.test.Test
import kotlin.test.expect

class ContextTest {

    @Test fun `should retrieve property from map`() {
        val testMap = mapOf("aaa" to "Hello", "bbb" to "World")
        val context = Context(testMap)
        expect("Hello") { context.resolve("aaa") }
        expect("World") { context.resolve("bbb") }
    }

    @Test fun `should retrieve property from data class`() {
        val testClass = TestClass("Hello", "World")
        val context = Context(testClass)
        expect("Hello") { context.resolve("aaa") }
        expect("World") { context.resolve("bbb") }
    }

}
