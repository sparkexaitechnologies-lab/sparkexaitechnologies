package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.speech.RecognizerIntent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.ui.res.stringResource
import com.example.R
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.ChatMessage
import com.example.ui.viewmodel.SparkexViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun CosmicBackground(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "cosmic")
    val animProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "anim"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val color1 = Color(0xFF0C0C0E)
        val color2 = Color(0xFF030303)
        val color3 = Color(0xFF141417)
        
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(color1, color2, color3),
                center = Offset(size.width * animProgress, size.height * (1f - animProgress)),
                radius = size.maxDimension * 1.5f
            )
        )
        
        // Subtle stardust
        repeat(30) { i ->
            val x = (i * 12345.67f) % size.width
            val y = (i * 98765.43f) % size.height
            drawCircle(
                color = Color.White.copy(alpha = 0.05f),
                radius = 1.dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun ChatScreen(
    viewModel: SparkexViewModel,
    onNavigateToSidebar: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToImages: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onNavigateToVideoCreator: () -> Unit,
    onNavigateToMemberships: () -> Unit
) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    
    val prefs = context.getSharedPreferences("sparkex_prefs", android.content.Context.MODE_PRIVATE)
    var showPlanModal by remember { mutableStateOf<String?>(null) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var renameInputText by remember { mutableStateOf("") }

    val sessions by viewModel.sessions.collectAsState()
    val pinnedSessionIds by viewModel.pinnedSessionIds.collectAsState()
    val activeSessionId by viewModel.activeSessionId.collectAsState()
    val messages by viewModel.activeMessages.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val deepResearchEnabled by viewModel.deepResearchEnabled.collectAsState()
    val profile by viewModel.userProfile.collectAsState()
    val currentlyPlayingMessageId by viewModel.currentlyPlayingMessageId.collectAsState()
    val isSpeakingTts by viewModel.isSpeakingTts.collectAsState()
    val currentStreamingMessage by viewModel.currentStreamingMessage.collectAsState()

    var textInput by remember { mutableStateOf("") }
    var attachedImagePath by remember { mutableStateOf<String?>(null) }
    var attachedVoicePath by remember { mutableStateOf<String?>(null) }

    var showAttachmentMenu by remember { mutableStateOf(false) }
    var showLiveVoiceModal by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val data = result.data
            val matches = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!matches.isNullOrEmpty()) {
                textInput = matches[0]
            }
        }
    }

    val liveVoiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val data = result.data
            val matches = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!matches.isNullOrEmpty()) {
                viewModel.sendTextMessage(text = matches[0], attachedImagePath = null, attachedVoicePath = null)
            }
        }
        showLiveVoiceModal = false
    }


    // Auto scroll to latest reply with optimized performance to prevent UI jank
    LaunchedEffect(messages.size, isGenerating) {
        val hasStreaming = currentStreamingMessage != null
        val hasSkeleton = isGenerating && !hasStreaming
        val totalItems = messages.size + (if (hasStreaming) 1 else 0) + (if (hasSkeleton) 1 else 0)
        if (totalItems > 0) {
            // Animate only for major list changes (new messages)
            listState.animateScrollToItem(totalItems - 1)
        }
    }

    // Faster non-animated scroll for streaming text updates
    LaunchedEffect(currentStreamingMessage?.text) {
        if (currentStreamingMessage != null) {
            val totalItems = messages.size + 1
            if (totalItems > 0) {
                listState.scrollToItem(totalItems - 1)
            }
        }
    }

    val greetings = listOf(
        stringResource(R.string.greeting_1),
        stringResource(R.string.greeting_2),
        stringResource(R.string.greeting_3),
        stringResource(R.string.greeting_4),
        stringResource(R.string.greeting_5)
    )
    val randomGreeting = remember { greetings.random() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background, // Soft off-white background
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding() // Proper edge-to-edge status bar handling
                    .padding(bottom = 8.dp, start = 16.dp, end = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left: Hamburger menu
                    IconButton(onClick = onNavigateToSidebar) {
                        Icon(imageVector = Icons.Outlined.Menu, contentDescription = "Menu", tint = MaterialTheme.colorScheme.onSurface)
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Right: New Chat icon
                    IconButton(onClick = { viewModel.startNewSession() }) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "New Chat",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding())
            ) {
                

                Column(modifier = Modifier.fillMaxSize()) {
                    // Chat Screen Main Area
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        // Conversations Scrolling Area
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            if (messages.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillParentMaxSize()
                                            .padding(bottom = 60.dp), // Adjust for input bar
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = randomGreeting,
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(horizontal = 32.dp)
                                        )
                                    }
                                }
                            }

                            items(messages, key = { it.id }) { message ->
                                    val isError = !message.role.equals("user", ignoreCase = true) && (message.text.startsWith("Error:") || message.text.contains("API Error", ignoreCase = true) || message.text.contains("Error", ignoreCase = true))
                                    val onRetryAction = if (isError) {
                                        {
                                            val index = messages.indexOf(message)
                                            if (index > 0) {
                                                val prevMessage = messages[index - 1]
                                                if (prevMessage.role == "user") {
                                                    viewModel.deleteMessageById(message.id)
                                                    viewModel.sendTextMessage(
                                                        text = prevMessage.text,
                                                        attachedImagePath = prevMessage.imagePath,
                                                        attachedVoicePath = prevMessage.voicePath
                                                    )
                                                }
                                            }
                                        }
                                    } else null

                                    ChatMessageBubble(
                                        message = message,
                                        isPlaying = message.id == currentlyPlayingMessageId,
                                        onPlayVoice = { viewModel.playMessageVoice(message) },
                                        isSpeakingTts = message.id == isSpeakingTts,
                                        onToggleTtsSpeech = { viewModel.toggleTtsSpeaking(message) },
                                        onRegenerate = {
                                            viewModel.sendTextMessage("Regenerate the last response")
                                        },
                                        onRetry = onRetryAction,
                                        lowLatency = profile.lowLatencyEnabled
                                    )
                                }
                                
                                currentStreamingMessage?.let { streamMsg ->
                                    item(key = "streaming") {
                                        ChatMessageBubble(
                                            message = streamMsg,
                                            isPlaying = false,
                                            onPlayVoice = {},
                                            onRegenerate = {},
                                            isGenerating = true,
                                            lowLatency = profile.lowLatencyEnabled
                                        )
                                    }
                                }

                                if (isGenerating && currentStreamingMessage == null) {
                                    item(key = "loading_indicator") {
                                        ChatMessageBubble(
                                            message = ChatMessage(
                                                id = 999999L,
                                                sessionId = activeSessionId ?: "",
                                                role = "model",
                                                text = "",
                                                timestamp = System.currentTimeMillis()
                                            ),
                                            isPlaying = false,
                                            onPlayVoice = {},
                                            isGenerating = true,
                                            lowLatency = profile.lowLatencyEnabled
                                        )
                                    }
                                }
                            }
                    }

                    // Deep Research Banner Indicator
                    if (attachedImagePath != null || attachedVoicePath != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, MaterialTheme.colorScheme.outline)
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            attachedImagePath?.let { path ->
                                Box(modifier = Modifier.size(52.dp)) {
                                    if (path.startsWith("/simulated/")) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(RoundedCornerShape(18.dp))
                                                .background(MaterialTheme.colorScheme.outline),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Image,
                                                contentDescription = "Simulated Image",
                                                modifier = Modifier.size(24.dp),
                                                tint = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    } else {
                                        AsyncImage(
                                            model = File(path),
                                            contentDescription = "Attached Photo Preview",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.clip(RoundedCornerShape(18.dp))
                                        )
                                    }
                                    IconButton(
                                        onClick = { attachedImagePath = null },
                                        modifier = Modifier
                                            .size(20.dp)
                                            .align(Alignment.TopEnd)
                                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Close,
                                            contentDescription = "Remove",
                                            tint = MaterialTheme.colorScheme.surface,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }

                            attachedVoicePath?.let { path ->
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(MaterialTheme.colorScheme.background)
                                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Mic,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text("Voice snippet ready", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                    IconButton(
                                        onClick = { attachedVoicePath = null },
                                        modifier = Modifier.size(16.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Close,
                                            contentDescription = "Remove Audio",
                                            modifier = Modifier.size(12.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }



                    // Premium redesigned input bar (ChatGPT Style)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .imePadding() // Moves above keyboard
                            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp) // 16dp above bottom
                            .height(56.dp) // Taller for premium feel
                            .clip(RoundedCornerShape(28.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(28.dp))
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Add Button
                        IconButton(
                            onClick = { showAttachmentMenu = !showAttachmentMenu },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = "Add",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Minimal TextField
                        TextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            placeholder = {
                                Text(
                                    text = "Ask Sparkex...",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    fontSize = 15.sp
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = MaterialTheme.colorScheme.primary
                            ),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp)
                        )

                        // Right: Actions
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            // Live Talk Button (Headset)
                            IconButton(
                                onClick = { 
                                    Toast.makeText(context, "Live Voice Mode (ChatGPT style) coming soon", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Headset,
                                    contentDescription = "Live Talk",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            // Microphone Button
                            val speechHelper = remember { com.example.util.SpeechHelper(context) }
                            var isListening by remember { mutableStateOf(false) }
                            val voicePermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { if (it) { isListening = true; speechHelper.startListening() } }
                            
                            DisposableEffect(Unit) {
                                speechHelper.onResult = { textInput = it; isListening = false }
                                speechHelper.onPartialResult = { textInput = it }
                                speechHelper.onError = { isListening = false }
                                onDispose { speechHelper.destroy() }
                            }

                            IconButton(
                                onClick = {
                                    if (isListening) { isListening = false; speechHelper.stopListening() }
                                    else voicePermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (isListening) Icons.Outlined.StopCircle else Icons.Outlined.Mic,
                                    contentDescription = "Voice",
                                    tint = if (isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            // Send Button (only if typing)
                            if (textInput.isNotBlank() || attachedImagePath != null) {
                                IconButton(
                                    onClick = {
                                        if (textInput.isNotBlank() || attachedImagePath != null || attachedVoicePath != null) {
                                            viewModel.sendTextMessage(textInput, attachedImagePath, attachedVoicePath)
                                            textInput = ""; attachedImagePath = null; attachedVoicePath = null
                                        }
                                    },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.ArrowUpward,
                                        contentDescription = "Send",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                }

                // Popup Attachment Menu above bar
                if (showAttachmentMenu) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(bottom = 76.dp, start = 16.dp)
                            .width(260.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            AttachmentMenuItem(icon = Icons.Outlined.CameraAlt, title = "Camera") {
                                attachedImagePath = "/simulated/camera_capture.jpg"
                                showAttachmentMenu = false
                            }
                            AttachmentMenuItem(icon = Icons.Outlined.Image, title = "Gallery") {
                                attachedImagePath = "/simulated/gallery_upload.jpg"
                                showAttachmentMenu = false
                            }
                            AttachmentMenuItem(icon = Icons.Outlined.Mic, title = "Voice") {
                                attachedVoicePath = "/simulated/voice_snippet.mp3"
                                showAttachmentMenu = false
                            }
                        }
                    }
                }

                // Plan Modal Overlay
                if (showPlanModal != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f))
                            .clickable { showPlanModal = null },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .clickable(enabled = false) {}
                                .padding(24.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Subscription Plan",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = showPlanModal ?: "",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                
                                val isPro = showPlanModal?.contains("Pro") == true
                                val features = if (isPro) {
                                    listOf(
                                        "Unlimited Gemini 1.5 Pro & Video",
                                        "Advanced Data Analysis",
                                        "Priority Server Access",
                                        "Early Access to new features"
                                    )
                                } else {
                                    listOf(
                                        "Unlimited Gemini 1.5 Flash",
                                        "Standard Response Speed",
                                        "Basic Data Analysis",
                                        "Access to standard models"
                                    )
                                }
                                
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    features.forEach { feature ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.CheckCircle,
                                                contentDescription = "Feature included",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text(
                                                text = feature,
                                                fontSize = 14.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }

                                Button(
                                    onClick = { showPlanModal = null },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text("Got it")
                                }
                            }
                        }
                    }
                }

                // Rename Chat Dialog
                if (showRenameDialog) {
                    AlertDialog(
                        onDismissRequest = { showRenameDialog = false },
                        title = { Text("Rename Chat Session") },
                        text = {
                            Column {
                                OutlinedTextField(
                                    value = renameInputText,
                                    onValueChange = { renameInputText = it },
                                    label = { Text("New Session Title") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    activeSessionId?.let { id ->
                                        if (renameInputText.isNotBlank()) {
                                            viewModel.renameSession(id, renameInputText)
                                        }
                                    }
                                    showRenameDialog = false
                                }
                            ) {
                                Text("Rename")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showRenameDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }

                // About App Dialog
                if (showAboutDialog) {
                    AlertDialog(
                        onDismissRequest = { showAboutDialog = false },
                        icon = { Icon(Icons.Outlined.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        title = { Text("About Sparkex AI") },
                        text = {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Sparkex AI",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Version 1.2.0",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "A premium, minimal, modern AI assistant interface crafted with elegant Material Design 3 and powered by state-of-the-art Google Gemini models.",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "© 2026 Sparkex AI",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showAboutDialog = false }) {
                                Text("Close")
                            }
                        }
                    )
                }

                // Interactive Live Voice Conversation overlay modal
                if (showLiveVoiceModal) {
                    LiveVoiceModalOverlay(
                        viewModel = viewModel,
                        onEnd = { showLiveVoiceModal = false },
                        onResult = { result ->
                                
                                viewModel.sendTextMessage(text = result, attachedImagePath = null, attachedVoicePath = null)
                                // Do NOT close modal, keep it open for true Live Talk
                            }
                    )
                }
            }
        }
    }

@Composable
fun CompactSuggestionChip(
    icon: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "chipScale"
    )

    Surface(
        onClick = onClick,
        modifier = modifier
            .height(52.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale),
        interactionSource = interactionSource,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = icon,
                fontSize = 18.sp,
                modifier = Modifier.padding(end = 8.dp)
            )
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 13.sp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun AILogo(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "logo_anim")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Canvas(modifier = modifier.size(size)) {
        val centerX = this.size.width / 2f
        val centerY = this.size.height / 2f
        val radius = this.size.minDimension / 2.5f

        rotate(rotation) {
            // Draw a diamond shape
            val path = Path().apply {
                moveTo(centerX, centerY - radius)
                lineTo(centerX + radius, centerY)
                lineTo(centerX, centerY + radius)
                lineTo(centerX - radius, centerY)
                close()
            }
            drawPath(path, color = color, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
            
            // Inner glowing dot
            drawCircle(
                color = color.copy(alpha = 0.3f),
                radius = radius / 2.5f,
                center = center
            )
            drawCircle(
                color = color,
                radius = radius / 6f,
                center = center
            )
        }
    }
}

@Composable
fun PremiumActionCard(
    icon: ImageVector,
    title: String,
    gradient: Brush,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "cardScale"
    )

    Box(
        modifier = modifier
            .height(96.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable(interactionSource = interactionSource, indication = androidx.compose.foundation.LocalIndication.current, onClick = onClick)
            .padding(1.dp) // border effect
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(gradient),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun ProfessionalErrorPage(
    message: String,
    onRetry: (() -> Unit)?,
    onChangeApi: () -> Unit,
    onReport: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = "Error Illustration",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
        )
        Text(
            text = "Something went wrong",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Sparkex AI encountered an issue. " + message.replace("Error: ", ""),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onChangeApi,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.onSurface),
                modifier = Modifier.weight(1f)
            ) {
                Text("Change API")
            }
            if (onRetry != null) {
                Button(
                    onClick = { onRetry.invoke() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Retry")
                }
            }
        }
        TextButton(onClick = onReport) {
            Text("Report Issue", color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun WordStreamingText(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    isStreaming: Boolean = false,
    lowLatency: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cursor")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursor"
    )
    val cursorSymbol = if (cursorAlpha > 0.5f && isStreaming) "▍" else ""

    if (!isStreaming || lowLatency) {
        MarkdownText(text = text, color = color, modifier = modifier)
        return
    }

    var displayedWords by remember { mutableStateOf(listOf<String>()) }
    
    LaunchedEffect(text) {
        val currentWords = text.split(" ")
        if (currentWords.size > displayedWords.size) {
            val wordsToAdd = currentWords.size - displayedWords.size
            for (i in 1..wordsToAdd) {
                displayedWords = currentWords.take(displayedWords.size + 1)
                delay(20) 
            }
        } else {
            displayedWords = currentWords
        }
    }

    val textWithCursor = remember(displayedWords, cursorSymbol) {
        val baseText = displayedWords.joinToString(" ")
        if (baseText.isNotEmpty()) "$baseText$cursorSymbol" else cursorSymbol
    }

    MarkdownText(
        text = textWithCursor,
        color = color,
        modifier = modifier
    )
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ChatMessageBubble(
    message: ChatMessage,
    isPlaying: Boolean,
    onPlayVoice: () -> Unit,
    onRegenerate: () -> Unit = {},
    onRetry: (() -> Unit)? = null,
    isSpeakingTts: Boolean = false,
    onToggleTtsSpeech: () -> Unit = {},
    isGenerating: Boolean = false,
    lowLatency: Boolean = false
) {
    val isUser = message.role == "user"
    var showActions by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    var isLiked by remember { mutableStateOf(false) }
    var isDisliked by remember { mutableStateOf(false) }

    val entryAlpha = remember { Animatable(if (lowLatency) 1f else 0f) }
    val entrySlide = remember { Animatable(if (lowLatency) 0f else 16.dp.value) }
    
    if (!lowLatency) {
        LaunchedEffect(Unit) {
            launch {
                entryAlpha.animateTo(1f, animationSpec = spring(stiffness = Spring.StiffnessLow))
            }
            launch {
                entrySlide.animateTo(0f, animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow))
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .graphicsLayer {
                alpha = entryAlpha.value
                translationY = entrySlide.value
            },
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {


        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            // Optional image rendering
            if (message.imagePath != null) {
                Box(
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .width(260.dp)
                        .height(180.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp))
                ) {
                    if (message.imagePath.startsWith("/simulated/")) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        AsyncImage(
                            model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                                .data(File(message.imagePath))
                                .crossfade(true)
                                .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                                .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                                .build(),
                            contentDescription = "Snapshot",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // Thinking Process Section
            if (!isUser && !message.thinkingProcess.isNullOrEmpty()) {
                var expandedThinking by remember { mutableStateOf(false) }
                Column(
                    modifier = Modifier
                        .padding(bottom = 12.dp, start = 4.dp)
                        .fillMaxWidth(0.9f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .clickable { expandedThinking = !expandedThinking }
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Lightbulb,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (expandedThinking) "Thought process" else "View thought process",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            imageVector = if (expandedThinking) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (expandedThinking) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = message.thinkingProcess,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            val isError = !isUser && (message.text.startsWith("Error:") || message.text.contains("API Error", ignoreCase = true) || message.text.contains("Error", ignoreCase = true))

            if (isError) {
                ProfessionalErrorPage(
                    message = message.text,
                    onRetry = onRetry,
                    onChangeApi = { Toast.makeText(context, "Use the Top Bar to change models", Toast.LENGTH_SHORT).show() },
                    onReport = { Toast.makeText(context, "Issue reported to Sparkex AI Engineering", Toast.LENGTH_SHORT).show() }
                )
            } else {
                // Text Bubble Container
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                if (isUser) {
                                    androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant
                                } else {
                                    androidx.compose.material3.MaterialTheme.colorScheme.surface
                                }
                            )
                            .combinedClickable(
                                onClick = {},
                                onLongClick = { showActions = !showActions }
                            )
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                                WordStreamingText(
                                    text = cleanText,
                                    isStreaming = isGenerating,
                                    lowLatency = lowLatency,
                                    color = if (isUser) {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            } else if (!isUser) {
                                ElegantLoadingIndicator()
                            }
    
                            // Render extracted widgets
                        if (!isUser) {
                            imageRequestMatch?.let {
                                val description = it.groupValues[1]
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Image,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
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
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant), // Dark slate for map style
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
                                        .clip(RoundedCornerShape(18.dp))
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
                                                    .clip(RoundedCornerShape(18.dp))
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
                                                        imageVector = Icons.Outlined.ArrowForward,
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
                        }
                        
                        // Action Chips (Mimicking the image)
                        if (!isUser) {
                            val lowerText = message.text.lowercase()
                            val chips = mutableListOf<Pair<ImageVector, String>>()
                            
                            if (lowerText.contains("reminder") || lowerText.contains("date") || lowerText.contains("schedule")) {
                                chips.add(Icons.Outlined.CalendarToday to "Reminder")
                            }
                            if (lowerText.contains("link") || lowerText.contains("http") || lowerText.contains("deposit")) {
                                chips.add(Icons.Outlined.Link to "Link")
                            }
                            if (lowerText.contains("event") || lowerText.contains("market") || lowerText.contains("meeting")) {
                                chips.add(Icons.Outlined.Event to "View event")
                            }
                            
                            if (chips.isNotEmpty()) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    chips.forEach { (icon, label) ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier
                                                .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp))
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Optional audio dictation widget
                        if (!isUser && message.voicePath != null) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { onPlayVoice() }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Outlined.VolumeMute else Icons.Outlined.VolumeUp,
                                contentDescription = "Play Audio",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(16.dp)
                            )
                            if (isPlaying) {
                                AudioWaveformIndicator(isAnimating = true, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.height(12.dp))
                            } else {
                                Text("Speak reply", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                    }

                    if (!isUser) {
                        DropdownMenu(
                            expanded = showActions,
                            onDismissRequest = { showActions = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Copy Text") },
                                onClick = {
                                    clipboardManager.setText(androidx.compose.ui.text.buildAnnotatedString { append(message.text) })
                                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                    showActions = false
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.ContentCopy,
                                        contentDescription = "Copy Text",
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                modifier = Modifier.testTag("copy_text_menu_item")
                            )
                        }
                    }
                }
            }
            }

            // Action row for AI
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
                    // Speak (TTS togglable speaker icon)
                    Icon(
                        imageVector = if (isSpeakingTts) Icons.Outlined.VolumeOff else Icons.Outlined.VolumeUp,
                        contentDescription = "Read aloud",
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { onToggleTtsSpeech() }
                            .testTag("speaker_tts_icon"),
                        tint = if (isSpeakingTts) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
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
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "AI can make mistakes. Verify important info.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    fontSize = 10.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (showActions) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Edit Prompt",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { /* Handle Edit Prompt */ }
                    )
                }
            }
        }
    }
}
@Composable
fun AttachmentMenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun ModernWaveform(
    state: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = modifier
            .size(300.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val baseRadius = size.width * 0.25f
            
            val amplitude = when(state) {
                "Listening" -> 15.dp.toPx()
                "Speaking" -> 35.dp.toPx()
                "Thinking" -> 10.dp.toPx()
                else -> 5.dp.toPx()
            }

            val layers = 3
            for (layer in 1..layers) {
                val path = Path()
                val points = 100
                val layerPhase = phase * (1.2f / layer)
                
                for (i in 0..points) {
                    val angle = (i.toFloat() / points) * 2f * Math.PI.toFloat()
                    val wave = Math.sin(angle.toDouble() * (2 + layer) + layerPhase.toDouble()).toFloat() * amplitude
                    val r = baseRadius + wave
                    
                    val x = center.x + r * Math.cos(angle.toDouble()).toFloat()
                    val y = center.y + r * Math.sin(angle.toDouble()).toFloat()
                    
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                path.close()
                
                val color = when(state) {
                    "Listening" -> Color.White
                    "Speaking" -> Color.White
                    "Thinking" -> Color.LightGray
                    else -> Color.Gray
                }

                drawPath(
                    path = path,
                    color = color.copy(alpha = 0.3f / layer),
                    style = Stroke(width = (4 - layer).dp.toPx(), cap = StrokeCap.Round)
                )
            }
            
            drawCircle(
                color = when(state) {
                    "Listening" -> Color.White.copy(alpha = 0.1f)
                    "Speaking" -> Color.White.copy(alpha = 0.15f)
                    "Thinking" -> Color.LightGray.copy(alpha = 0.15f)
                    else -> Color.Transparent
                },
                radius = baseRadius * 0.8f,
                center = center
            )
        }
    }
}

@Composable
fun LiveVoiceModalOverlay(
    viewModel: SparkexViewModel,
    onEnd: () -> Unit,
    onResult: (String) -> Unit
) {
    val context = LocalContext.current
    var isMuted by remember { mutableStateOf(false) }
    var partialText by remember { mutableStateOf("") }
    var hasSpoken by remember { mutableStateOf(false) }
    
    val isGenerating by viewModel.isGenerating.collectAsState()
    val streamingMessage by viewModel.currentStreamingMessage.collectAsState()
    val speechHelper = remember { com.example.util.SpeechHelper(context) }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { if (it) speechHelper.startListening() else onEnd() }
    
    DisposableEffect(Unit) {
        speechHelper.onResult = { hasSpoken = true; onResult(it) }
        speechHelper.onPartialResult = { partialText = it }
        speechHelper.onError = { partialText = "Try speaking again..." }
        
        if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            speechHelper.startListening()
        } else permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
        
        onDispose { speechHelper.destroy() }
    }
    
    LaunchedEffect(isGenerating) {
        if (!isGenerating && hasSpoken) {
            delay(800)
            partialText = ""
            speechHelper.startListening()
        }
    }

    val currentState = when {
        isGenerating && (streamingMessage?.text.isNullOrEmpty() || streamingMessage?.text == "Thinking...") -> "Thinking"
        isGenerating -> "Speaking"
        else -> "Listening"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF050505))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 64.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = currentState.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = 0.4f),
                letterSpacing = 2.sp
            )
        }

        Box(
            modifier = Modifier.align(Alignment.Center),
            contentAlignment = Alignment.Center
        ) {
            ModernWaveform(state = currentState)
            
            val displaySubtitle = when (currentState) {
                "Speaking" -> streamingMessage?.text ?: ""
                "Listening" -> if (partialText.isNotEmpty()) partialText else ""
                else -> ""
            }
            
            AnimatedVisibility(
                visible = displaySubtitle.isNotEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
                modifier = Modifier.align(Alignment.BottomCenter).offset(y = 120.dp)
            ) {
                Text(
                    text = displaySubtitle,
                    fontSize = 18.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.width(300.dp)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { 
                    isMuted = !isMuted
                    if (isMuted) speechHelper.stopListening() else speechHelper.startListening()
                },
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(if (isMuted) Color.White.copy(alpha = 0.1f) else Color.Transparent)
            ) {
                Icon(
                    imageVector = if (isMuted) Icons.Outlined.MicOff else Icons.Outlined.Mic,
                    contentDescription = null,
                    tint = if (isMuted) Color.Red else Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(32.dp))
            
            IconButton(
                onClick = onEnd,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            ) {
                Icon(Icons.Outlined.Close, contentDescription = null, tint = Color.Black, modifier = Modifier.size(28.dp))
            }
        }
    }
}

@Composable
fun ElegantLoadingIndicator(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulsingDots")
    
    val dotAnimation = @Composable { delay: Int ->
        infiniteTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 900
                    0.4f at 0 with FastOutSlowInEasing
                    1.0f at 450 with FastOutSlowInEasing
                    0.4f at 900
                },
                repeatMode = RepeatMode.Restart,
                initialStartOffset = StartOffset(delay)
            ),
            label = "dotPulse"
        )
    }

    val scale1 by dotAnimation(0)
    val scale2 by dotAnimation(300)
    val scale3 by dotAnimation(600)

    Row(
        modifier = modifier.padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val dotColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        
        listOf(scale1, scale2, scale3).forEach { scale ->
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        alpha = scale
                    }
                    .clip(CircleShape)
                    .background(dotColor)
            )
        }
    }
}
