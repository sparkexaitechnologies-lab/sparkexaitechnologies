package com.example.ui.screens
import androidx.compose.ui.text.withStyle

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BW_Silver

@Composable
fun PremiumContainer(
    modifier: Modifier = Modifier,
    borderWidth: Dp = 1.dp,
    shape: RoundedCornerShape = RoundedCornerShape(18.dp),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .border(borderWidth, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), shape)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        content()
    }
}

@Composable
fun AudioWaveformIndicator(
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    isAnimating: Boolean = true
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val barCount = 7
        val infiniteTransition = rememberInfiniteTransition(label = "waveform")
        
        for (i in 0 until barCount) {
            val duration = remember { (400..1000).random() }
            val scale by if (isAnimating) {
                infiniteTransition.animateFloat(
                    initialValue = 0.2f,
                    targetValue = 1.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(duration, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "barScale"
                )
            } else {
                remember { mutableStateOf(0.3f) }
            }

            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(32.dp)
                    .scale(scaleY = scale, scaleX = 1f)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
        }
    }
}

@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    val blocks = remember(text) {
        val lines = text.split("\n")
        val parsedBlocks = mutableListOf<MarkdownBlock>()
        var inCodeBlock = false
        var codeContent = StringBuilder()
        var currentParagraph = StringBuilder()

        fun flushParagraph() {
            if (currentParagraph.isNotEmpty()) {
                parsedBlocks.add(MarkdownBlock.Paragraph(currentParagraph.toString().trim()))
                currentParagraph.setLength(0)
            }
        }

        for (line in lines) {
            if (line.trim().startsWith("```")) {
                if (inCodeBlock) {
                    parsedBlocks.add(MarkdownBlock.Code(codeContent.toString().trimEnd()))
                    codeContent.setLength(0)
                    inCodeBlock = false
                } else {
                    flushParagraph()
                    inCodeBlock = true
                }
                continue
            }

            if (inCodeBlock) {
                codeContent.append(line).append("\n")
                continue
            }

            val trimmedLine = line.trim()
            when {
                line.startsWith("|") && line.contains("|") -> {
                    flushParagraph()
                    val cells = line.split("|").filter { it.isNotBlank() }.map { it.trim() }
                    if (cells.isNotEmpty() && !line.contains("---")) {
                        parsedBlocks.add(MarkdownBlock.Table(cells))
                    }
                }
                line.startsWith("### ") -> {
                    flushParagraph()
                    parsedBlocks.add(MarkdownBlock.Header(line.removePrefix("### ").trim(), 3))
                }
                line.startsWith("## ") -> {
                    flushParagraph()
                    parsedBlocks.add(MarkdownBlock.Header(line.removePrefix("## ").trim(), 2))
                }
                line.startsWith("# ") -> {
                    flushParagraph()
                    parsedBlocks.add(MarkdownBlock.Header(line.removePrefix("# ").trim(), 1))
                }
                trimmedLine.startsWith("- ") || trimmedLine.startsWith("* ") -> {
                    flushParagraph()
                    parsedBlocks.add(MarkdownBlock.ListItem(trimmedLine.substring(2).trim(), isOrdered = false))
                }
                trimmedLine.firstOrNull()?.isDigit() == true && trimmedLine.getOrNull(1) == '.' -> {
                    flushParagraph()
                    val index = trimmedLine.takeWhile { it.isDigit() }
                    parsedBlocks.add(MarkdownBlock.ListItem(trimmedLine.substring(index.length + 2).trim(), isOrdered = true, order = index))
                }
                line.startsWith("> ") -> {
                    flushParagraph()
                    parsedBlocks.add(MarkdownBlock.BlockQuote(line.removePrefix("> ").trim()))
                }
                line.trim().isEmpty() -> {
                    flushParagraph()
                }
                else -> {
                    if (currentParagraph.isNotEmpty()) currentParagraph.append(" ")
                    currentParagraph.append(line)
                }
            }
        }
        flushParagraph()
        parsedBlocks
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.Paragraph -> {
                    Text(
                        text = parseInlineMarkdown(block.text),
                        style = MaterialTheme.typography.bodyLarge,
                        color = color.copy(alpha = 0.9f),
                        lineHeight = 28.sp
                    )
                }
                is MarkdownBlock.Header -> {
                    val style = when (block.level) {
                        1 -> MaterialTheme.typography.headlineSmall
                        2 -> MaterialTheme.typography.titleLarge
                        else -> MaterialTheme.typography.titleMedium
                    }
                    Text(
                        text = parseInlineMarkdown(block.text),
                        style = style,
                        fontWeight = FontWeight.Bold,
                        color = color,
                        modifier = Modifier.padding(top = if (block.level == 1) 12.dp else 4.dp)
                    )
                }
                is MarkdownBlock.Code -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = block.code,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp
                            ),
                            color = color.copy(alpha = 0.85f)
                        )
                    }
                }
                is MarkdownBlock.Table -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        block.cells.forEach { cell ->
                            Text(
                                text = parseInlineMarkdown(cell),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = color,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                is MarkdownBlock.ListItem -> {
                    Row(
                        modifier = Modifier.padding(start = 8.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (block.isOrdered) "${block.order}." else "•",
                            color = color,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = parseInlineMarkdown(block.text),
                            style = MaterialTheme.typography.bodyLarge,
                            color = color.copy(alpha = 0.9f),
                            lineHeight = 26.sp
                        )
                    }
                }
                is MarkdownBlock.BlockQuote -> {
                    Row(
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .drawBehind {
                                drawLine(
                                    color = color.copy(alpha = 0.2f),
                                    start = Offset(0f, 0f),
                                    end = Offset(0f, size.height),
                                    strokeWidth = 4.dp.toPx()
                                )
                            }
                            .padding(start = 16.dp)
                    ) {
                        Text(
                            text = parseInlineMarkdown(block.text),
                            style = MaterialTheme.typography.bodyLarge,
                            color = color.copy(alpha = 0.7f),
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }
        }
    }
}

sealed class MarkdownBlock {
    data class Paragraph(val text: String) : MarkdownBlock()
    data class Header(val text: String, val level: Int) : MarkdownBlock()
    data class Code(val code: String) : MarkdownBlock()
    data class Table(val cells: List<String>) : MarkdownBlock()
    data class ListItem(val text: String, val isOrdered: Boolean, val order: String = "") : MarkdownBlock()
    data class BlockQuote(val text: String) : MarkdownBlock()
}

fun parseInlineMarkdown(text: String): androidx.compose.ui.text.AnnotatedString {
    return androidx.compose.ui.text.buildAnnotatedString {
        var currentIndex = 0
        val boldRegex = Regex("\\*\\*(.*?)\\*\\*")
        val matches = boldRegex.findAll(text)
        for (match in matches) {
            append(text.substring(currentIndex, match.range.first))
            withStyle(style = androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold)) {
                append(match.groupValues[1])
            }
            currentIndex = match.range.last + 1
        }
        append(text.substring(currentIndex))
    }
}
