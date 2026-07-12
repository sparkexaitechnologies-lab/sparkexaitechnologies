with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "r") as f:
    text = f.read()

if "import androidx.compose.runtime.getValue" not in text:
    text = text.replace("import androidx.compose.runtime.remember", "import androidx.compose.runtime.remember\nimport androidx.compose.runtime.getValue\nimport androidx.compose.runtime.setValue")

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "w") as f:
    f.write(text)
