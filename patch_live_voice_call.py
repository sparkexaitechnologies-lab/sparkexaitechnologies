import re

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "r") as f:
    text = f.read()

old_call = """                // Interactive Live Voice Conversation overlay modal
                if (showLiveVoiceModal) {
                    LiveVoiceModalOverlay(
                        onEnd = { showLiveVoiceModal = false }
                    )
                }"""

new_call = """                // Interactive Live Voice Conversation overlay modal
                if (showLiveVoiceModal) {
                    LiveVoiceModalOverlay(
                        onEnd = { showLiveVoiceModal = false },
                        onResult = { result ->
                            viewModel.sendTextMessage(text = result, attachedImagePath = null, attachedVoicePath = null)
                            showLiveVoiceModal = false
                        }
                    )
                }"""

if old_call in text:
    text = text.replace(old_call, new_call)
    with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "w") as f:
        f.write(text)
    print("Patched call")
else:
    print("Could not find call")
