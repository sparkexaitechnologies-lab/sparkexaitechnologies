import re

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "r") as f:
    text = f.read()

old_fun = """fun LiveVoiceModalOverlay(
    onEnd: () -> Unit
) {
    var animatedWaveState by remember { mutableStateOf(true) }"""

new_fun = """fun LiveVoiceModalOverlay(
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
    }"""

if old_fun in text:
    text = text.replace(old_fun, new_fun)
    text = text.replace('text = "Listening... speak contextually",', 'text = partialText,')
    text = text.replace('if (showLiveVoiceModal) {\n        LiveVoiceModalOverlay(\n            onEnd = { showLiveVoiceModal = false }\n        )\n    }', '''if (showLiveVoiceModal) {
        LiveVoiceModalOverlay(
            onEnd = { showLiveVoiceModal = false },
            onResult = { result ->
                viewModel.sendTextMessage(text = result, attachedImagePath = null, attachedVoicePath = null)
                showLiveVoiceModal = false
            }
        )
    }''')
    with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "w") as f:
        f.write(text)
    print("Patched LiveVoiceModalOverlay")
else:
    print("Could not find LiveVoiceModalOverlay")
