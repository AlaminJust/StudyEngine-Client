package com.gatishil.studyengine.presentation.screens.legal

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.gatishil.studyengine.R

enum class LegalDocumentType {
    PRIVACY_POLICY,
    TERMS_OF_SERVICE
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LegalScreen(
    documentType: LegalDocumentType,
    baseUrl: String,
    onNavigateBack: () -> Unit
) {
    val title = when (documentType) {
        LegalDocumentType.PRIVACY_POLICY -> stringResource(R.string.privacy_policy)
        LegalDocumentType.TERMS_OF_SERVICE -> stringResource(R.string.terms_of_service)
    }

    val url = when (documentType) {
        LegalDocumentType.PRIVACY_POLICY -> "$baseUrl/privacy-policy.html"
        LegalDocumentType.TERMS_OF_SERVICE -> "$baseUrl/terms-of-service.html"
    }

    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                windowInsets = WindowInsets(0.dp)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                isLoading = false
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                errorCode: Int,
                                description: String?,
                                failingUrl: String?
                            ) {
                                super.onReceivedError(view, errorCode, description, failingUrl)
                                hasError = true
                                isLoading = false
                            }
                        }
                        settings.javaScriptEnabled = false
                        settings.loadWithOverviewMode = true
                        settings.useWideViewPort = true
                        loadUrl(url)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            if (hasError) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.error_loading_document),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        OutlinedButton(onClick = onNavigateBack) {
                            Text(stringResource(R.string.go_back))
                        }
                    }
                }
            }
        }
    }
}

