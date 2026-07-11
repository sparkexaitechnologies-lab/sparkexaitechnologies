package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
        containerColor = Color(0xFFF7F7F8), // Soft off-white
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFF7F7F8)
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp)
        ) {
            item {
                Text(
                    text = "Last Updated: July 2026",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6E6E73)
                )
            }

            if (isPrivacy) {
                item {
                    LegalSectionCard(
                        number = "1",
                        title = "Information We Collect",
                        content = "Sparkex AI operates under enterprise-grade offline synchronizations. All chat logs, settings profiles, and uploaded visual materials are securely stored on your local SQLite storage through encrypted schema tables. When calling our Gemini LLM APIs, prompt texts and image buffers are sent securely using TLS encryption protocols directly to Google's backend processing hosts."
                    )
                }

                item {
                    LegalSectionCard(
                        number = "2",
                        title = "Enterprise-Grade Security & Encryption",
                        content = "Our databases are protected with dynamic structural isolation boundaries. We never store, extract, or sell user personal communications. Generated media buffers are preserved exclusively in the app's sandboxed local cache files. We strictly follow Google Play secure developers policies."
                    )
                }

                item {
                    LegalSectionCard(
                        number = "3",
                        title = "Third-Party Integrations",
                        content = "Sparkex AI embeds direct API proxies to run state-of-the-art models including gemini-3.5-flash, gemini-3.1-pro-preview, gemini-3-pro-image-preview, and Veo visual engines. These requests are governed directly by Google's API Privacy policies."
                    )
                }
            } else {
                item {
                    LegalSectionCard(
                        number = "1",
                        title = "Acceptance of Terms",
                        content = "By creating an account, running research prompts, or generating graphic creations inside Sparkex AI, you agree to comply with our modern user usage agreements. If you do not accept these criteria, please terminate use immediately."
                    )
                }

                item {
                    LegalSectionCard(
                        number = "2",
                        title = "Responsible AI Usage",
                        content = "You agree to use Sparkex AI's reasoning tools exclusively for lawful activities. You may not bypass API restrictions, trigger harmful or malicious prompts, scrape visual assets, or attempt reverse engineering on the local databases."
                    )
                }

                item {
                    LegalSectionCard(
                        number = "3",
                        title = "Proprietary Media Ownership",
                        content = "Images generated using gemini-3-pro-image-preview or storyboards built using Veo visual pipelines are fully owned by you, subject to Google's underlying model terms of use."
                    )
                }
            }
        }
    }
}

@Composable
fun LegalSectionCard(
    number: String,
    title: String,
    content: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFE5E5EA))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(Color(0xFFF7F7F8), RoundedCornerShape(6.dp))
                        .border(1.dp, Color(0xFFE5E5EA), RoundedCornerShape(6.dp)),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text(
                        text = number,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            Text(
                text = content,
                fontSize = 13.sp,
                color = Color.Black,
                lineHeight = 19.sp
            )
        }
    }
}
