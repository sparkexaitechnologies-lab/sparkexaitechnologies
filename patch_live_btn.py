import re

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "r") as f:
    text = f.read()

old_live_btn = """                            // Live AI Button
                            IconButton(
                                onClick = {
                                    showLiveVoiceModal = true
                                    try {
                                        val intent = android.content.Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                        }
                                        liveVoiceLauncher.launch(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Voice input not supported", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )"""

new_live_btn = """                            // Live AI Button
                            IconButton(
                                onClick = { showLiveVoiceModal = true }
                            )"""

if old_live_btn in text:
    text = text.replace(old_live_btn, new_live_btn)
    with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "w") as f:
        f.write(text)
    print("Patched live btn")
else:
    print("Could not find live btn")
