import re

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "r") as f:
    text = f.read()

old_ai_icon = """        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color.Transparent),
                contentAlignment = Alignment.TopCenter
            ) {
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFF4285F4),
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
        }"""

old_user_icon = """        if (isUser) {
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.outline),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }

                if (showActions) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Edit Prompt",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { /* Handle Edit Prompt */ }
                    )
                }
            }
        }"""

if old_ai_icon in text:
    text = text.replace(old_ai_icon, "")
    print("Patched AI icon")
else:
    print("Could not find AI icon")

if old_user_icon in text:
    # We still want the Edit prompt part, maybe just remove the Box containing the icon? Or remove the whole block.
    # The user said "Don't show user profile icon". The showActions only shows on long click.
    new_user_block = """        if (isUser) {
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (showActions) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Edit Prompt",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { /* Handle Edit Prompt */ }
                    )
                }
            }
        }"""
    text = text.replace(old_user_icon, new_user_block)
    print("Patched User icon")
else:
    print("Could not find User icon")

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "w") as f:
    f.write(text)
