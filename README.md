# kotlin-mustache

[![Build Status](https://travis-ci.com/pwall567/kotlin-mustache.svg?branch=main)](https://travis-ci.com/github/pwall567/kotlin-mustache)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Kotlin](https://img.shields.io/static/v1?label=Kotlin&message=v1.7.21&color=7f52ff&logo=kotlin&logoColor=7f52ff)](https://github.com/JetBrains/kotlin/releases/tag/v1.7.21)
[![Maven Central](https://img.shields.io/maven-central/v/net.pwall.mustache/kotlin-mustache?label=Maven%20Central)](https://search.maven.org/search?q=g:%22net.pwall.mustache%22%20AND%20a:%22kotlin-mustache%22)

Kotlin implementation of [Mustache](https://mustache.github.io/mustache.5.html) templates.

This is a not a complete implementation, but it covers the majority of features described in the Mustache Manual, and it
provides some additional features that have been found to be valuable.

This documentation is a work in progress - more will be added over time.

## Reference

The reference [Mustache Specification](https://github.com/mustache/spec) contains the definitive description of the
Mustache template system.
This implementation does not include all features covered by the specification; the differences are detailed below.

A Mustache template is a block of text to be copied to the output, interspersed with "tags" that control the
substitution of values from a supplied object &ndash; the "context object".
All text that is not part of a tag is copied without modification to the generated output.
The tags are described below.

### Tags

To distinguish tags from general text, the original authors chose the open brace (mustache) character.
Tags are opened by two left brace characters (`{{`) and closed by two right brace characters (`}}`).
(These delimiters may be changed if the use of these characters would cause a conflict &ndash; see
[below](#set-delimiter).)

#### Variables

The simplest tag type is the variable, which is replaced in the output by the result of resolving the variable in the
context object.
If the context object is a `Map`, the variable name is used as a key to locate an entry; otherwise the variable name is
used to locate a property in the Kotlin object.
See [Name Resolution](#name-resolution) below for more detail.

For example, given the template:
```handlebars
Hello {{who}}
```

Then either:
```kotlin
    val contextObject = mapOf("who" to "World")
    template.processToString(contextObject)
```
Or:
```kotlin
    data class ContextObject(val who: String)

    val contextObject = ContextObject("World")
    template.processToString(contextObject)
```
Will cause the generated output to be:
```text
Hello World
```

If the name is not located &ndash; the key is not found in the map or the class does not have a variable with the
specified name &ndash; no text will be output.
The name `.` will select the entire context object.

By default, Mustache performs HTML escaping on the substituted text &ndash; that is, HTML special characters are
converted to the HTML entity references for those characters.
For example, `<` is converted to `&lt;` and `&` is converted to `&amp;`.

If this HTML escaping is not required, the literal form of the variable tag may be used - either `{{{name}}}` (three
braces instead of two) or `{{&name}}` (ampersand before the variable name).

The name may be a structured name &ndash; see [Name Resolution](#name-resolution) below.

#### Sections

A section is a block of text to be processed zero or more times depending on the value nominated by the section name.
Sections are introduced by a opening tag which has a `#` following the two left braces, and closed by a tag with a `/`
following the left braces, for example `{{#person}}...{{/person}}`.
Sections may be nested, and closing tags must match the most recent unmatched opening tag.

The content of the section is processed conditionally, depending on the type of the value (the value is determined using
[the rules detailed below](#name-resolution)):

- `Iterable` (e.g. `List`), `Array`: the content of the section is processed for each entry in the collection, with the
individual entry used as the context object for the nested content
- `Map`: the content of the section is processed for each entry in the map, with the `Map.Entry` used as the context
object for the nested content
- `Enum`: the content of the section is processed with the enum as the context object, allowing the individual enum
values to be used to control nested sections (see [Enums](#enums) below)
- `Enum` values: the content of the section is processed if the context object is an enum with the specified value (see
[Enums](#enums) below)
- `String` (and other forms of `CharSequence`): the content of the section is processed if the string is not empty
- `Int`, `Long`, `Short`, `Byte`, `Double`, `Float`, `BigInteger` and `BigDecimal`: the content of the section is
processed if the value is non-zero
- `Boolean`: the content of the section is processed if the value is `true`
- anything else: the content of the section is processed if the value is not `null`, with the value used as the context
object for the nested content (this is how properties of nested objects may be accessed)

When processing an `Iterable` or `Map`, additional names are available in the context for each item processed:
- `first` (`Boolean`) &ndash; `true` if the item is the first in the collection
- `last` (`Boolean`) &ndash; `true` if the item is the last in the collection
- `index` (`Int`) &ndash; the index (zero-based) of the item in the collection
- `index1` (`Int`) &ndash; the index (one-based) of the item in the collection

#### Inverted Sections

An inverted section is a block of text to be processed once, if the value is null or the following:

- `Iterable` (`List` etc.), `Array`, `Map`: if the collection or map is empty
- `Enum` values: if the enum does not have the specified value
- `String` etc.: if the string is empty
- number types: if the number is non-zero
- `Boolean`: if the value is `false`

Inverted sections are introduced by a opening tag which has a `^` following the two left braces, and closed by a tag
with a `/` following the left braces.
Sections and inverted sections may nest within each other.

#### Partials

"Partial" is the term given to a nested section &ndash; for example a template in a separate file.
Partials are introduced by a tag with '>' following the two left braces &ndash; the remainder of the tag is the name of
an external file.

In the most common usage, the directory and extension of the first template will be stored and used as the directory and
extension for any partials encountered in that template.

Recursive data structures may be processed by recursive templates.

#### Set Delimiter

For cases where the standard double-brace delimiters clash with the text being processed, the delimiters may be changed
to some other combination of characters for the remainder of the current template.
A "Set Delimiter" tag consists of the current opening delimiter, immediately followed by an equals sign '=', then the
new opening delimiter, one or more spaces, the new closing delimiter, an equals sign '=' again, and finally the current
closing delimiter.

For example, to change the delimiters to `<%` and `%>`:
```
{{=<% %>=}}
```

The delimiters may not contain spaces or the equals sign.

### Name Resolution

The variable and section (including inverted section) tags use the following rules to determine the value to be used for
the tag:
- If the context object is `null`, the result value is `null`
- If the context object is a `Map` containing a key with the tag name, the value associated with that key is used
- If the context object has a property with the tag name, that property is used
- If the current context is a nested context (part of a section or inverted section), the context object of the outer
context is searched using these same rules (repeatedly up to the outermost context)
- If nothing is found, `null` is returned

The variable or section may be specified in a structured form, e.g. `person.firstName`.
In this case, the above rules are used for the first part of the name (the part before the first dot), but for the
subsequent parts only the result of the first part is searched and the enclosing contexts do form part of the process.

### Whitespace

This implementation treats all whitespace as significant, and copies it to the output.
This includes the newline at the end of a file, so if, for example, you have a partial to substitute a word or phrase
into the middle of a line, then to preserve line formatting the partial must not have a newline at the end of the file.

### Enums

One very powerful way in which this library differs from other Mustache implementations is in its handling of enums.
If an enum value is used in a [Section](#sections), the contents of the section are processed with the context object
for the nested content set to a special object that yields `true` for the name corresponding to the value of the enum,
and `false` for all other values.

For example:
```kotlin
    enum class Type { CREDIT, DEBIT }
    data class Transaction(val type: Type, val amount: Int)

    val template = Template.parse("{{#type}}{{#CREDIT}}+{{/CREDIT}}{{#DEBIT}}-{{/DEBIT}}{{/type}}{{&amount}}")
    println(template.render(Transaction(Type.DEBIT, 100))) // will print -100
```

## Dependency Specification

The latest version of the library is 0.12, and it may be obtained from the Maven Central repository.

### Maven
```xml
    <dependency>
      <groupId>net.pwall.mustache</groupId>
      <artifactId>kotlin-mustache</artifactId>
      <version>0.12</version>
    </dependency>
```
### Gradle
```groovy
    implementation 'net.pwall.mustache:kotlin-mustache:0.12'
```
### Gradle (kts)
```kotlin
    implementation("net.pwall.mustache:kotlin-mustache:0.12")
```

Peter Wall

2024-03-11
