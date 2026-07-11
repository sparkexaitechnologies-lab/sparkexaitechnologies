package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.GeneratedImageItem
import com.example.ui.viewmodel.SparkexViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImagesViewerScreen(
    viewModel: SparkexViewModel,
    onBack: () -> Unit
) {
    val images by viewModel.generatedImages.collectAsState()
    val isGeneratingImage by viewModel.isGeneratingImage.collectAsState()
    val generationError by viewModel.imageGenerationError.collectAsState()

    var promptText by remember { mutableStateOf("") }
    var selectedSize by remember { mutableStateOf("1K") } // "1K", "2K", "4K"
    var activeViewItem by remember { mutableStateOf<GeneratedImageItem?>(null) }

    val sizesList = listOf("1K", "2K", "4K")

    Scaffold(
        containerColor = Color(0xFFF7F7F8), // Soft off-white
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Creative Studio", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color.Black) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                        .clip(RoundedCornerShape(28.dp)) // circular
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Image,
                        contentDescription = "Creative Studio",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = "AI Creative Studio",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Generate high-resolution digital art, illustration canvases, and photorealistic concept renders instantly.",
                    fontSize = 14.sp,
                    color = Color(0xFF6E6E73),
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }

            // Creative Generator Box
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
                        text = "Generate High-Quality Artwork",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    OutlinedTextField(
                        value = promptText,
                        onValueChange = { promptText = it },
                        placeholder = { Text("A futuristic neon lion wearing glasses...") },
                        modifier = Modifier.fillMaxWidth(),
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
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Size Affordance Selector
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Resolution:", 
                                fontSize = 11.sp, 
                                fontWeight = FontWeight.Bold, 
                                color = Color(0xFF6E6E73)
                            )
                            sizesList.forEach { size ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selectedSize == size) Color.Black else Color(0xFFF7F7F8))
                                        .border(
                                            width = 1.dp,
                                            color = if (selectedSize == size) Color.Black else Color(0xFFE5E5EA),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { selectedSize = size }
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = size,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedSize == size) Color.White else Color(0xFF6E6E73)
                                    )
                                }
                            }
                        }

                        // Generate Button
                        Button(
                            onClick = {
                                if (promptText.isNotBlank()) {
                                    viewModel.generateArtImage(promptText, selectedSize) {
                                        promptText = ""
                                    }
                                }
                            },
                            enabled = !isGeneratingImage && promptText.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Black,
                                contentColor = Color.White,
                                disabledContainerColor = Color(0xFFE5E5EA),
                                disabledContentColor = Color(0xFF6E6E73)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isGeneratingImage) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.Black,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Generate", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }

                    if (generationError != null) {
                        Text(
                            text = generationError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Generated Grid Title
            Text(
                text = "My Digital Creations",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            if (images.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Image,
                            contentDescription = null,
                            tint = Color(0xFF6E6E73),
                            modifier = Modifier.size(44.dp)
                        )
                        Text(
                            text = "No creations found yet.",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = "Describe an artwork above to start.",
                            fontSize = 13.sp,
                            color = Color(0xFF6E6E73)
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(images) { item ->
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0xFFE5E5EA), RoundedCornerShape(12.dp))
                                .clickable { activeViewItem = item }
                        ) {
                            AsyncImage(
                                model = File(item.imagePath),
                                contentDescription = item.prompt,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Size Badge
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.Black.copy(alpha = 0.7f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = item.size,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // Active Art Modal/Dialog
        activeViewItem?.let { item ->
            AlertDialog(
                onDismissRequest = { activeViewItem = null },
                containerColor = Color.White,
                shape = RoundedCornerShape(24.dp),
                confirmButton = {
                    Button(
                        onClick = { activeViewItem = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Close", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    IconButton(
                        onClick = {
                            viewModel.deleteGeneratedImage(item)
                            activeViewItem = null
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Delete Image",
                            tint = Color.Red,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0xFFE5E5EA), RoundedCornerShape(12.dp))
                        ) {
                            AsyncImage(
                                model = File(item.imagePath),
                                contentDescription = item.prompt,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Text(
                            text = "Prompt: ${item.prompt}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        Text(
                            text = "Resolution: ${item.size} (${if (item.size == "1K") "1024x1024" else if (item.size == "2K") "2048x2048" else "4096x4096"})",
                            fontSize = 12.sp,
                            color = Color(0xFF6E6E73)
                        )
                    }
                }
            )
        }
    }
}
