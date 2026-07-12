import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    text = f.read()

imports = "import com.example.util.NotificationHelper\n"
if imports not in text:
    text = text.replace("import android.os.Bundle", imports + "import android.os.Bundle")

if "NotificationHelper.createNotificationChannel(this)" not in text:
    text = text.replace("super.onCreate(savedInstanceState)", "super.onCreate(savedInstanceState)\n        NotificationHelper.createNotificationChannel(this)")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(text)

