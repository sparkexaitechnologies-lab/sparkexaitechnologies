package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SparkexGold
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoCreatorScreen(
    onBack: () -> Unit
) {
    var prompt by remember { mutableStateOf("") }
    var aspectRatio by remember { mutableStateOf("16:9") }
    var duration by remember { mutableStateOf("5s") }
    var isCompiling by remember { mutableStateOf(false) }
    var compileStatus by remember { mutableStateOf("") }
    var progressVal by remember { mutableStateOf(0f) }
    var generatedVideoResult by remember { mutableStateOf<VideoCompileResult?>(null) }

    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Veo Video Director", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "AI Video Director Studio",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Harness the power of Google Veo text-to-video diffusion engines to craft professional cinematic visual shots instantly.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Describe Your Cinematic Scene",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        OutlinedTextField(
                            value = prompt,
                            onValueChange = { prompt = it },
                            placeholder = { Text("Cinematic slow tracking shot of a silver astronaut exploring a vibrant alien crystal forest, cinematic lighting, 8k...") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SparkexGold,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Aspect Ratio", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    listOf("16:9", "9:16", "1:1").forEach { ratio ->
                                        FilterChip(
                                            selected = aspectRatio == ratio,
                                            onClick = { aspectRatio = ratio },
                                            label = { Text(ratio) }
                                        )
                                    }
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text("Clip Duration", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    listOf("5s", "10s").forEach { dur ->
                                        FilterChip(
                                            selected = duration == dur,
                                            onClick = { duration = dur },
                                            label = { Text(dur) }
                                        )
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = {
                                if (prompt.isNotBlank()) {
                                    coroutineScope.launch {
                                        isCompiling = true
                                        generatedVideoResult = null
                                        progressVal = 0f

                                        val steps = listOf(
                                            "Parsing visual token embeddings..." to 0.15f,
                                            "Constructing 3D spatial wireframes..." to 0.35f,
                                            "Slicing frame rates at 30fps diffusion..." to 0.55f,
                                            "Applying cinematic shading & raytracing..." to 0.75f,
                                            "Finalizing motion synthesis compile..." to 0.95f,
                                            "Veo compile completed successfully!" to 1.0f
                                        )

                                        steps.forEach { (status, prog) ->
                                            compileStatus = status
                                            while (progressVal < prog) {
                                                progressVal += 0.02f
                                                delay(40)
                                            }
                                            delay(300)
                                        }

                                        generatedVideoResult = VideoCompileResult(
                                            prompt = prompt,
                                            aspectRatio = aspectRatio,
                                            duration = duration,
                                            scenes = listOf(
                                                "Scene 1: Space traveler enters dense sapphire foliage.",
                                                "Scene 2: Glimmering crystals refract crimson sunlight.",
                                                "Scene 3: Deep low-panning camera focus on crystal formation."
                                            )
                                        )
                                        isCompiling = false
                                        prompt = ""
                                    }
                                }
                            },
                            enabled = !isCompiling && prompt.isNotBlank(),
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SparkexGold,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Videocam, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generate AI Cinematic Clip")
                        }
                    }
                }
            }

            if (isCompiling) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Compiling Render Pipeline",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                            LinearProgressIndicator(
                                progress = { progressVal },
                                modifier = Modifier.fillMaxWidth(),
                                color = SparkexGold
                            )
                            Text(
                                text = compileStatus,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            generatedVideoResult?.let { result ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Ready Director Cut",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = SparkexGold
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.primary)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "VEO RENDER",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }

                            // Mock video playback viewport with anim
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(if (result.aspectRatio == "16:9") 1.77f else if (result.aspectRatio == "9:16") 0.56f else 1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.Black)
                                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Movie,
                                        contentDescription = null,
                                        tint = SparkexGold,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "[Playing: ${result.prompt.take(30)}...]",
                                        fontSize = 11.sp,
                                        color = Color.LightGray
                                    )
                                    Text(
                                        text = "Duration: ${result.duration} | Ratio: ${result.aspectRatio}",
                                        fontSize = 9.sp,
                                        color = Color.Gray
                                    )
                                }
                            }

                            Text(
                                text = "Director Storyboard:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )

                            result.scenes.forEach { scene ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = SparkexGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = scene,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

data class VideoCompileResult(
    val prompt: String,
    val aspectRatio: String,
    val duration: String,
    val scenes: List<String>
)
