package com.gatishil.studyengine.presentation.common.components

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun LatexText(
    text: String,
    modifier: Modifier = Modifier
) {
    val hasLatex = remember(text) { text.contains("$") || text.contains("\\ce{") }
    
    if (hasLatex) {
        LatexWebView(text, modifier)
    } else {
        SimpleMarkdownText(text, modifier)
    }
}

@Composable
private fun SimpleMarkdownText(text: String, modifier: Modifier) {
    val annotatedText = remember(text) { parseSimpleMarkdown(text) }
    Text(
        text = annotatedText,
        modifier = modifier.fillMaxWidth(),
        style = MaterialTheme.typography.bodyLarge
    )
}

private fun parseSimpleMarkdown(text: String): AnnotatedString = buildAnnotatedString {
    var i = 0
    while (i < text.length) {
        when {
            text.startsWith("**", i) -> {
                val end = text.indexOf("**", i + 2)
                if (end != -1) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(text.substring(i + 2, end))
                    }
                    i = end + 2
                } else {
                    append(text[i++])
                }
            }
            text.startsWith("*", i) && !text.startsWith("**", i) -> {
                val end = text.indexOf("*", i + 1)
                if (end != -1) {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(text.substring(i + 1, end))
                    }
                    i = end + 1
                } else {
                    append(text[i++])
                }
            }
            else -> append(text[i++])
        }
    }
}

@Composable
private fun LatexWebView(text: String, modifier: Modifier) {
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val backgroundColor = MaterialTheme.colorScheme.surface.toArgb()
    
    val htmlContent = remember(text, textColor, backgroundColor) {
        createLatexHtml(text, textColor, backgroundColor)
    }

    AndroidView(
        modifier = modifier.fillMaxWidth().heightIn(min = 40.dp, max = 300.dp),
        factory = { ctx ->
            WebView(ctx).apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    cacheMode = android.webkit.WebSettings.LOAD_CACHE_ELSE_NETWORK
                    loadWithOverviewMode = true
                    useWideViewPort = false
                }
                setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                webViewClient = WebViewClient()
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL("https://cdn.jsdelivr.net", htmlContent, "text/html", "UTF-8", null)
        }
    )
}

private fun createLatexHtml(text: String, textColor: Int, backgroundColor: Int): String {
    val textColorHex = String.format("#%06X", 0xFFFFFF and textColor)
    val backgroundColorHex = String.format("#%06X", 0xFFFFFF and backgroundColor)
    val escapedText = text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
    
    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/katex.min.css" crossorigin="anonymous">
            <style>
                * { margin: 0; padding: 0; box-sizing: border-box; }
                html, body { height: auto; overflow: hidden; }
                body {
                    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                    color: $textColorHex;
                    background-color: $backgroundColorHex;
                    padding: 4px 8px;
                    font-size: 16px;
                    line-height: 1.5;
                }
                .katex { font-size: 1.05em; }
                .katex-display { margin: 0.3em 0; }
                strong { font-weight: bold; }
                em { font-style: italic; }
            </style>
        </head>
        <body>
            <div id="content"></div>
            <script src="https://cdn.jsdelivr.net/npm/marked@11.1.1/marked.min.js" crossorigin="anonymous"></script>
            <script src="https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/katex.min.js" crossorigin="anonymous"></script>
            <script src="https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/contrib/auto-render.min.js" crossorigin="anonymous"></script>
            <script src="https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/contrib/mhchem.min.js" crossorigin="anonymous"></script>
            <script>
                var text = "$escapedText";
                document.getElementById('content').innerHTML = marked.parseInline(text);
                renderMathInElement(document.getElementById('content'), {
                    delimiters: [
                        {left: '$$', right: '$$', display: true},
                        {left: '$', right: '$', display: false}
                    ],
                    throwOnError: false
                });
            </script>
        </body>
        </html>
    """.trimIndent()
}
