package com.smokingtracker.ui.components.markdown

sealed class MarkdownSection {
    data class Header1(val text: String) : MarkdownSection()
    data class Image(val url: String) : MarkdownSection()
    data class GroupedCard(val elements: List<MarkdownElement>) : MarkdownSection()
}

sealed class MarkdownElement {
    data class Header2(val text: String) : MarkdownElement()
    data class Header3(val text: String) : MarkdownElement()
    data class Text(val text: String) : MarkdownElement()
    data class ListItem(val text: String) : MarkdownElement()
    data class Quote(val text: String) : MarkdownElement()
}
