package com.smokingtracker.ui.components.markdown

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration

object MarkdownParser {

    val HEADER_3_PLUS_REGEX = Regex("^#{3,6}\\s*")
    val HTML_IMG_SRC_DOUBLE_QUOTE = Regex("""src="([^"]+)"""", RegexOption.IGNORE_CASE)
    val HTML_IMG_SRC_SINGLE_QUOTE = Regex("""src='([^']+)'""", RegexOption.IGNORE_CASE)

    fun parseMarkdownIntoSections(content: String): List<MarkdownSection> {
        val lines = content.trim().lines()
        val result = mutableListOf<MarkdownSection>()
        var currentGroup = mutableListOf<MarkdownElement>()

        fun flushGroup() {
            if (currentGroup.isNotEmpty()) {
                result.add(MarkdownSection.GroupedCard(currentGroup.toList()))
                currentGroup = mutableListOf()
            }
        }

        lines.forEach { line ->
            val trimmed = line.trim()
            when {
                trimmed.startsWith("# ") -> {
                    flushGroup()
                    result.add(MarkdownSection.Header1(trimmed.removePrefix("# ")))
                }
                trimmed.startsWith("![") && trimmed.contains("(") && trimmed.contains(")") -> {
                    flushGroup()
                    val url = trimmed.substringAfter("(").substringBefore(")")
                    if (url.isNotBlank()) {
                        result.add(MarkdownSection.Image(url))
                    }
                }
                trimmed.contains("<img", ignoreCase = true) -> {
                    flushGroup()
                    val src = HTML_IMG_SRC_DOUBLE_QUOTE.find(trimmed)?.groupValues?.getOrNull(1)
                        ?: HTML_IMG_SRC_SINGLE_QUOTE.find(trimmed)?.groupValues?.getOrNull(1)
                        ?: if (trimmed.contains("src=\"", ignoreCase = true)) {
                            trimmed.substringAfter("src=\"", "").substringBefore("\"")
                        } else if (trimmed.contains("src='", ignoreCase = true)) {
                            trimmed.substringAfter("src='", "").substringBefore("'")
                        } else {
                            ""
                        }
                    if (src.isNotBlank()) {
                        result.add(MarkdownSection.Image(src))
                    }
                }
                trimmed.startsWith("## ") -> {
                    currentGroup.add(MarkdownElement.Header2(trimmed.removePrefix("## ")))
                }
                trimmed.startsWith("### ") || trimmed.startsWith("#### ") -> {
                    val headerText = trimmed.replace(HEADER_3_PLUS_REGEX, "")
                    currentGroup.add(MarkdownElement.Header3(headerText))
                }
                trimmed.startsWith("* ") || trimmed.startsWith("- ") || trimmed.startsWith("+ ") -> {
                    currentGroup.add(MarkdownElement.ListItem(trimmed.substring(2).trim()))
                }
                trimmed.startsWith("> ") -> {
                    currentGroup.add(MarkdownElement.Quote(trimmed.removePrefix("> ").trim()))
                }
                trimmed.isNotBlank() && !trimmed.startsWith("<br", ignoreCase = true) -> {
                    currentGroup.add(MarkdownElement.Text(trimmed))
                }
            }
        }
        flushGroup()
        return result
    }

    fun parseInlineMarkdown(
        text: String,
        primaryColor: Color = Color.Unspecified,
        secondaryColor: Color = Color.Unspecified,
        onSurfaceColor: Color = Color.Unspecified,
        codeBackgroundColor: Color = Color.Unspecified
    ): AnnotatedString {
        val builder = AnnotatedString.Builder()
        var i = 0

        while (i < text.length) {
            when {
                text.startsWith("**", i) -> {
                    val end = text.indexOf("**", i + 2)
                    if (end != -1) {
                        builder.pushStyle(SpanStyle(fontWeight = FontWeight.Bold, color = onSurfaceColor))
                        builder.append(text.substring(i + 2, end))
                        builder.pop()
                        i = end + 2
                    } else {
                        builder.append(text[i])
                        i++
                    }
                }
                text.startsWith("*", i) -> {
                    val end = text.indexOf("*", i + 1)
                    if (end != -1) {
                        builder.pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                        builder.append(text.substring(i + 1, end))
                        builder.pop()
                        i = end + 1
                    } else {
                        builder.append(text[i])
                        i++
                    }
                }
                text.startsWith("`", i) -> {
                    val end = text.indexOf("`", i + 1)
                    if (end != -1) {
                        builder.pushStyle(
                            SpanStyle(
                                fontFamily = FontFamily.Monospace,
                                background = codeBackgroundColor
                            )
                        )
                        builder.append(text.substring(i + 1, end))
                        builder.pop()
                        i = end + 1
                    } else {
                        builder.append(text[i])
                        i++
                    }
                }
                text.startsWith("![", i) -> {
                    val endAlt = text.indexOf("]", i)
                    if (endAlt != -1 && text.startsWith("(", endAlt + 1)) {
                        val endUrl = text.indexOf(")", endAlt + 1)
                        if (endUrl != -1) {
                            i = endUrl + 1
                        } else {
                            builder.append(text[i])
                            i++
                        }
                    } else {
                        builder.append(text[i])
                        i++
                    }
                }
                text.startsWith("[", i) -> {
                    val endText = text.indexOf("]", i)
                    if (endText != -1 && text.startsWith("(", endText + 1)) {
                        val endUrl = text.indexOf(")", endText + 1)
                        if (endUrl != -1) {
                            val linkText = text.substring(i + 1, endText)
                            val url = text.substring(endText + 2, endUrl)
                            builder.pushStyle(
                                SpanStyle(
                                    color = primaryColor,
                                    textDecoration = TextDecoration.Underline,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            builder.append(linkText)
                            builder.addStringAnnotation("URL", url, builder.length - linkText.length, builder.length)
                            builder.pop()
                            i = endUrl + 1
                        } else {
                            builder.append(text[i])
                            i++
                        }
                    } else {
                        builder.append(text[i])
                        i++
                    }
                }
                text.startsWith("<img", i, ignoreCase = true) -> {
                    val end = text.indexOf(">", i)
                    if (end != -1) {
                        i = end + 1
                    } else {
                        builder.append(text[i])
                        i++
                    }
                }
                text.startsWith("<br", i, ignoreCase = true) -> {
                    val end = text.indexOf(">", i)
                    if (end != -1) {
                        i = end + 1
                    } else {
                        builder.append(text[i])
                        i++
                    }
                }
                text[i] == '#' && i + 1 < text.length && text[i + 1].isDigit() -> {
                    var j = i + 1
                    while (j < text.length && text[j].isDigit()) {
                        j++
                    }
                    builder.pushStyle(
                        SpanStyle(
                            color = secondaryColor,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    builder.append(text.substring(i, j))
                    builder.pop()
                    i = j
                }
                else -> {
                    builder.append(text[i])
                    i++
                }
            }
        }
        return builder.toAnnotatedString()
    }
}

fun parseMarkdownIntoSections(content: String): List<MarkdownSection> =
    MarkdownParser.parseMarkdownIntoSections(content)

fun parseInlineMarkdown(
    text: String,
    primaryColor: Color = Color.Unspecified,
    secondaryColor: Color = Color.Unspecified,
    onSurfaceColor: Color = Color.Unspecified,
    codeBackgroundColor: Color = Color.Unspecified
): AnnotatedString = MarkdownParser.parseInlineMarkdown(
    text = text,
    primaryColor = primaryColor,
    secondaryColor = secondaryColor,
    onSurfaceColor = onSurfaceColor,
    codeBackgroundColor = codeBackgroundColor
)

@Composable
fun parseInlineMarkdown(text: String): AnnotatedString {
    val colorScheme = MaterialTheme.colorScheme
    return MarkdownParser.parseInlineMarkdown(
        text = text,
        primaryColor = colorScheme.primary,
        secondaryColor = colorScheme.secondary,
        onSurfaceColor = colorScheme.onSurface,
        codeBackgroundColor = colorScheme.surfaceVariant
    )
}
