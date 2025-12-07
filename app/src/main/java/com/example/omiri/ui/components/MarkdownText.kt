package com.example.omiri.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp

/**
 * A simple Markdown text renderer that supports:
 * - **Bold**
 * - *Italic*
 * - `Inline Code`
 */
@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    style: TextStyle = MaterialTheme.typography.bodyMedium
) {
    val styledText = parseMarkdown(markdown, color)
    
    Text(
        text = styledText,
        modifier = modifier,
        style = style,
        color = color
    )
}

/**
 * Parse simple markdown syntax into AnnotatedString
 */
@Composable
private fun parseMarkdown(markdown: String, baseColor: Color): AnnotatedString {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    
    return buildAnnotatedString {
        var currentIndex = 0
        
        // Regex for bold (**text**), italic (*text*), and code (`text`)
        // The order matters!
        val pattern = "(\\*\\*.*?\\*\\*)|(`.*?`)|(\\*.*?\\*)".toRegex()
        
        val matches = pattern.findAll(markdown)
        
        for (match in matches) {
            // Append text before the match
            if (match.range.first > currentIndex) {
                append(markdown.substring(currentIndex, match.range.first))
            }
            
            val matchText = match.value
            when {
                // Bold: **text**
                matchText.startsWith("**") -> {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(matchText.removePrefix("**").removeSuffix("**"))
                    }
                }
                // Code: `text`
                matchText.startsWith("`") -> {
                    withStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = surfaceVariant.copy(alpha = 0.5f),
                            color = onSurfaceVariant
                        )
                    ) {
                        append(matchText.removePrefix("`").removeSuffix("`"))
                    }
                }
                // Italic: *text*
                matchText.startsWith("*") -> {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(matchText.removePrefix("*").removeSuffix("*"))
                    }
                }
            }
            
            currentIndex = match.range.last + 1
        }
        
        // Append remaining text
        if (currentIndex < markdown.length) {
            append(markdown.substring(currentIndex))
        }
    }
}
