import re

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "r") as f:
    text = f.read()

# Fix the Book icon
text = text.replace("Icons.Outlined.Book", "Icons.Outlined.MenuBook")

# Replace ShortcutCard
old_card = """fun ShortcutCard(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val (bgCircleColor, iconTint) = when (title) {
        "Business Ideas" -> Pair(Color(0xFFFEF3C7), Color(0xFFD97706)) // Amber/Gold theme
        "Study Helper" -> Pair(Color(0xFFDBEAFE), Color(0xFF2563EB))   // Blue theme
        "Coding Help" -> Pair(Color(0xFFEEF2F6), Color(0xFF4F46E5))    // Indigo theme
        else -> Pair(Color(0xFFFFEDD5), Color(0xFFEA580C))             // Orange/Warm theme
    }

    Card(
        onClick = onClick,
        modifier = modifier.height(115.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF0F4F9)
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(bgCircleColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = Color(0xFF6E6E73),
                    maxLines = 1
                )
            }
        }
    }
}"""

new_card = """fun ShortcutCard(
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

if old_card in text:
    text = text.replace(old_card, new_card)
    with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "w") as f:
        f.write(text)
    print("Patched ShortcutCard successfully")
else:
    print("Could not find ShortcutCard block")
