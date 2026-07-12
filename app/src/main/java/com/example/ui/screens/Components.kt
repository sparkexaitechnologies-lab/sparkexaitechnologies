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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SilverAccent
import com.example.ui.theme.SparkexGold

@Composable
fun GlowBorderContainer(
    modifier: Modifier = Modifier,
    borderWidth: Dp = 1.dp,
    shape: RoundedCornerShape = RoundedCornerShape(16.dp),
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "borderGlow")
    val animProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "borderOffset"
    )

    val goldColor = SparkexGold
    val glowBrush = remember(animProgress, goldColor) {
        Brush.sweepGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.1f),
                Color.White.copy(alpha = 0.9f),
                Color.White.copy(alpha = 0.1f),
                goldColor.copy(alpha = 0.8f),
                Color.White.copy(alpha = 0.1f)
            )
        )
    }

    Box(
        modifier = modifier
            .clip(shape)
            .border(borderWidth, glowBrush, shape)
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
    var inCodeBlock = false
    var codeContent = ""

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val lines = text.split("\n")
        var currentParagraph = StringBuilder()

        val flushParagraph: @Composable () -> Unit = {
            if (currentParagraph.isNotEmpty()) {
                Text(
                    text = parseInlineMarkdown(currentParagraph.toString()),
                    style = MaterialTheme.typography.bodyLarge,
                    color = color,
                    lineHeight = 24.sp
                )
                currentParagraph.clear()
            }
        }

        for (line in lines) {
            if (line.trim().startsWith("```")) {
                if (inCodeBlock) {
                    // end code block
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF0F4F9))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = codeContent.trimEnd(),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily.Monospace
                            ),
                            color = Color(0xFF1C1C1E)
                        )
                    }
                    inCodeBlock = false
                    codeContent = ""
                } else {
                    flushParagraph()
                    inCodeBlock = true
                }
                continue
            }

            if (inCodeBlock) {
                codeContent += line + "\n"
                continue
            }

            when {
                line.startsWith("### ") -> {
                    flushParagraph()
                    Text(
                        text = parseInlineMarkdown(line.removePrefix("### ").trim()),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = color,
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                    )
                }
                line.startsWith("## ") -> {
                    flushParagraph()
                    Text(
                        text = parseInlineMarkdown(line.removePrefix("## ").trim()),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = color,
                        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                    )
                }
                line.startsWith("# ") -> {
                    flushParagraph()
                    Text(
                        text = parseInlineMarkdown(line.removePrefix("# ").trim()),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = color,
                        modifier = Modifier.padding(top = 20.dp, bottom = 6.dp)
                    )
                }
                line.trim().startsWith("- ") || line.trim().startsWith("* ") -> {
                    flushParagraph()
                    Row(
                        modifier = Modifier.padding(start = 8.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "•", color = color, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = parseInlineMarkdown(line.trim().substring(2).trim()),
                            style = MaterialTheme.typography.bodyLarge,
                            color = color,
                            lineHeight = 24.sp
                        )
                    }
                }
                line.trim().firstOrNull()?.isDigit() == true && line.trim().getOrNull(1) == '.' -> {
                    flushParagraph()
                    Row(
                        modifier = Modifier.padding(start = 8.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = line.trim().take(2),
                            color = color,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = parseInlineMarkdown(line.trim().substring(2).trim()),
                            style = MaterialTheme.typography.bodyLarge,
                            color = color,
                            lineHeight = 24.sp
                        )
                    }
                }
                line.isBlank() -> {
                    flushParagraph()
                }
                else -> {
                    if (currentParagraph.isNotEmpty()) {
                        currentParagraph.append(" ")
                    }
                    currentParagraph.append(line)
                }
            }
        }
        flushParagraph()
    }
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
