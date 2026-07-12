import re

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "r") as f:
    text = f.read()

old_voice_btn = """                            // Voice Button
                            IconButton(
                                onClick = {
                                    try {
                                        val intent = android.content.Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                        }
                                        speechRecognizerLauncher.launch(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Voice input not supported", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Mic,
                                    contentDescription = "Voice Input",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                            }"""

new_voice_btn = """                            // Voice Button
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
                            }"""

if old_voice_btn in text:
    text = text.replace(old_voice_btn, new_voice_btn)
    
    # Let's change the hint text if listening
    text = text.replace('placeholder = { Text("Ask Sparkex AI...", color = MaterialTheme.colorScheme.onSurfaceVariant) },',
                        'placeholder = { Text(if (isListening) "Listening..." else "Ask Sparkex AI...", color = MaterialTheme.colorScheme.onSurfaceVariant) },')
    
    with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "w") as f:
        f.write(text)
    print("Patched regular voice button")
else:
    print("Could not find regular voice button")

