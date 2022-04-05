/*
 * @(#) SpecsTest.kt
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

package net.pwall.mustache.specs

import net.pwall.mustache.Template
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.snakeyaml.engine.v2.api.Load
import org.snakeyaml.engine.v2.api.LoadSettings
import java.io.Closeable
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.expect


class SpecsTest {

    @TestFactory
    fun `comments tests`() =
        makeTests("comments.yml")

    @TestFactory
    fun `delimiters tests`() =
        makeTests("delimiters.yml")

    @TestFactory
    fun `interpolation tests`() =
        makeTests("interpolation.yml")

    @TestFactory
    fun `inverted tests`() =
        makeTests("inverted.yml")

    @TestFactory
    fun `partials tests`() =
        makeTests("partials.yml")

    @TestFactory
    fun `sections tests`() =
        makeTests("sections.yml")

    @TestFactory
    fun `additional tests`() =
        makeTests("~additional.yml")
}


@Suppress("UNCHECKED_CAST")
private fun makeTests(fileName: String) =
    File("src/test/resources/specs/$fileName").inputStream().use {
        yamlLoader.loadFromInputStream(it)
    }.let { top ->
        val spec = top as Map<*, *>
        val tests = spec["tests"] as List<*>
        tests.map { inner ->
            val test = inner as Map<*, *>
            makeSingleTest(
                name = test["name"] as String,
                desc = test["desc"] as String,
                template = test["template"] as String,
                partials = test["partials"]?.let { it as Map<String, String> } ?: emptyMap(),
                data = test["data"] as Any,
                expected = test["expected"] as String
            )
        }
    }

private val yamlLoader = Load(LoadSettings.builder().build())

private fun makeSingleTest(
    name: String,
    desc: String,
    template: String,
    partials: Map<String, String>,
    data: Any,
    expected: String
) = DynamicTest.dynamicTest("$name: $desc") {
    PartialEnv(partials).use { partialEnv ->
        Template.clearPartials()
        Template.parser.directory = partialEnv.directory
        expect(expected) {
            Template.parse(template).processToString(data)
        }
    }
}


private class PartialEnv(partials: Map<String, String>) : Closeable {
    val directory: File = createTempDirectory("partials").toFile().also {
        partials.forEach { (name, content) ->
            File("$it/$name.mustache").writeText(content)
        }
    }

    override fun close() {
        directory.deleteRecursively()
    }
}
