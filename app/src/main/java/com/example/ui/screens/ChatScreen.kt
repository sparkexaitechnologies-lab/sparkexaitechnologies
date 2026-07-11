package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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

    val sessions by viewModel.sessions.collectAsState()
    val activeSessionId by viewModel.activeSessionId.collectAsState()
    val messages by viewModel.activeMessages.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val deepResearchEnabled by viewModel.deepResearchEnabled.collectAsState()
    val profile by viewModel.userProfile.collectAsState()
    val currentlyPlayingMessageId by viewModel.currentlyPlayingMessageId.collectAsState()

    var textInput by remember { mutableStateOf("") }
    var attachedImagePath by remember { mutableStateOf<String?>(null) }
    var attachedVoicePath by remember { mutableStateOf<String?>(null) }

    var showAttachmentMenu by remember { mutableStateOf(false) }
    var showLiveVoiceModal by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

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
            containerColor = Color(0xFFF7F7F8), // Soft off-white background
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Sparkex AI",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color.Black
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Outlined.Menu,
                                contentDescription = "Sidebar Menu",
                                tint = Color.Black,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    actions = {},
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color(0xFFF7F7F8)
                    )
                )
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
                                
                                Icon(
                                    imageVector = Icons.Outlined.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .graphicsLayer(alpha = 0.99f)
                                        .drawWithCache {
                                            onDrawWithContent {
                                                drawContent()
                                                drawRect(
                                                    brush = geminiGradient,
                                                    blendMode = BlendMode.SrcAtop
                                                )
                                            }
                                        }
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
                                        icon = Icons.Outlined.Lightbulb,
                                        title = "Business Ideas",
                                        description = "Brainstorm tech startups",
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            viewModel.startNewSessionWithPrompt(
                                                "Business Ideas",
                                                "Give me 3 innovative business ideas in tech."
                                            )
                                        }
                                    )
                                    ShortcutCard(
                                        icon = Icons.Outlined.Book,
                                        title = "Study Helper",
                                        description = "Explain complex topics",
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            viewModel.startNewSessionWithPrompt(
                                                "Study Help",
                                                "Explain quantum mechanics in simple terms."
                                            )
                                        }
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    ShortcutCard(
                                        icon = Icons.Outlined.Code,
                                        title = "Coding Help",
                                        description = "Refactor and debug code",
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            viewModel.startNewSessionWithPrompt(
                                                "Coding Help",
                                                "Write a clean Kotlin singleton pattern."
                                            )
                                        }
                                    )
                                    ShortcutCard(
                                        icon = Icons.Outlined.Movie,
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
                                    onPlayVoice = { viewModel.playMessageVoice(message) }
                                )
                            }

                            if (isGenerating) {
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = Color.Black,
                                            strokeWidth = 2.dp
                                        )
                                        Text(
                                            text = "Sparkex AI is compiling response...",
                                            fontSize = 13.sp,
                                            color = Color(0xFF6E6E73)
                                        )
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
                                .background(Color.White)
                                .border(1.dp, Color(0xFFE5E5EA))
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
                                        tint = Color.Black,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Deep Research Mode Active",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = null,
                                    tint = Color.Black,
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
                                .background(Color.White)
                                .border(1.dp, Color(0xFFE5E5EA))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            attachedImagePath?.let { path ->
                                Box(modifier = Modifier.size(52.dp)) {
                                    AsyncImage(
                                        model = File(path),
                                        contentDescription = "Attached Photo Preview",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.clip(RoundedCornerShape(8.dp))
                                    )
                                    IconButton(
                                        onClick = { attachedImagePath = null },
                                        modifier = Modifier
                                            .size(20.dp)
                                            .align(Alignment.TopEnd)
                                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Close,
                                            contentDescription = "Remove",
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }

                            attachedVoicePath?.let { path ->
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFF7F7F8))
                                        .border(1.dp, Color(0xFFE5E5EA), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Mic,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text("Voice snippet ready", fontSize = 12.sp, color = Color.Black)
                                    IconButton(
                                        onClick = { attachedVoicePath = null },
                                        modifier = Modifier.size(16.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Close,
                                            contentDescription = "Remove Audio",
                                            modifier = Modifier.size(12.dp),
                                            tint = Color(0xFF6E6E73)
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
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFFF0F4F9))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Plus Button
                        IconButton(onClick = { showAttachmentMenu = !showAttachmentMenu }) {
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = "Plus Attachment",
                                tint = Color(0xFF6E6E73),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Borderless TextField
                        TextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            placeholder = {
                                Text(
                                    text = "Ask Sparkex...",
                                    color = Color(0xFF6E6E73),
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
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black
                            ),
                            maxLines = 4
                        )

                        // Voice, Live AI, Send buttons
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Voice Button
                            IconButton(
                                onClick = {
                                    attachedVoicePath = "/simulated/audio_path.wav"
                                    textInput = "Explain quantum computing in simple terms"
                                    Toast.makeText(context, "Simulated voice dictation complete.", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Mic,
                                    contentDescription = "Voice Input",
                                    tint = Color(0xFF6E6E73),
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
                                    tint = Color(0xFF6E6E73),
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            // Send Button
                            IconButton(
                                onClick = {
                                    if (textInput.isNotBlank() || attachedImagePath != null || attachedVoicePath != null) {
                                        viewModel.sendTextMessage(
                                            text = textInput,
                                            attachedImagePath = attachedImagePath,
                                            attachedVoicePath = attachedVoicePath
                                        )
                                        textInput = ""
                                        attachedImagePath = null
                                        attachedVoicePath = null
                                    }
                                },
                                enabled = textInput.isNotBlank() || attachedImagePath != null || attachedVoicePath != null
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ArrowUpward,
                                    contentDescription = "Send Message",
                                    tint = if (textInput.isNotBlank() || attachedImagePath != null || attachedVoicePath != null) Color.Black else Color(0xFFE5E5EA),
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
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, Color(0xFFE5E5EA), RoundedCornerShape(16.dp))
                            .background(Color.White)
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

                // Interactive Live Voice Conversation overlay modal
                if (showLiveVoiceModal) {
                    LiveVoiceModalOverlay(
                        onEnd = { showLiveVoiceModal = false }
                    )
                }
            }
        }
    }
}

@Composable
fun ShortcutCard(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val (bgCircleColor, iconTint) = when (title) {
        "Business Ideas" -> Pair(Color(0xFFFEF3C7), Color(0xFFD97706)) // Amber/Gold theme
        "Study Helper" -> Pair(Color(0xFFDBEAFE), Color(0xFF2563EB))   // Blue theme
        "Coding Help" -> Pair(Color(0xFFEEF2F6), Color(0xFF4F46E5))    // Indigo theme
        else -> Pair(Color(0xFFFFEDD5), Color(0xFFEA580C))             // Orange/Warm theme
    }

    Card(
        onClick = onClick,
        modifier = modifier.height(115.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF0F4F9)
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(bgCircleColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = Color(0xFF6E6E73),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun ChatMessageBubble(
    message: ChatMessage,
    isPlaying: Boolean,
    onPlayVoice: () -> Unit
) {
    val isUser = message.role == "user"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE5E5EA), CircleShape)
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp) // Same style size
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
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
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFFE5E5EA), RoundedCornerShape(12.dp))
                ) {
                    if (message.imagePath.startsWith("/simulated/")) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White),
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

            // Text Bubble Container (Cards: pure white, Borders: very light gray)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE5E5EA), RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    MarkdownText(
                        text = message.text,
                        color = Color.Black
                    )

                    // Optional audio dictation widget
                    if (!isUser && message.voicePath != null) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF7F7F8))
                                .border(1.dp, Color(0xFFE5E5EA), RoundedCornerShape(12.dp))
                                .clickable { onPlayVoice() }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Outlined.VolumeMute else Icons.Outlined.VolumeUp,
                                contentDescription = "Play Audio",
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            if (isPlaying) {
                                AudioWaveformIndicator(isAnimating = true, color = Color.Black, modifier = Modifier.height(12.dp))
                            } else {
                                Text("Speak reply", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                        }
                    }
                }
            }
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE5E5EA), CircleShape)
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp) // Same size
                )
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
            tint = Color.Black,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = Color.Black
        )
    }
}

@Composable
fun LiveVoiceModalOverlay(
    onEnd: () -> Unit
) {
    var animatedWaveState by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White), // Sleek minimalist flat white overlay
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Sparkex AI ", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color.Black)
                Text("Live Mode", fontWeight = FontWeight.Normal, fontSize = 24.sp, color = Color(0xFF6E6E73))
            }

            Text(
                text = "Voice Conversation Session Active",
                color = Color(0xFF6E6E73),
                fontSize = 13.sp
            )

            Box(
                modifier = Modifier
                    .height(100.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                AudioWaveformIndicator(
                    color = Color.Black,
                    isAnimating = animatedWaveState,
                    modifier = Modifier.scale(1.5f)
                )
            }

            Text(
                text = "Listening... speak contextually",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF6E6E73)
            )

            // Terminate connection button
            IconButton(
                onClick = onEnd,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.Black)
            ) {
                Icon(
                    imageVector = Icons.Outlined.CallEnd,
                    contentDescription = "End Call",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
