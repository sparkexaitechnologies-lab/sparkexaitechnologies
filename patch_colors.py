import re

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "r") as f:
    text = f.read()

# Replace hardcoded colors with MaterialTheme colors
text = text.replace("Color(0xFFF7F7F8)", "MaterialTheme.colorScheme.background")
text = text.replace("Color(0xFFF0F4F9)", "MaterialTheme.colorScheme.surfaceVariant")
text = text.replace("Color.White", "MaterialTheme.colorScheme.surface")
text = text.replace("Color.Black", "MaterialTheme.colorScheme.onSurface")
text = text.replace("Color(0xFF1C1C1E)", "MaterialTheme.colorScheme.onSurface")
text = text.replace("Color(0xFFE5E5EA)", "MaterialTheme.colorScheme.outline")
text = text.replace("Color(0xFF6E6E73)", "MaterialTheme.colorScheme.onSurfaceVariant")
text = text.replace("Color(0xFF8E8E93)", "MaterialTheme.colorScheme.onSurfaceVariant")
text = text.replace("Color(0xFFF7F7F8)", "MaterialTheme.colorScheme.background") # just in case

# Make sure MaterialTheme is imported
if "import androidx.compose.material3.MaterialTheme" not in text:
    text = text.replace("import androidx.compose.material3.Text", "import androidx.compose.material3.MaterialTheme\nimport androidx.compose.material3.Text")

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "w") as f:
    f.write(text)
