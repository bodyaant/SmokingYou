package com.smokingtracker.ui.components.markdown

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ListAlt
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun MarkdownRenderer(
    content: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
) {
    MarkdownBody(
        content = content,
        containerColor = containerColor,
        modifier = modifier
    )
}

@Composable
fun MarkdownBody(
    content: String,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
    modifier: Modifier = Modifier
) {
    val sections = remember(content) { parseMarkdownIntoSections(content) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
fun MarkdownGroupedCard(
    elements: List<MarkdownElement>,
    containerColor: Color,
    modifier: Modifier = Modifier
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

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
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
fun MarkdownHeaderItem(
    text: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    style: TextStyle = MaterialTheme.typography.titleMedium,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(vertical = 4.dp)
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
fun MarkdownListItemRow(
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
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
fun MarkdownQuoteItem(
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
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
fun MarkdownImageItem(
    url: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
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
