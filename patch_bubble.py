import re

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "r") as f:
    text = f.read()

# Add context and clipboard manager to ChatMessageBubble
bubble_signature_old = """@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ChatMessageBubble(
    message: ChatMessage,
    isPlaying: Boolean,
    onPlayVoice: () -> Unit
) {
    val isUser = message.role == "user"
    var showActions by remember { mutableStateOf(false) }"""

bubble_signature_new = """@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ChatMessageBubble(
    message: ChatMessage,
    isPlaying: Boolean,
    onPlayVoice: () -> Unit,
    onRegenerate: () -> Unit = {}
) {
    val isUser = message.role == "user"
    var showActions by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    var isLiked by remember { mutableStateOf(false) }
    var isDisliked by remember { mutableStateOf(false) }"""

if bubble_signature_old in text:
    text = text.replace(bubble_signature_old, bubble_signature_new)
    print("Patched bubble signature")
else:
    print("Could not find bubble signature")

# Replace Action row for AI
action_row_old = """            // Action row for AI
            if (!isUser && showActions) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                ) {
                    Icon(imageVector = Icons.Outlined.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Icon(imageVector = Icons.Outlined.ThumbUp, contentDescription = "Good response", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Icon(imageVector = Icons.Outlined.ThumbDown, contentDescription = "Bad response", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Icon(imageVector = Icons.Outlined.Refresh, contentDescription = "Regenerate", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Icon(imageVector = Icons.Outlined.Share, contentDescription = "Share", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }"""

action_row_new = """            // Action row for AI
            if (!isUser) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 4.dp)
                ) {
                    // Like
                    Icon(
                        imageVector = if (isLiked) Icons.Outlined.ThumbUp else Icons.Outlined.ThumbUp,
                        contentDescription = "Good response",
                        modifier = Modifier
                            .size(18.dp)
                            .clickable {
                                isLiked = !isLiked
                                if (isLiked) isDisliked = false
                            },
                        tint = if (isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    // Dislike
                    Icon(
                        imageVector = if (isDisliked) Icons.Outlined.ThumbDown else Icons.Outlined.ThumbDown,
                        contentDescription = "Bad response",
                        modifier = Modifier
                            .size(18.dp)
                            .clickable {
                                isDisliked = !isDisliked
                                if (isDisliked) isLiked = false
                            },
                        tint = if (isDisliked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    // Regenerate
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = "Regenerate",
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { onRegenerate() },
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    // Share
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = "Share",
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { Toast.makeText(context, "Share action", Toast.LENGTH_SHORT).show() },
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    // Copy
                    Icon(
                        imageVector = Icons.Outlined.ContentCopy,
                        contentDescription = "Copy",
                        modifier = Modifier
                            .size(18.dp)
                            .clickable {
                                clipboardManager.setText(androidx.compose.ui.text.buildAnnotatedString { append(message.text) })
                                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    // More Options
                    Icon(
                        imageVector = Icons.Outlined.MoreHoriz,
                        contentDescription = "More options",
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { Toast.makeText(context, "More options", Toast.LENGTH_SHORT).show() },
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }"""

if action_row_old in text:
    text = text.replace(action_row_old, action_row_new)
    print("Patched action row")
else:
    print("Could not find Action row")

# Pass down onRegenerate to ChatMessageBubble
# We need to find where ChatMessageBubble is called and add onRegenerate = { viewModel.sendTextMessage("Regenerate last response", attachedImagePath = null, attachedVoicePath = null) }
# Wait, it's called twice. Let's patch both.

call1_old = """                                ChatMessageBubble(
                                    message = message,
                                    isPlaying = message.id == currentlyPlayingMessageId,
                                    onPlayVoice = { viewModel.playMessageVoice(message) }
                                )"""

call1_new = """                                ChatMessageBubble(
                                    message = message,
                                    isPlaying = message.id == currentlyPlayingMessageId,
                                    onPlayVoice = { viewModel.playMessageVoice(message) },
                                    onRegenerate = {
                                        if (activePlan == "Sparkex Free" && freeUses >= 10) {
                                            showLimitModal = true
                                        } else {
                                            if (activePlan == "Sparkex Free") {
                                                val newCount = freeUses + 1
                                                freeUses = newCount
                                                prefs.edit().putInt("free_uses", newCount).apply()
                                            }
                                            viewModel.sendTextMessage("Regenerate the last response")
                                        }
                                    }
                                )"""

call2_old = """                                        ChatMessageBubble(
                                            message = streamMsg,
                                            isPlaying = false,
                                            onPlayVoice = {}
                                        )"""

call2_new = """                                        ChatMessageBubble(
                                            message = streamMsg,
                                            isPlaying = false,
                                            onPlayVoice = {},
                                            onRegenerate = {}
                                        )"""

if call1_old in text:
    text = text.replace(call1_old, call1_new)
    print("Patched call 1")
else:
    print("Could not find call 1")

if call2_old in text:
    text = text.replace(call2_old, call2_new)
    print("Patched call 2")
else:
    print("Could not find call 2")

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "w") as f:
    f.write(text)
