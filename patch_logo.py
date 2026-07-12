import re

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "r") as f:
    text = f.read()

old_logo = """                                val geminiGradient = Brush.linearGradient(
                                    colors = listOf(Color(0xFF4285F4), Color(0xFF9b72cb), Color(0xFFd96570))
                                )
                                
                                Icon(
                                    imageVector = Icons.Outlined.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(48.dp)
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
                                )"""

new_logo = """                                val geminiGradient = Brush.linearGradient(
                                    colors = listOf(Color(0xFF4285F4), Color(0xFF9b72cb), Color(0xFFd96570))
                                )"""

if old_logo in text:
    text = text.replace(old_logo, new_logo)
    print("Logo removed.")
else:
    print("Logo not found.")

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "w") as f:
    f.write(text)
