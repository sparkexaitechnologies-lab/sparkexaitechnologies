package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        containerColor = Color(0xFFF7F7F8), // Soft off-white
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Video Director", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFF7F7F8)
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Videocam,
                            contentDescription = "Video Creator",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Text(
                        text = "AI Video Director Studio",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Harness the power of Google Veo text-to-video diffusion engines to craft professional cinematic visual shots instantly.",
                        fontSize = 14.sp,
                        color = Color(0xFF6E6E73),
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFE5E5EA))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Describe Your Cinematic Scene",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        OutlinedTextField(
                            value = prompt,
                            onValueChange = { prompt = it },
                            placeholder = { Text("Cinematic slow tracking shot of a silver astronaut exploring a vibrant alien crystal forest, cinematic lighting, 8k...") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Black,
                                unfocusedBorderColor = Color(0xFFE5E5EA),
                                focusedLabelColor = Color.Black,
                                unfocusedLabelColor = Color(0xFF6E6E73)
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Aspect Ratio", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6E6E73))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf("16:9", "9:16", "1:1").forEach { ratio ->
                                        MinimalSelectableChip(
                                            text = ratio,
                                            isSelected = aspectRatio == ratio,
                                            onClick = { aspectRatio = ratio }
                                        )
                                    }
                                }
                            }

                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Clip Duration", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6E6E73))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf("5s", "10s").forEach { dur ->
                                        MinimalSelectableChip(
                                            text = dur,
                                            isSelected = duration == dur,
                                            onClick = { duration = dur }
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
                                containerColor = Color.Black,
                                contentColor = Color.White,
                                disabledContainerColor = Color(0xFFE5E5EA),
                                disabledContentColor = Color(0xFF6E6E73)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Videocam, 
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generate AI Cinematic Clip", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (isCompiling) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFFE5E5EA))
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Compiling Render Pipeline",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.Black
                            )
                            LinearProgressIndicator(
                                progress = { progressVal },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                color = Color.Black,
                                trackColor = Color(0xFFF7F7F8)
                            )
                            Text(
                                text = compileStatus,
                                fontSize = 12.sp,
                                color = Color(0xFF6E6E73)
                            )
                        }
                    }
                }
            }

            generatedVideoResult?.let { result ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFFE5E5EA))
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Ready Director Cut",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.Black
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.Black)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "VEO RENDER",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            // Mock video playback viewport with anim
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(if (result.aspectRatio == "16:9") 1.77f else if (result.aspectRatio == "9:16") 0.56f else 1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.Black),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Movie,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(44.dp)
                                    )
                                    Text(
                                        text = "[Playing: ${result.prompt.take(30)}...]",
                                        fontSize = 12.sp,
                                        color = Color.LightGray,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "Duration: ${result.duration} | Ratio: ${result.aspectRatio}",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                            }

                            Text(
                                text = "Director Storyboard",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                result.scenes.forEach { scene ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.PlayArrow,
                                            contentDescription = null,
                                            tint = Color.Black,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = scene,
                                            fontSize = 13.sp,
                                            color = Color.Black
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MinimalSelectableChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Color.Black else Color.White)
            .border(
                width = 1.dp,
                color = if (isSelected) Color.Black else Color(0xFFE5E5EA),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.White else Color(0xFF6E6E73)
        )
    }
}

data class VideoCompileResult(
    val prompt: String,
    val aspectRatio: String,
    val duration: String,
    val scenes: List<String>
)
