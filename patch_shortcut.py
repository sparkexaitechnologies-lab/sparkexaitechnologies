import re

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "r") as f:
    text = f.read()

# Pattern for startNewSessionWithPrompt calls in ShortcutCard
# There are 3 calls.
# We will create a helper function at the beginning of ChatScreen or just replace them inline.
# Let's replace the onClick blocks.

def replace_shortcut(match):
    full_match = match.group(0)
    title = match.group(1)
    prompt = match.group(2)
    new_onClick = f"""onClick = {{
                                            if (activePlan == "Sparkex Free" && freeUses >= 10) {{
                                                showLimitModal = true
                                            }} else {{
                                                if (activePlan == "Sparkex Free") {{
                                                    val newCount = freeUses + 1
                                                    freeUses = newCount
                                                    prefs.edit().putInt("free_uses", newCount).apply()
                                                }}
                                                viewModel.startNewSessionWithPrompt(
                                                    {title},
                                                    {prompt}
                                                )
                                            }}
                                        }}"""
    return new_onClick

text = re.sub(r'onClick\s*=\s*\{\s*viewModel\.startNewSessionWithPrompt\(\s*(".*?")\s*,\s*(".*?")\s*\)\s*\}', replace_shortcut, text)

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "w") as f:
    f.write(text)
print("Shortcut patched")
