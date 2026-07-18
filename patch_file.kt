@Composable
fun ChatGPTLiveTalkButton(
    onClick: () -> Unit,
    isListening: Boolean,
    isSpeaking: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(if (isSpeaking) 600 else if (isListening) 1000 else 2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val baseHeight = 6.dp
    val amplitude = if (isSpeaking) 14.dp else if (isListening) 10.dp else 4.dp

    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Color(0xFF2F6BFF))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until 4) {
                val offset = i * (Math.PI / 1.5f)
                val animatedScale = (Math.sin(phase.toDouble() + offset).toFloat() + 1f) / 2f
                val barHeight = baseHeight + (amplitude * animatedScale)
                
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(barHeight)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }
    }
}
