package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class HelpSection(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val steps: List<String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    onBack: () -> Unit
) {
    val helpSections = listOf(
        HelpSection(
            title = "1. Chatting & Multi-turn Context",
            description = "Learn how to hold dynamic conversations with Sparkex AI.",
            icon = Icons.Outlined.ChatBubbleOutline,
            steps = listOf(
                "Select 'New Chat' from the left sidebar or start a fresh session at any time.",
                "Type your query in the bottom input bar and tap the Send icon.",
                "Sparkex AI retains history across multiple messages, allowing you to ask follow-up questions.",
                "To start fresh without clogging your context, tap the 'Temporary Chat' shortcut at the top-right."
            )
        ),
        HelpSection(
            title = "2. Deep Research Mode (High Thinking)",
            description = "Activate deep reasoning using advanced analytical models.",
            icon = Icons.Outlined.Search,
            steps = listOf(
                "In the bottom input bar, tap the '+' Plus button.",
                "Select 'Toggle Deep Research'. A banner indicator will confirm the mode is active.",
                "Ask complex mathematical, scientific, or logical queries.",
                "Sparkex AI will detail its complete thinking process before supplying the final summary."
            )
        ),
        HelpSection(
            title = "3. Interactive Audio Dictation",
            description = "Dictate your prompts and play back speech natively.",
            icon = Icons.Outlined.Mic,
            steps = listOf(
                "Tap the Voice (Microphone) icon in the input bar to transcribe audio dictation directly.",
                "Once transcribed, press the Send button to deliver your query.",
                "When Sparkex AI responds, tap the 'Speak reply' audio widget in its message bubble to play synthesized voice playback."
            )
        ),
        HelpSection(
            title = "4. Real-time Voice Conversations",
            description = "Hold a real-time speech conversation with low latency.",
            icon = Icons.Outlined.GraphicEq,
            steps = listOf(
                "On the bottom input bar, tap the 'Live AI' wave button.",
                "An elegant recording session modal with real-time audio waveforms will appear.",
                "Speak clearly. Once you finish, Sparkex AI transcribes and responds with synchronized streaming playback.",
                "Tap the Red Call-End button to close the session."
            )
        ),
        HelpSection(
            title = "5. Image Understanding (Multimodal)",
            description = "Ask questions about physical objects or documents using images.",
            icon = Icons.Outlined.CameraAlt,
            steps = listOf(
                "In the input bar, tap the '+' Plus button and select Camera or Gallery.",
                "Attach or snap a photo of any document, equation, error screen, or product.",
                "Ask your question in the text bar and send. Sparkex AI will analyze the picture and answer contextually."
            )
        )
    )

    Scaffold(
        containerColor = Color(0xFFF7F7F8), // Soft off-white
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Help & Guides", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color.Black) },
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
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text(
                        text = "Welcome to Sparkex AI!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = "This application is built with pristine, zero-distraction layout principles. Follow this step-by-step breakdown on how to leverage our high-performance assistance system:",
                        fontSize = 14.sp,
                        color = Color(0xFF6E6E73),
                        lineHeight = 20.sp
                    )
                }
            }

            items(helpSections) { section ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFE5E5EA))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF7F7F8))
                                    .border(1.dp, Color(0xFFE5E5EA), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = section.icon,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(20.dp) // Sized consistently
                                )
                            }
                            Column {
                                Text(
                                    text = section.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Text(
                                    text = section.description,
                                    fontSize = 12.sp,
                                    color = Color(0xFF6E6E73)
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            section.steps.forEachIndexed { index, step ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                            .border(1.dp, Color(0xFFE5E5EA), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${index + 1}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black
                                        )
                                    }
                                    Text(
                                        text = step,
                                        fontSize = 13.sp,
                                        color = Color.Black,
                                        lineHeight = 18.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
