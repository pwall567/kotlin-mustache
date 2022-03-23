/*
 * @(#) WhitespaceTest.kt
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

import java.io.StringReader
import kotlin.test.Test
import kotlin.test.expect


class WhitespaceTest {

    private fun wsTest(ts: String, r: String) {
        val template = Template.parse(StringReader(ts))
        val data = mapOf("boolean" to true)
        expect(r) { template.processToString(data) }
    }


    @Test
    fun `Sections should not alter surrounding whitespace`() {
        wsTest(
                " | {{#boolean}}\t|\t{{/boolean}} | \n",
                " | \t|\t | \n"
        )
    }

    @Test
    fun `Sections should not alter internal whitespace`() =
            wsTest(
                    " | {{#boolean}} {{! Important Whitespace }}\n {{/boolean}} | \n",
                    " |  \n  | \n"
            )

    @Test
    fun `Single-line sections should not alter surrounding whitespace`() =
            wsTest(
                    " {{#boolean}}YES{{/boolean}}\n {{#boolean}}GOOD{{/boolean}}\n",
                    " YES\n GOOD\n"
            )

    @Test
    fun `Standalone lines should be removed from the template`() =
            wsTest(
                    "| This is\n{{#boolean}}\n|\n{{/boolean}}\n| A Line",
                    "| This is\n|\n| A Line"
            )

    @Test
    fun `Indented standalone lines should be removed from the template`() =
            wsTest(
                    "| This is\n  {{#boolean}}\n|\n  {{/boolean}}\n| A Line",
                    "| This is\n|\n| A Line"
            )

    @Test
    fun `CRLF should be considered a newline for standalone tags`() =
            wsTest(
                    "|\r\n{{#boolean}}\r\n{{/boolean}}\r\n|",
                    "|\r\n|"
            )

    @Test
    fun `Standalone tags should not require a newline to precede them`() =
            wsTest(
                    "  {{#boolean}}\n#{{/boolean}}\n/",
                    "#\n/"
            )

    @Test
    fun `Standalone tags should not require a newline to follow them`() =
            wsTest(
                    "#{{#boolean}}\n/\n  {{/boolean}}",
                    "#\n/\n"
            )

    @Test
    fun `Superfluous in-tag whitespace should be ignored`() =
            wsTest(
                    "|{{# boolean }}={{/ boolean }}|",
                    "|=|"
            )
}
