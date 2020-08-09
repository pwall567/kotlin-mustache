# kotlin-mustache

Kotlin implementation of [Mustache](https://mustache.github.io/mustache.5.html) templates.

This is a not a complete implementation, but it covers the majority of features described in the Mustache Manual, and it
provides some additional features that have been found to be valuable.

This documentation is a work in progress - more will be added over time.

## Reference

The reference [Mustache Specification](https://github.com/mustache/spec) contains the definitive description of the
Mustache template system.
This implementation does not include all features covered by the specification; the differences are detailed below.

A Mustache template is a block of text to be copied to the output, interspersed with "tags" that control the
substitution of values from a supplied object - the "context object".
All text that is not part of a tag is copied without modification to the generated output.
The tags are described below.

### Tags

To distinguish tags from general text, the original authors chose the open brace (mustache) character.
Tags are opened by two left brace characters (`{{`) and closed by two right brace characters (`}}`).
(These delimiters may be changed if the use of these characters would cause a conflict - see later.)

#### Variables

The simplest tag type is the variable, which is replaced in the output by the result of resolving the variable in the
context object.
If the context object is a `Map`, the variable name is used as a key to locate an entry; otherwise the variable name is
used to locate a property in the Kotlin object.

For example, given the template:
```handlebars
Hello {{who}}
```

Then either:
```kotlin
    val contextObject = mapOf("who" to "World")
```
Or:
```kotlin
    data class ContextObject(val who: String)

    val contextObject = ContextObject("World")
```
Will cause the generated output to be:
```text
Hello World
```

If the name is not located - the key is not found in the map or the class does not have a variable with the specified
name - no text will be output.
The name `.` can be used to select the entire context object.

By default, Mustache performs HTML escaping on the substituted text - that is, HTML special characters are converted to
the HTML entity references for those characters.
For example, `<` is converted to `&lt;` and `&` is converted to `&amp;`.

If this HTML escaping is not required, the literal form of the variable tag may be used - either `{{{name}}}` (three
braces instead of two) or `{{&name}}` (ampersand before the variable name).

Some implementations of Mustache allow a structured name in a variable tag; this implementation does not allow anything
other than a simple selector - a map key or a property name.
(See the Section tag below to see how to get around this limitation.)

#### Sections

A section is a block of text to be processed zero or more times depending on the value nominated by the section name.
Sections are introduced by a opening tag which has a `#` following the two left braces, and closed by a tag with a `/`
following the left braces, for example `{{#person}}...{{/person}}`.
Sections may be nested, and closing tags must match the most recent unmatched opening tag.

The content of the section is processed conditionally, depending on the type of the value:

- `Iterable` (e.g. `List`), `Array`: the content of the section is processed for each entry in the collection, with the
individual entry used as the context object for the nested content
- `Map`: the content of the section is processed for each entry in the map, with the `Map.Entry` used as the context
object for the nested content
- `Enum`: the content of the section is processed with the enum as the context object, allowing the individual enum
values to be used to control nested sections
- `Enum` values: the content of the section is processed if the context object is an enum with the specified value
- `String` (and other forms of `CharSequence`): the content of the section is processed if the string is not empty
- `Int`, `Long`, `Short`, `Byte`, `Double`, `Float`, `BigInteger` and `BigDecimal`: the content of the section is
processed if the value is non-zero
- `Boolean`: the content of the section is processed if the value is `true`
- anything else: the content of the section is processed if the value is not `null`, with the value used as the context
object for the nested content (this is how properties of nested objects may be accessed)

#### Inverted Sections

An inverted section is a block of text to be processed once, if the value is null or the following:

- `Iterable` (`List` etc.), `Array`, `Map`: if the collection or map is empty
- `Enum` values: if the enum to not have the specified value
- `String` etc.: if the string is empty
- number types: if the number is non-zero
- `Boolean`: if the value is `false`

Inverted sections are introduced by a opening tag which has a `^` following the two left braces, and closed by a tag
with a `/` following the left braces.
Sections and inverted sections may nest within each other.

#### Partials

Partial is the term given to a nested section - for example a template in a separate file.
Partials are introduced by a tag with '>' following the two left braces - the remainder of the tag is the name of an
external file.

In the most common usage, the directory and extension of the first template will be stored and used as the directory and
extension for any partials encountered in that template.

Recursive data structures may be processed by recursive templates.

## Dependency Specification

The latest version of the library is 0.5, and it may be obtained from the Maven Central repository.

### Maven
```xml
    <dependency>
    <groupId>net.pwall.mustache</groupId>
    <artifactId>kotlin-mustache</artifactId>
    <version>0.5</version>
    </dependency>
```
### Gradle
```groovy
    testImplementation 'net.pwall.mustache:kotlin-mustache:0.5'
```
### Gradle (kts)
```kotlin
    testImplementation("net.pwall.mustache:kotlin-mustache:0.5")
```

Peter Wall

2020-08-09
