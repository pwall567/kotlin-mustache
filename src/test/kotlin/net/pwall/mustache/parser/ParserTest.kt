package net.pwall.mustache.parser

import kotlin.test.Test
import kotlin.test.expect

import java.io.StringReader

import net.pwall.mustache.parser.Parser.Companion.readUntilDelimiter

class ParserTest {

    @Test fun `should read entire string when delimiter not present`() {
        val reader = StringReader("ABCD")
        val stringBuffer = StringBuilder()
        expect(false) {reader.readUntilDelimiter("//", stringBuffer)}
        expect("ABCD") { stringBuffer.toString() }
    }

    @Test fun `should read until delimiter`() {
        val reader = StringReader("ABCD//EFG")
        val stringBuffer = StringBuilder()
        expect(true) {reader.readUntilDelimiter("//", stringBuffer)}
        expect("ABCD") { stringBuffer.toString() }
    }

    @Test fun `should read until delimiter when partial delimiter present`() {
        val reader = StringReader("AB/CD//EFG")
        val stringBuffer = StringBuilder()
        expect(true) {reader.readUntilDelimiter("//", stringBuffer)}
        expect("AB/CD") { stringBuffer.toString() }
    }

}
