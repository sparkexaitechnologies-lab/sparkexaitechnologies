import re

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "r") as f:
    content = f.read()

pattern = r"@Composable\nfun LiveVoiceModalOverlay\([\s\S]*?$"

new_overlay = """@Composable
fun LiveVoiceModalOverlay(
    viewModel: SparkexViewModel,
    onEnd: () -> Unit,
    onResult: (String) -> Unit
) {
    val context = LocalContext.current
    var isMuted by remember { mutableStateOf(false) }
    var isVideoOn by remember { mutableStateOf(false) }
    var showTranscript by remember { mutableStateOf(false) }
    var animatedWaveState by remember { mutableStateOf(true) }
    var partialText by remember { mutableStateOf("") }
    var hasSpoken by remember { mutableStateOf(false) }
    
    val isGenerating by viewModel.isGenerating.collectAsState()
    val streamingMessage by viewModel.currentStreamingMessage.collectAsState()
    
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
            hasSpoken = true
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
    
    // Automatically restart listening if generation is complete and we want continuous talk
    LaunchedEffect(isGenerating) {
        if (!isGenerating && hasSpoken) {
            // Wait a moment before listening again
            kotlinx.coroutines.delay(1000)
            partialText = ""
            speechHelper.startListening()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFDFBFB),
                        Color(0xFFFCE3EC), // Light pink
                        Color(0xFFEAE2FB), // Light purple
                        Color(0xFFE2F0FB)  // Light blue
                    )
                )
            )
    ) {
        // Top right close button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, end = 16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = onEnd) {
                Icon(Icons.Outlined.Close, contentDescription = "Close", tint = Color.Black)
            }
        }
        
        // Center Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isGenerating || streamingMessage != null) {
                // Response Mode
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "Sparkex AI Live Talk",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = streamingMessage?.text ?: "Thinking...",
                            fontSize = 16.sp,
                            color = Color.DarkGray
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(64.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color.White)
                        .clickable { 
                            // Interrupt: stop speaking and start listening
                            speechHelper.startListening()
                        }
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text("Tap to interrupt", fontSize = 16.sp, color = Color.Black)
                }
            } else {
                // Listening Mode
                Text(
                    text = "Speak now",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                
                Spacer(modifier = Modifier.height(48.dp))
                
                if (partialText.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = partialText,
                            fontSize = 20.sp,
                            color = Color.DarkGray,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Bottom Actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mute Button
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = { 
                        isMuted = !isMuted 
                        if (isMuted) speechHelper.stopListening() else speechHelper.startListening()
                    },
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(if (isMuted) Color.White else Color.White.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Outlined.MicOff else Icons.Outlined.Mic, 
                        contentDescription = "Mute", 
                        modifier = Modifier.size(32.dp), 
                        tint = Color.Black
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(if (isMuted) "Unmute" else "Mute", color = Color.Black, fontSize = 14.sp)
            }
            
            // Video Button
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = { isVideoOn = !isVideoOn },
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(if (isVideoOn) Color.White else Color.White.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = if (isVideoOn) Icons.Outlined.Videocam else Icons.Outlined.VideocamOff, 
                        contentDescription = "Video", 
                        modifier = Modifier.size(32.dp), 
                        tint = Color.Black
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Video", color = Color.Black, fontSize = 14.sp)
            }

            // Transcript Button
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = { showTranscript = !showTranscript },
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(if (showTranscript) Color.White else Color.White.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Subject, 
                        contentDescription = "Transcript", 
                        modifier = Modifier.size(32.dp), 
                        tint = Color.Black
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Transcript", color = Color.Black, fontSize = 14.sp)
            }
        }
    }
}
"""

new_content = re.sub(pattern, new_overlay, content)

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "w") as f:
    f.write(new_content)

print("Done")
