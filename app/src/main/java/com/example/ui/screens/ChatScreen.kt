package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.ChatMessage
import com.example.ui.viewmodel.SparkexViewModel
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: SparkexViewModel,
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
    var activePlan by remember { mutableStateOf(prefs.getString("active_plan", "Sparkex Free") ?: "Sparkex Free") }
    var freeUses by remember { mutableStateOf(prefs.getInt("free_uses", 0)) }
    var showLimitModal by remember { mutableStateOf(false) }

    var showDropdown by remember { mutableStateOf(false) }
    var showPlanModal by remember { mutableStateOf<String?>(null) }

    val sessions by viewModel.sessions.collectAsState()
    val activeSessionId by viewModel.activeSessionId.collectAsState()
    val messages by viewModel.activeMessages.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val deepResearchEnabled by viewModel.deepResearchEnabled.collectAsState()
    val profile by viewModel.userProfile.collectAsState()
    val currentlyPlayingMessageId by viewModel.currentlyPlayingMessageId.collectAsState()
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


    // Auto scroll to latest reply
    LaunchedEffect(messages.size, isGenerating) {
        if (messages.isNotEmpty()) {
            val targetIndex = messages.size - 1 + (if (isGenerating) 1 else 0)
            if (targetIndex >= 0) {
                listState.animateScrollToItem(targetIndex)
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            SidebarComponent(
                sessions = sessions,
                activeSessionId = activeSessionId,
                profile = profile,
                onSelectSession = { id -> viewModel.selectSession(id) },
                onDeleteSession = { id -> viewModel.deleteSession(id) },
                onNewChat = { viewModel.startNewSession() },
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToImages = onNavigateToImages,
                onNavigateToVideoCreator = onNavigateToVideoCreator,
                onNavigateToHelp = onNavigateToHelp,
                onNavigateToMemberships = onNavigateToMemberships,
                onCloseSidebar = { coroutineScope.launch { drawerState.close() } }
            )
        }
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background, // Soft off-white background
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Menu Icon (Leftmost position)
                    IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                        Icon(
                            imageVector = Icons.Outlined.Menu,
                            contentDescription = "Menu",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(4.dp))

                    // 2. Sparkex Model Selector Dropdown (Gemini Pro Position)
                    Box {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showDropdown = true }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = activePlan, // "Sparkex Plus" or "Sparkex Pro"
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Outlined.ArrowDropDown,
                                contentDescription = "Select Plan",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        DropdownMenu(
                            expanded = showDropdown,
                            onDismissRequest = { showDropdown = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Sparkex Free") },
                                onClick = {
                                    showDropdown = false
                                    activePlan = "Sparkex Free"
                                    prefs.edit().putString("active_plan", "Sparkex Free").apply()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Sparkex Plus") },
                                onClick = {
                                    showDropdown = false
                                    activePlan = "Sparkex Plus"
                                    prefs.edit().putString("active_plan", "Sparkex Plus").apply()
                                    showPlanModal = "Sparkex Plus Tier - ₹199/month"
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Sparkex Pro") },
                                onClick = {
                                    showDropdown = false
                                    activePlan = "Sparkex Pro"
                                    prefs.edit().putString("active_plan", "Sparkex Pro").apply()
                                    showPlanModal = "Sparkex Pro Tier - ₹399/month"
                                }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Chat Screen Main Area
                    if (activeSessionId == null || messages.isEmpty()) {
                        // Empty Chat state - Large, clean margins
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Friendly welcome message
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.padding(bottom = 36.dp)
                            ) {
                                val geminiGradient = Brush.linearGradient(
                                    colors = listOf(Color(0xFF4285F4), Color(0xFF9b72cb), Color(0xFFd96570))
                                )

                                Text(
                                    text = "How can I help you today?",
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    style = androidx.compose.ui.text.TextStyle(
                                        brush = geminiGradient
                                    ),
                                    letterSpacing = (-0.5).sp
                                )
                            }

                            // Four clean feature cards in a 2x2 grid
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    ShortcutCard(
                                        icon = "💡",
                                        title = "Business Ideas",
                                        description = "Brainstorm tech startups",
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            if (activePlan == "Sparkex Free" && freeUses >= 10) {
                                                showLimitModal = true
                                            } else {
                                                if (activePlan == "Sparkex Free") {
                                                    val newCount = freeUses + 1
                                                    freeUses = newCount
                                                    prefs.edit().putInt("free_uses", newCount).apply()
                                                }
                                                viewModel.startNewSessionWithPrompt(
                                                    "Business Ideas",
                                                    "Give me 3 innovative business ideas in tech."
                                                )
                                            }
                                        }
                                    )
                                    ShortcutCard(
                                        icon = "📖",
                                        title = "Study Helper",
                                        description = "Explain complex topics",
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            if (activePlan == "Sparkex Free" && freeUses >= 10) {
                                                showLimitModal = true
                                            } else {
                                                if (activePlan == "Sparkex Free") {
                                                    val newCount = freeUses + 1
                                                    freeUses = newCount
                                                    prefs.edit().putInt("free_uses", newCount).apply()
                                                }
                                                viewModel.startNewSessionWithPrompt(
                                                    "Study Help",
                                                    "Explain quantum mechanics in simple terms."
                                                )
                                            }
                                        }
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    ShortcutCard(
                                        icon = "</>",
                                        title = "Coding Help",
                                        description = "Refactor and debug code",
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            if (activePlan == "Sparkex Free" && freeUses >= 10) {
                                                showLimitModal = true
                                            } else {
                                                if (activePlan == "Sparkex Free") {
                                                    val newCount = freeUses + 1
                                                    freeUses = newCount
                                                    prefs.edit().putInt("free_uses", newCount).apply()
                                                }
                                                viewModel.startNewSessionWithPrompt(
                                                    "Coding Help",
                                                    "Write a clean Kotlin singleton pattern."
                                                )
                                            }
                                        }
                                    )
                                    ShortcutCard(
                                        icon = "🎥",
                                        title = "Create AI Videos",
                                        description = "Harness Veo models",
                                        modifier = Modifier.weight(1f),
                                        onClick = onNavigateToVideoCreator
                                    )
                                }
                            }
                        }
                    } else {
                        // Conversations Scrolling Area
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            items(messages, key = { it.id }) { message ->
                                ChatMessageBubble(
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
                                )
                            }
                            
                            currentStreamingMessage?.let { streamMsg ->
                                item(key = "streaming") {
                                    Column {
                                        ChatMessageBubble(
                                            message = streamMsg,
                                            isPlaying = false,
                                            onPlayVoice = {},
                                            onRegenerate = {}
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Button(
                                                onClick = { viewModel.stopGenerating() },
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.outline, contentColor = MaterialTheme.colorScheme.onSurface)
                                            ) {
                                                Icon(Icons.Outlined.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Stop Generating")
                                            }
                                        }
                                    }
                                }
                            }

                            if (isGenerating && currentStreamingMessage == null) {
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 12.dp, horizontal = 16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            val geminiGradient = Brush.linearGradient(
                                                colors = listOf(Color(0xFF4285F4), Color(0xFF9b72cb), Color(0xFFd96570))
                                            )
                                            Text(
                                                text = "Sparkex AI is thinking...",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium,
                                                style = androidx.compose.ui.text.TextStyle(
                                                    brush = geminiGradient
                                                )
                                            )
                                        }
                                        // Stop Button
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                                .clickable { viewModel.stopGenerating() },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Stop,
                                                contentDescription = "Stop Generating",
                                                tint = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Deep Research Banner Indicator
                    AnimatedVisibility(visible = deepResearchEnabled) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, MaterialTheme.colorScheme.outline)
                                .padding(vertical = 8.dp, horizontal = 16.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Search,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Deep Research Mode Active",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    // Previews of attachment snippets
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
                                                .clip(RoundedCornerShape(8.dp))
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
                                            modifier = Modifier.clip(RoundedCornerShape(8.dp))
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
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.background)
                                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
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

                    // Modern rounded chat input bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp)
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Plus Button
                        IconButton(onClick = { showAttachmentMenu = !showAttachmentMenu }) {
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = "Plus Attachment",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Borderless TextField
                        TextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            placeholder = {
                                Text(
                                    text = "Ask Sparkex AI...",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 15.sp
                                )
                            },
                            modifier = Modifier.weight(1f),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            ),
                            maxLines = 4
                        )

                        // Voice, Live AI, Send buttons
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Voice Button
                            val speechHelper = remember { com.example.util.SpeechHelper(context) }
                            var isListening by remember { mutableStateOf(false) }
                            
                            val voicePermissionLauncher = rememberLauncherForActivityResult(
                                contract = ActivityResultContracts.RequestPermission()
                            ) { isGranted ->
                                if (isGranted) {
                                    isListening = true
                                    speechHelper.startListening()
                                } else {
                                    Toast.makeText(context, "Microphone permission required", Toast.LENGTH_SHORT).show()
                                }
                            }
                            
                            DisposableEffect(Unit) {
                                speechHelper.onResult = { result ->
                                    textInput = result
                                    isListening = false
                                }
                                speechHelper.onPartialResult = { result ->
                                    textInput = result
                                }
                                speechHelper.onError = {
                                    isListening = false
                                    Toast.makeText(context, "Voice input error", Toast.LENGTH_SHORT).show()
                                }
                                onDispose { speechHelper.destroy() }
                            }

                            IconButton(
                                onClick = {
                                    if (isListening) {
                                        isListening = false
                                        speechHelper.stopListening()
                                    } else {
                                        if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                            isListening = true
                                            speechHelper.startListening()
                                        } else {
                                            voicePermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (isListening) Icons.Outlined.Stop else Icons.Outlined.Mic,
                                    contentDescription = "Voice Input",
                                    tint = if (isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            // Live AI Button
                            IconButton(
                                onClick = { showLiveVoiceModal = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.GraphicEq,
                                    contentDescription = "Live AI Mode",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            // Send Button
                            IconButton(
                                onClick = {
                                    if (activePlan == "Sparkex Free" && freeUses >= 10) {
                                        showLimitModal = true
                                    } else {
                                        if (textInput.isNotBlank() || attachedImagePath != null || attachedVoicePath != null) {
                                            if (activePlan == "Sparkex Free") {
                                                val newCount = freeUses + 1
                                                freeUses = newCount
                                                prefs.edit().putInt("free_uses", newCount).apply()
                                            }
                                            viewModel.sendTextMessage(
                                                text = textInput,
                                                attachedImagePath = attachedImagePath,
                                                attachedVoicePath = attachedVoicePath
                                            )
                                            textInput = ""
                                            attachedImagePath = null
                                            attachedVoicePath = null
                                        }
                                    }
                                },
                                enabled = textInput.isNotBlank() || attachedImagePath != null || attachedVoicePath != null
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ArrowUpward,
                                    contentDescription = "Send Message",
                                    tint = if (textInput.isNotBlank() || attachedImagePath != null || attachedVoicePath != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(24.dp)
                                )
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
                            .width(240.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(6.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            AttachmentMenuItem(icon = Icons.Outlined.CameraAlt, title = "Camera snapshot") {
                                attachedImagePath = "/simulated/camera_capture.jpg"
                                showAttachmentMenu = false
                                Toast.makeText(context, "Attached simulated Camera capture.", Toast.LENGTH_SHORT).show()
                            }
                            AttachmentMenuItem(icon = Icons.Outlined.Image, title = "Upload from Gallery") {
                                attachedImagePath = "/simulated/gallery_upload.jpg"
                                showAttachmentMenu = false
                                Toast.makeText(context, "Attached simulated Gallery upload.", Toast.LENGTH_SHORT).show()
                            }
                            AttachmentMenuItem(icon = Icons.Outlined.UploadFile, title = "Attach document file") {
                                showAttachmentMenu = false
                                Toast.makeText(context, "Attached document file.", Toast.LENGTH_SHORT).show()
                            }
                            AttachmentMenuItem(icon = Icons.Outlined.Search, title = "Toggle Deep Research") {
                                viewModel.toggleDeepResearch()
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

                // Limit Reached Modal Overlay
                if (showLimitModal) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f))
                            .clickable { showLimitModal = false },
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
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = "Limit Reached",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = "Free Limit Reached",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "You've used your 10 free messages. Upgrade to Sparkex Plus or Sparkex Pro for unlimited access.",
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Button(
                                        onClick = { showLimitModal = false },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                            contentColor = MaterialTheme.colorScheme.onSurface
                                        )
                                    ) {
                                        Text("Cancel")
                                    }
                                    Button(
                                        onClick = {
                                            showLimitModal = false
                                            onNavigateToMemberships()
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        )
                                    ) {
                                        Text("Show Plans")
                                    }
                                }
                            }
                        }
                    }
                }

                // Interactive Live Voice Conversation overlay modal
                if (showLiveVoiceModal) {
                    LiveVoiceModalOverlay(
                        onEnd = { showLiveVoiceModal = false },
                        onResult = { result ->
                            if (activePlan == "Sparkex Free" && freeUses >= 10) {
                                showLiveVoiceModal = false
                                showLimitModal = true
                            } else {
                                if (activePlan == "Sparkex Free") {
                                    val newCount = freeUses + 1
                                    freeUses = newCount
                                    prefs.edit().putInt("free_uses", newCount).apply()
                                }
                                viewModel.sendTextMessage(text = result, attachedImagePath = null, attachedVoicePath = null)
                                showLiveVoiceModal = false
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ShortcutCard(
    icon: String,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = icon,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
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
    var isDisliked by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
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
                        .padding(bottom = 6.dp)
                        .width(200.dp)
                        .height(150.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    if (message.imagePath.startsWith("/simulated/")) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.outline),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onSurface
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
                    .background(if (isUser) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent)
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
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    // Optional audio dictation widget
                    if (!isUser && message.voicePath != null) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
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
            .clip(RoundedCornerShape(12.dp))
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
fun LiveVoiceModalOverlay(
    onEnd: () -> Unit,
    onResult: (String) -> Unit
) {
    val context = LocalContext.current
    var animatedWaveState by remember { mutableStateOf(true) }
    var partialText by remember { mutableStateOf("Listening... speak contextually") }
    
    val speechHelper = remember { com.example.util.SpeechHelper(context) }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            speechHelper.startListening()
        } else {
            Toast.makeText(context, "Microphone permission required for Live Talk", Toast.LENGTH_SHORT).show()
            onEnd()
        }
    }
    
    DisposableEffect(Unit) {
        speechHelper.onResult = { result ->
            onResult(result)
        }
        speechHelper.onPartialResult = { result ->
            partialText = result
        }
        speechHelper.onError = { error ->
            animatedWaveState = false
            partialText = "Error listening. Please try again."
        }
        
        if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            speechHelper.startListening()
        } else {
            permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
        }
        
        onDispose {
            speechHelper.destroy()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface), // Sleek minimalist flat white overlay
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Sparkex AI ", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = MaterialTheme.colorScheme.onSurface)
                Text("Live Mode", fontWeight = FontWeight.Normal, fontSize = 24.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Text(
                text = "Voice Conversation Session Active",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )

            Box(
                modifier = Modifier
                    .height(100.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                AudioWaveformIndicator(
                    color = MaterialTheme.colorScheme.onSurface,
                    isAnimating = animatedWaveState,
                    modifier = Modifier.scale(1.5f)
                )
            }

            Text(
                text = partialText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Terminate connection button
            IconButton(
                onClick = onEnd,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface)
            ) {
                Icon(
                    imageVector = Icons.Outlined.CallEnd,
                    contentDescription = "End Call",
                    tint = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
