package com.gatishil.studyengine.presentation.common.components

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.toArgb

@Composable
fun LatexText(
    text: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val backgroundColor = MaterialTheme.colorScheme.surface.toArgb()
    
    val htmlContent = remember(text, textColor, backgroundColor) {
        createLatexHtml(text, textColor, backgroundColor)
    }

    AndroidView(
        modifier = modifier.fillMaxWidth().heightIn(min = 40.dp, max = 300.dp),
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = false
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                webViewClient = WebViewClient()
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
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
            <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/katex.min.css">
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
            <script src="https://cdn.jsdelivr.net/npm/marked@11.1.1/marked.min.js"></script>
            <script src="https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/katex.min.js"></script>
            <script src="https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/contrib/auto-render.min.js"></script>
            <script src="https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/contrib/mhchem.min.js"></script>
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
