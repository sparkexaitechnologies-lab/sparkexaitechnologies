import re

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "r") as f:
    text = f.read()

# Replace ShortcutCard calls
old_calls = """                                    ShortcutCard(
                                        icon = Icons.Outlined.Lightbulb,
                                        title = "Business Ideas",
                                        description = "Brainstorm tech startups",
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            viewModel.startNewSessionWithPrompt(
                                                "Business Ideas",
                                                "Give me 3 innovative business ideas in tech."
                                            )
                                        }
                                    )
                                    ShortcutCard(
                                        icon = Icons.Outlined.MenuBook,
                                        title = "Study Helper",
                                        description = "Explain complex topics",
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            viewModel.startNewSessionWithPrompt(
                                                "Study Help",
                                                "Explain quantum mechanics in simple terms."
                                            )
                                        }
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    ShortcutCard(
                                        icon = Icons.Outlined.Code,
                                        title = "Coding Help",
                                        description = "Refactor and debug code",
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            viewModel.startNewSessionWithPrompt(
                                                "Coding Help",
                                                "Write a clean Kotlin singleton pattern."
                                            )
                                        }
                                    )
                                    ShortcutCard(
                                        icon = Icons.Outlined.Movie,
                                        title = "Create AI Videos",
                                        description = "Harness Veo models",
                                        modifier = Modifier.weight(1f),
                                        onClick = onNavigateToVideoCreator
                                    )"""

new_calls = """                                    ShortcutCard(
                                        icon = "💡",
                                        title = "Business Ideas",
                                        description = "Brainstorm tech startups",
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            viewModel.startNewSessionWithPrompt(
                                                "Business Ideas",
                                                "Give me 3 innovative business ideas in tech."
                                            )
                                        }
                                    )
                                    ShortcutCard(
                                        icon = "📖",
                                        title = "Study Helper",
                                        description = "Explain complex topics",
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            viewModel.startNewSessionWithPrompt(
                                                "Study Help",
                                                "Explain quantum mechanics in simple terms."
                                            )
                                        }
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    ShortcutCard(
                                        icon = "</>",
                                        title = "Coding Help",
                                        description = "Refactor and debug code",
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            viewModel.startNewSessionWithPrompt(
                                                "Coding Help",
                                                "Write a clean Kotlin singleton pattern."
                                            )
                                        }
                                    )
                                    ShortcutCard(
                                        icon = "🎥",
                                        title = "Create AI Videos",
                                        description = "Harness Veo models",
                                        modifier = Modifier.weight(1f),
                                        onClick = onNavigateToVideoCreator
                                    )"""
if old_calls in text:
    text = text.replace(old_calls, new_calls)
    print("Replaced ShortcutCard calls")

# Replace ShortcutCard composable
old_composable = """@Composable
fun ShortcutCard(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(28.dp)
            )
            
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.Black
            )
            
            Text(
                text = description,
                fontSize = 12.sp,
                color = Color(0xFF6E6E73),
                maxLines = 1
            )
        }
    }
}"""

new_composable = """@Composable
fun ShortcutCard(
    icon: String,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = icon,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.Black
            )
            
            Text(
                text = description,
                fontSize = 12.sp,
                color = Color(0xFF6E6E73),
                maxLines = 1
            )
        }
    }
}"""

if old_composable in text:
    text = text.replace(old_composable, new_composable)
    print("Replaced ShortcutCard composable")

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "w") as f:
    f.write(text)

