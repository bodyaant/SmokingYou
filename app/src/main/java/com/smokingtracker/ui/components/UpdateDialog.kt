package com.smokingtracker.ui.components

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ListAlt
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.smokingtracker.R
import com.smokingtracker.data.manager.GitHubRelease
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateDialog(
    release: GitHubRelease,
    onDismissRequest: () -> Unit
) {
    UpdateBottomSheet(
        release = release,
        onDismissRequest = onDismissRequest
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateBottomSheet(
    release: GitHubRelease,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    val hideAndDismiss: () -> Unit = {
        scope.launch {
            try {
                sheetState.hide()
            } finally {
                onDismissRequest()
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = screenHeight * 0.9f)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
            ) {
                UpdateHeaderSection(
                    tagName = release.tagName.orEmpty()
                )

                val body = release.body
                if (!body.isNullOrBlank()) {
                    MarkdownBody(
                        content = body,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = hideAndDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.update_dialog_later),
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = {
                            val apkAsset = release.assets?.firstOrNull { it.name?.endsWith(".apk") == true }
                            val downloadUrl = apkAsset?.browserDownloadUrl ?: release.htmlUrl
                            if (!downloadUrl.isNullOrEmpty()) {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, downloadUrl.toUri())
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.update_error, e.localizedMessage ?: downloadUrl),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.update_error, "No URL"),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            hideAndDismiss()
                        },
                        modifier = Modifier
                            .weight(1.4f)
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Download,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.update_dialog_download),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UpdateHeaderSection(
    tagName: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(68.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(34.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.update_dialog_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )

        if (tagName.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = CircleShape
            ) {
                Text(
                    text = tagName,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun MarkdownBody(
    content: String,
    containerColor: Color
) {
    val sections = remember(content) { parseMarkdownIntoSections(content) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        sections.forEach { section ->
            when (section) {
                is MarkdownSection.Header1 -> {
                    Text(
                        text = parseInlineMarkdown(section.text),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                is MarkdownSection.GroupedCard -> {
                    MarkdownGroupedCard(
                        elements = section.elements,
                        containerColor = containerColor
                    )
                }
                is MarkdownSection.Image -> {
                    MarkdownImageItem(url = section.url)
                }
            }
        }
    }
}

@Composable
private fun MarkdownGroupedCard(
    elements: List<MarkdownElement>,
    containerColor: Color
) {
    val elementGroups = remember(elements) {
        val groups = mutableListOf<List<MarkdownElement>>()
        var currentSubGroup = mutableListOf<MarkdownElement>()

        elements.forEach { element ->
            if (element is MarkdownElement.Header2 && currentSubGroup.isNotEmpty()) {
                groups.add(currentSubGroup.toList())
                currentSubGroup = mutableListOf()
            }
            currentSubGroup.add(element)
        }
        if (currentSubGroup.isNotEmpty()) groups.add(currentSubGroup)
        groups
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        elementGroups.forEach { group ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = containerColor),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    group.forEach { element ->
                        when (element) {
                            is MarkdownElement.Header2 -> {
                                MarkdownHeaderItem(
                                    text = element.text,
                                    icon = Icons.AutoMirrored.Outlined.ListAlt,
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            is MarkdownElement.Header3 -> {
                                MarkdownHeaderItem(
                                    text = element.text,
                                    icon = Icons.Outlined.AutoAwesome,
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                            is MarkdownElement.ListItem -> {
                                MarkdownListItemRow(text = element.text)
                            }
                            is MarkdownElement.Text -> {
                                Text(
                                    text = parseInlineMarkdown(element.text),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                            is MarkdownElement.Quote -> {
                                MarkdownQuoteItem(text = element.text)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MarkdownHeaderItem(
    text: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    style: TextStyle = MaterialTheme.typography.titleMedium
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(containerColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = parseInlineMarkdown(text),
            style = style,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun MarkdownListItemRow(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(top = 7.dp, start = 2.dp)
                .size(6.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = parseInlineMarkdown(text),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MarkdownQuoteItem(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = parseInlineMarkdown(text),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MarkdownImageItem(url: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(url)
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 350.dp)
                .clip(RoundedCornerShape(20.dp)),
            contentScale = ContentScale.FillWidth
        )
    }
}

@Composable
private fun parseInlineMarkdown(text: String): AnnotatedString {
    val builder = AnnotatedString.Builder()
    var i = 0
    val colorScheme = MaterialTheme.colorScheme
    val onSurface = colorScheme.onSurface

    while (i < text.length) {
        when {
            text.startsWith("**", i) -> {
                val end = text.indexOf("**", i + 2)
                if (end != -1) {
                    builder.pushStyle(SpanStyle(fontWeight = FontWeight.Bold, color = onSurface))
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
                            background = colorScheme.surfaceVariant
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
                                color = colorScheme.primary,
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
                        color = colorScheme.secondary,
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

private sealed class MarkdownSection {
    data class Header1(val text: String) : MarkdownSection()
    data class Image(val url: String) : MarkdownSection()
    data class GroupedCard(val elements: List<MarkdownElement>) : MarkdownSection()
}

private sealed class MarkdownElement {
    data class Header2(val text: String) : MarkdownElement()
    data class Header3(val text: String) : MarkdownElement()
    data class Text(val text: String) : MarkdownElement()
    data class ListItem(val text: String) : MarkdownElement()
    data class Quote(val text: String) : MarkdownElement()
}

private fun parseMarkdownIntoSections(content: String): List<MarkdownSection> {
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
                val src = if (trimmed.contains("src=\"", ignoreCase = true)) {
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
                val headerText = trimmed.replace(Regex("^#{3,6}\\s*"), "")
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
