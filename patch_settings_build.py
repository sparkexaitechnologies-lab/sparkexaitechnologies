import re

with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "r") as f:
    text = f.read()

text = text.replace("Build.VERSION", "android.os.Build.VERSION")
text = text.replace("Manifest.permission", "android.Manifest.permission")

with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "w") as f:
    f.write(text)

