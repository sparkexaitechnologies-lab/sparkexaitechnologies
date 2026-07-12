import re

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "r") as f:
    text = f.read()

disclaimer_insert = """                            }
                        }
                    }
                }

                // Popup Attachment Menu above bar"""

new_disclaimer = """                            }
                        }
                    }
                    Text(
                        text = "Sparkex AI can make mistakes.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 8.dp, top = 4.dp)
                    )
                }

                // Popup Attachment Menu above bar"""

if disclaimer_insert in text:
    text = text.replace(disclaimer_insert, new_disclaimer)
    with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "w") as f:
        f.write(text)
    print("Patched disclaimer successfully")
else:
    print("Could not find Disclaimer location")
