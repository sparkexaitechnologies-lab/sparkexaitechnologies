import re

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "r") as f:
    text = f.read()

old_thinking = """                                            val geminiGradient = Brush.linearGradient(
                                                colors = listOf(Color(0xFF4285F4), Color(0xFF9b72cb), Color(0xFFd96570))
                                            )
                                            Icon(
                                                imageVector = Icons.Outlined.AutoAwesome,
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .graphicsLayer(alpha = 0.99f)
                                                    .drawWithCache {
                                                        onDrawWithContent {
                                                            drawContent()
                                                            drawRect(
                                                                brush = geminiGradient,
                                                                blendMode = BlendMode.SrcAtop
                                                            )
                                                        }
                                                    }
                                            )
                                            Text("""

new_thinking = """                                            val geminiGradient = Brush.linearGradient(
                                                colors = listOf(Color(0xFF4285F4), Color(0xFF9b72cb), Color(0xFFd96570))
                                            )
                                            Text("""

if old_thinking in text:
    text = text.replace(old_thinking, new_thinking)
    with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "w") as f:
        f.write(text)
    print("Patched thinking icon")
else:
    print("Could not find thinking icon")
