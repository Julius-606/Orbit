// ================================================================================
// FILE: Pocket_Orbit/app/src/main/java/com/example/pocket_orbit/ui/screens/MarkdownRenderer.kt
// VERSION: 1.0.0 | SYSTEM: Native Jetpack Compose Markdown Renderer
// IDENTITY: Parses and draws markdown tags like bold, inline code, blocks, and lists cleanly.
// VIBE: Pure, styled text tags without third-party dependency breakdown. 📝✨
// ================================================================================

package com.example.pocket_orbit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MarkdownText(text: String, color: Color = Color.Unspecified) {
    val lines = text.split("\n")
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        var inCodeBlock = false
        var codeBlockContent = StringBuilder()

        for (line in lines) {
            if (line.trim().startsWith("```")) {
                if (inCodeBlock) {
                    // End code block
                    CodeBlockDisplay(codeBlockContent.toString().trimEnd())
                    codeBlockContent = StringBuilder()
                    inCodeBlock = false
                } else {
                    // Start code block
                    inCodeBlock = true
                }
                continue
            }

            if (inCodeBlock) {
                codeBlockContent.append(line).append("\n")
                continue
            }

            // Parse list item
            if (line.trim().startsWith("- ") || line.trim().startsWith("* ")) {
                val cleanLine = line.trim().substring(2)
                Row(modifier = Modifier.padding(start = 8.dp)) {
                    Text("• ", color = color, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text(text = parseInlineMarkdown(cleanLine), color = color, fontSize = 15.sp)
                }
                continue
            }

            // Standard line parsing
            if (line.isNotBlank()) {
                Text(text = parseInlineMarkdown(line), color = color, fontSize = 15.sp)
            } else {
                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        // Catch unclosed code block safely
        if (inCodeBlock && codeBlockContent.isNotEmpty()) {
            CodeBlockDisplay(codeBlockContent.toString().trimEnd())
        }
    }
}

@Composable
fun CodeBlockDisplay(code: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF2D2D2D), shape = RoundedCornerShape(6.dp))
            .padding(10.dp)
    ) {
        Text(
            text = code,
            color = Color(0xFF61AFEF),
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp
        )
    }
}

fun parseInlineMarkdown(text: String): AnnotatedString {
    return buildAnnotatedString {
        var i = 0
        while (i < text.length) {
            // Bold (**text**)
            if (i < text.length - 1 && text[i] == '*' && text[i + 1] == '*') {
                val end = text.indexOf("**", i + 2)
                if (end != -1) {
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                    append(text.substring(i + 2, end))
                    pop()
                    i = end + 2
                    continue
                }
            }
            // Inline code (`code`)
            if (text[i] == '`') {
                val end = text.indexOf('`', i + 1)
                if (end != -1) {
                    pushStyle(SpanStyle(fontFamily = FontFamily.Monospace, color = Color(0xFFE5C07B), background = Color(0xFF3E4451)))
                    append(text.substring(i + 1, end))
                    pop()
                    i = end + 1
                    continue
                }
            }
            // Italics (*text*)
            if (text[i] == '*') {
                val end = text.indexOf('*', i + 1)
                if (end != -1) {
                    pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                    append(text.substring(i + 1, end))
                    pop()
                    i = end + 1
                    continue
                }
            }
            append(text[i].toString())
            i++
        }
    }
}
