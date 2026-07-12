import re

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "r") as f:
    text = f.read()

# Fix combinedClickable import
if "import androidx.compose.foundation.combinedClickable" not in text:
    text = text.replace("import androidx.compose.foundation.background", "import androidx.compose.foundation.background\nimport androidx.compose.foundation.combinedClickable")

# Replace Icons.AutoMirrored with Icons.Outlined since we just got an error about it (maybe it's not correctly imported)
text = text.replace("Icons.AutoMirrored.Outlined.VolumeMute", "Icons.Outlined.VolumeMute")
text = text.replace("Icons.AutoMirrored.Outlined.VolumeUp", "Icons.Outlined.VolumeUp")

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "w") as f:
    f.write(text)
