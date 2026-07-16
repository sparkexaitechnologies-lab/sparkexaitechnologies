import re

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "r") as f:
    content = f.read()

# 1. Remove increment logic
# Pattern: if (activePlan == "Sparkex Free") { ... }
increment_pattern = r'if \(activePlan == "Sparkex Free"\) \{\s+val newCount = freeUses \+ 1\s+freeUses = newCount\s+prefs\.edit\(\)\.putInt\("free_uses", newCount\)\.apply\(\)\s+\}'
content = re.sub(increment_pattern, '', content)

# 2. Remove limit checks
# Pattern: if (activePlan == "Sparkex Free" && freeUses >= 10) { ... } else { ... }
# We want to replace it with the content of the else block.

limit_pattern = r'if \(activePlan == "Sparkex Free" && freeUses >= 10\) \{\s+showLimitModal = true\s+\} else \{([\s\S]*?)\}'

def replace_limit(match):
    else_content = match.group(1)
    # Dedent if necessary or just return it
    return else_content.strip()

# Note: The above simple regex might be greedy if there are nested braces. 
# Let's try a more specific one for each case or use a loop.

# Case for onRegenerate
reg_pattern = r'onRegenerate = \{\s+if \(activePlan == "Sparkex Free" && freeUses >= 10\) \{\s+showLimitModal = true\s+\} else \{([\s\S]*?)\}\s+\}'
content = re.sub(reg_pattern, r'onRegenerate = {\1}', content)

# Case for Send Button onClick
send_pattern = r'onClick = \{\s+if \(activePlan == "Sparkex Free" && freeUses >= 10\) \{\s+showLimitModal = true\s+\} else \{([\s\S]*?)\}\s+\},'
content = re.sub(send_pattern, r'onClick = {\1},', content)

# Case for LiveVoiceModalOverlay onResult
live_pattern = r'onResult = \{ result ->\s+if \(activePlan == "Sparkex Free" && freeUses >= 10\) \{\s+showLiveVoiceModal = false\s+showLimitModal = true\s+\} else \{([\s\S]*?)\}\s+\}'
content = re.sub(live_pattern, r'onResult = { result ->\1}', content)

# Case for ShortcutCard onClick (they were already mostly handled but let's be sure)
shortcut_pattern = r'onClick = \{\s+if \(activePlan == "Sparkex Free" && freeUses >= 10\) \{\s+showLimitModal = true\s+\} else \{([\s\S]*?)\}\s+\}'
content = re.sub(shortcut_pattern, r'onClick = {\1}', content)

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "w") as f:
    f.write(content)

print("Done")
