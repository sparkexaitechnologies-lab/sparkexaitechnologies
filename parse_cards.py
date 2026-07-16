import re

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "r") as f:
    content = f.read()

# Insert extraction logic inside text bubble column before MarkdownText
search_block = r'''                    Column\(verticalArrangement = Arrangement\.spacedBy\(12\.dp\)\) \{
                        MarkdownText\(
                            text = message\.text,
                            color = if \(isUser\) MaterialTheme\.colorScheme\.onPrimary else MaterialTheme\.colorScheme\.onSurface
                        \)'''

replacement_block = r'''                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        val fullText = message.text
                        
                        val imageRequestMatch = Regex("\\[IMAGE_REQUEST: (.*?)\\]").find(fullText)
                        val mapRequestMatch = Regex("\\[MAP_REQUEST: (.*?)\\]").find(fullText)
                        val cardRequestMatch = Regex("\\[CARD_REQUEST: (.*?)\\]").find(fullText)
                        
                        var cleanText = fullText
                        imageRequestMatch?.let { cleanText = cleanText.replace(it.value, "") }
                        mapRequestMatch?.let { cleanText = cleanText.replace(it.value, "") }
                        cardRequestMatch?.let { cleanText = cleanText.replace(it.value, "") }
                        cleanText = cleanText.trim()

                        if (cleanText.isNotEmpty()) {
                            MarkdownText(
                                text = cleanText,
                                color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Render extracted widgets
                        if (!isUser) {
                            imageRequestMatch?.let {
                                val description = it.groupValues[1]
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Image,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Text(
                                        text = "Generating: $description",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.align(Alignment.BottomCenter).padding(8.dp)
                                    )
                                }
                            }

                            mapRequestMatch?.let {
                                val location = it.groupValues[1]
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(240.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFF1E293B)), // Dark slate for map style
                                    contentAlignment = Alignment.Center
                                ) {
                                    // A mockup of a map
                                    Icon(
                                        imageVector = Icons.Outlined.Map,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.2f),
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black.copy(alpha = 0.7f))
                                            .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("🏛️", fontSize = 16.sp)
                                    }
                                    Text(
                                        text = location,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        modifier = Modifier.align(Alignment.BottomStart).padding(12.dp)
                                    )
                                    
                                    // Fullscreen icon top right
                                    Icon(
                                        imageVector = Icons.Outlined.Fullscreen,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.7f),
                                        modifier = Modifier.align(Alignment.TopEnd).padding(12.dp).size(20.dp)
                                    )
                                }
                            }

                            cardRequestMatch?.let {
                                val parts = it.groupValues[1].split("|").map { s -> s.trim() }
                                val taskName = parts.getOrNull(0) ?: "Task"
                                val taskTime = parts.getOrNull(1) ?: "Daily"
                                val taskInstructions = parts.getOrNull(2) ?: ""

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .padding(16.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = taskName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = taskTime,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Instructions",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = taskInstructions,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "See more",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                                            )
                                            
                                            // Run now button
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(24.dp))
                                                    .background(MaterialTheme.colorScheme.primary)
                                                    .clickable {  }
                                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                            ) {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "Run now",
                                                        style = MaterialTheme.typography.labelMedium,
                                                        color = MaterialTheme.colorScheme.onPrimary,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                    Icon(
                                                        imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.onPrimary,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }'''

content = re.sub(search_block, replacement_block, content)

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "w") as f:
    f.write(content)

