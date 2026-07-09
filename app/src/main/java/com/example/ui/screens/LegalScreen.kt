package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalScreen(
    type: String, // "privacy" or "terms"
    onBack: () -> Unit
) {
    val isPrivacy = type == "privacy"
    val title = if (isPrivacy) "Privacy Policy" else "Terms of Service"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Last Updated: July 2026",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isPrivacy) {
                item {
                    Text("1. Information We Collect", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "Sparkex AI operates under enterprise-grade offline synchronizations. All chat logs, settings profiles, and uploaded visual materials are securely stored on your local SQLite storage through encrypted schema tables. When calling our Gemini LLM APIs, prompt texts and image buffers are sent securely using TLS encryption protocols directly to Google's backend processing hosts.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                item {
                    Text("2. Enterprise-Grade Security & Encryption", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "Our databases are protected with dynamic structural isolation boundaries. We never store, extract, or sell user personal communications. Generated media buffers are preserved exclusively in the app's sandboxed local cache files. We strictly follow Google Play secure developers policies.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                item {
                    Text("3. Third-Party Integrations", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "Sparkex AI embeds direct API proxies to run state-of-the-art models including gemini-3.5-flash, gemini-3.1-pro-preview, gemini-3-pro-image-preview, and Veo visual engines. These requests are governed directly by Google's API Privacy policies.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                item {
                    Text("1. Acceptance of Terms", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "By creating an account, running research prompts, or generating graphic creations inside Sparkex AI, you agree to comply with our modern user usage agreements. If you do not accept these criteria, please terminate use immediately.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                item {
                    Text("2. Responsible AI Usage", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "You agree to use Sparkex AI's reasoning tools exclusively for lawful activities. You may not bypass API restrictions, trigger harmful or malicious prompts, scrape visual assets, or attempt reverse engineering on the local databases.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                item {
                    Text("3. Proprietary Media Ownership", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "Images generated using gemini-3-pro-image-preview or storyboards built using Veo visual pipelines are fully owned by you, subject to Google's underlying model terms of use.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
