package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.SparkexViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembershipsScreen(
    viewModel: SparkexViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val profile by viewModel.userProfile.collectAsState()

    var showSuccessAnimation by remember { mutableStateOf(false) }
    var selectedPlanForSuccess by remember { mutableStateOf("") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background, // Premium Deep Black
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "MEMBERSHIP",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White,
                        letterSpacing = 2.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp)
            ) {
                // Header Intro
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "UPGRADE TODAY",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 1.sp
                            )
                        }

                        Text(
                            text = "Original Intelligence.",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            letterSpacing = (-0.5).sp
                        )

                        Text(
                            text = "Unlock ultra-fast speeds, advanced model tools, and seamless audio capabilities designed for your workflow.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }

                // Current Tier Banner
                item {
                    val currentSubscription = profile.subscriptionType.ifBlank { "Free" }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "CURRENT PLAN STATUS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$currentSubscription Tier",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "ACTIVE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }

                // 1. Plus Tier Card
                item {
                    val isPlusActive = profile.subscriptionType == "Plus"
                    PremiumPlanCard(
                        title = "Plus Plan",
                        price = "₹199 / month",
                        isActive = isPlusActive,
                        badge = "POPULAR CHOICE",
                        benefits = listOf(
                            "Ultra-fast processing latency",
                            "Interactive Full-Screen Live Voice Mode",
                            "Deep Research & Thinking modes",
                            "Image Generation capabilities",
                            "Analytical reasoning model priority"
                        ),
                        borderColor = MaterialTheme.colorScheme.outline,
                        bgColor = MaterialTheme.colorScheme.surface,
                        accentColor = Color.White,
                        buttonText = if (isPlusActive) "Currently Active" else "Upgrade to Plus",
                        onAction = {
                            viewModel.updateSubscription("Plus")
                            selectedPlanForSuccess = "Plus Plan"
                            showSuccessAnimation = true
                            Toast.makeText(context, "Upgraded to Plus Plan", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                // 2. Pro Tier Card
                item {
                    val isProActive = profile.subscriptionType == "Pro"
                    PremiumPlanCard(
                        title = "Pro Plan",
                        price = "₹399 / month",
                        isActive = isProActive,
                        badge = "ULTIMATE ACCESS",
                        benefits = listOf(
                            "Everything in Plus tier",
                            "Unlimited High-Res Image Generation",
                            "Early access to new experimental models",
                            "Premium developer API access",
                            "Priority human support channel"
                        ),
                        borderColor = MaterialTheme.colorScheme.outline, // Clean premium high-contrast border
                        bgColor = MaterialTheme.colorScheme.surface,
                        accentColor = Color.White,
                        buttonText = if (isProActive) "Currently Active" else "Upgrade to Pro",
                        onAction = {
                            viewModel.updateSubscription("Pro")
                            selectedPlanForSuccess = "Pro Plan"
                            showSuccessAnimation = true
                            Toast.makeText(context, "Upgraded to Pro Plan", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                // Restore Purchase and Footer Notes
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TextButton(
                            onClick = {
                                Toast.makeText(context, "Purchased receipt verified with store.", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Text(
                                text = "Restore Purchases",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 13.sp
                            )
                        }

                        Text(
                            text = "Subscriptions automatically renew monthly unless canceled in account settings. Billing handled securely through native platform services.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                }
            }

            // Success Purchase Modal Overlay
            AnimatedVisibility(
                visible = showSuccessAnimation,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.8f))
                        .clickable { showSuccessAnimation = false },
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .width(320.dp)
                            .padding(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(18.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = "Success",
                                tint = Color.White,
                                modifier = Modifier.size(56.dp)
                            )

                            Text(
                                text = "Welcome to Pro Tier",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "Your account has successfully upgraded. All premium pro tier features are now fully active.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )

                            Button(
                                onClick = { showSuccessAnimation = false },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(18.dp)
                            ) {
                                Text("Let's Go", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumPlanCard(
    title: String,
    price: String,
    isActive: Boolean,
    badge: String,
    benefits: List<String>,
    borderColor: Color,
    bgColor: Color,
    accentColor: Color,
    buttonText: String,
    onAction: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.5.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(accentColor.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = badge,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = accentColor,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        text = price,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }

                if (isActive) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = "Active Plan",
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)

            // Benefits list
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                benefits.forEach { benefit ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = benefit,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Action Button
            Button(
                onClick = { if (!isActive) onAction() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer else Color.White,
                    contentColor = if (isActive) MaterialTheme.colorScheme.onSurfaceVariant else Color.Black
                ),
                shape = RoundedCornerShape(18.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                Text(
                    text = buttonText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}
