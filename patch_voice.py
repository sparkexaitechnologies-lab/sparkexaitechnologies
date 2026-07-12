import re

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "r") as f:
    text = f.read()

old_voice_result = """                // Interactive Live Voice Conversation overlay modal
                if (showLiveVoiceModal) {
                    LiveVoiceModalOverlay(
                        onEnd = { showLiveVoiceModal = false },
                        onResult = { result ->
                            viewModel.sendTextMessage(text = result, attachedImagePath = null, attachedVoicePath = null)
                            showLiveVoiceModal = false
                        }
                    )
                }"""

new_voice_result = """                // Interactive Live Voice Conversation overlay modal
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
                }"""

if old_voice_result in text:
    text = text.replace(old_voice_result, new_voice_result)
    with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "w") as f:
        f.write(text)
    print("Voice handling updated")
else:
    print("Voice handling not found")
