import re

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "r") as f:
    text = f.read()

old_bubble = """@Composable
fun ChatMessageBubble(
    message: ChatMessage,
    isPlaying: Boolean,
    onPlayVoice: () -> Unit
) {
    val isUser = message.role == "user"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.Transparent),
                contentAlignment = Alignment.TopCenter
            ) {
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFF4285F4),
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
        }

        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            // Optional image rendering
            if (message.imagePath != null) {
                Box(
                    modifier = Modifier
                        .padding(bottom = 6.dp)
                        .width(200.dp)
                        .height(150.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    if (message.imagePath.startsWith("/simulated/")) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFFE5E5EA)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = Color.Black
                            )
                        }
                    } else {
                        AsyncImage(
                            model = File(message.imagePath),
                            contentDescription = "Uploaded Snapshot",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // Text Bubble Container
            Box(
                modifier = Modifier
                    .clip(
                        if (isUser) RoundedCornerShape(24.dp)
                        else RoundedCornerShape(8.dp)
                    )
                    .background(if (isUser) Color(0xFFF0F4F9) else Color.Transparent)
                    .padding(
                        horizontal = if (isUser) 16.dp else 4.dp, 
                        vertical = if (isUser) 12.dp else 4.dp
                    )
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    MarkdownText(
                        text = message.text,
                        color = Color(0xFF1C1C1E)
                    )
                    // Optional audio dictation widget
                    if (!isUser && message.voicePath != null) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF0F4F9))
                                .clickable { onPlayVoice() }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Outlined.VolumeMute else Icons.Outlined.VolumeUp,
                                contentDescription = "Play Audio",
                                tint = Color(0xFF1C1C1E),
                                modifier = Modifier.size(16.dp)
                            )
                            if (isPlaying) {
                                AudioWaveformIndicator(isAnimating = true, color = Color(0xFF1C1C1E), modifier = Modifier.height(12.dp))
                            } else {
                                Text("Speak reply", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1C1C1E))
                            }
                        }
                    }
                }
            }

            // Action row for AI
            if (!isUser) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                ) {
                    Icon(imageVector = Icons.Outlined.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(18.dp), tint = Color(0xFF8E8E93))
                    Icon(imageVector = Icons.Outlined.ThumbUp, contentDescription = "Good response", modifier = Modifier.size(18.dp), tint = Color(0xFF8E8E93))
                    Icon(imageVector = Icons.Outlined.ThumbDown, contentDescription = "Bad response", modifier = Modifier.size(18.dp), tint = Color(0xFF8E8E93))
                    Icon(imageVector = Icons.Outlined.Refresh, contentDescription = "Regenerate", modifier = Modifier.size(18.dp), tint = Color(0xFF8E8E93))
                    Icon(imageVector = Icons.Outlined.Share, contentDescription = "Share", modifier = Modifier.size(18.dp), tint = Color(0xFF8E8E93))
                }
            }
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE5E5EA)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "Edit Prompt",
                    tint = Color(0xFF8E8E93),
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { /* Handle Edit Prompt */ }
                )
            }
        }
    }
}"""

new_bubble = """@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ChatMessageBubble(
    message: ChatMessage,
    isPlaying: Boolean,
    onPlayVoice: () -> Unit
) {
    val isUser = message.role == "user"
    var showActions by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color.Transparent),
                contentAlignment = Alignment.TopCenter
            ) {
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFF4285F4),
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
        }

        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            // Optional image rendering
            if (message.imagePath != null) {
                Box(
                    modifier = Modifier
                        .padding(bottom = 6.dp)
                        .width(200.dp)
                        .height(150.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    if (message.imagePath.startsWith("/simulated/")) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFFE5E5EA)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = Color.Black
                            )
                        }
                    } else {
                        AsyncImage(
                            model = File(message.imagePath),
                            contentDescription = "Uploaded Snapshot",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // Text Bubble Container
            Box(
                modifier = Modifier
                    .clip(
                        if (isUser) RoundedCornerShape(24.dp)
                        else RoundedCornerShape(8.dp)
                    )
                    .background(if (isUser) Color(0xFFF0F4F9) else Color.Transparent)
                    .combinedClickable(
                        onClick = {},
                        onLongClick = { showActions = !showActions }
                    )
                    .padding(
                        horizontal = if (isUser) 16.dp else 4.dp, 
                        vertical = if (isUser) 14.dp else 8.dp
                    )
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    MarkdownText(
                        text = message.text,
                        color = Color(0xFF1C1C1E)
                    )
                    // Optional audio dictation widget
                    if (!isUser && message.voicePath != null) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF0F4F9))
                                .clickable { onPlayVoice() }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Outlined.VolumeMute else Icons.Outlined.VolumeUp,
                                contentDescription = "Play Audio",
                                tint = Color(0xFF1C1C1E),
                                modifier = Modifier.size(16.dp)
                            )
                            if (isPlaying) {
                                AudioWaveformIndicator(isAnimating = true, color = Color(0xFF1C1C1E), modifier = Modifier.height(12.dp))
                            } else {
                                Text("Speak reply", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1C1C1E))
                            }
                        }
                    }
                }
            }

            // Action row for AI
            if (!isUser && showActions) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                ) {
                    Icon(imageVector = Icons.Outlined.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(18.dp), tint = Color(0xFF8E8E93))
                    Icon(imageVector = Icons.Outlined.ThumbUp, contentDescription = "Good response", modifier = Modifier.size(18.dp), tint = Color(0xFF8E8E93))
                    Icon(imageVector = Icons.Outlined.ThumbDown, contentDescription = "Bad response", modifier = Modifier.size(18.dp), tint = Color(0xFF8E8E93))
                    Icon(imageVector = Icons.Outlined.Refresh, contentDescription = "Regenerate", modifier = Modifier.size(18.dp), tint = Color(0xFF8E8E93))
                    Icon(imageVector = Icons.Outlined.Share, contentDescription = "Share", modifier = Modifier.size(18.dp), tint = Color(0xFF8E8E93))
                }
            }
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE5E5EA)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }

                if (showActions) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Edit Prompt",
                        tint = Color(0xFF8E8E93),
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { /* Handle Edit Prompt */ }
                    )
                }
            }
        }
    }
}"""

if old_bubble in text:
    text = text.replace(old_bubble, new_bubble)
    with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "w") as f:
        f.write(text)
    print("Bubble successfully updated.")
else:
    print("Old bubble not found. Attempting regex or partial replace.")
