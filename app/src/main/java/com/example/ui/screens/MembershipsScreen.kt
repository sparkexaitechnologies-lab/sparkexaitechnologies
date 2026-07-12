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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
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
        containerColor = MaterialTheme.colorScheme.background, // Soft off-white
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("AI Plans", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = MaterialTheme.colorScheme.onSurface) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface,
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
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp)
            ) {
                // Intro Title section
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onSurface),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AutoAwesome,
                                contentDescription = "AI Plans",
                                tint = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        
                        Text(
                            text = "Elevate Your Workspace",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = "Choose a processing speed and model capacity level designed to optimize your professional workflow.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }

                // Current Plan Indicator
                item {
                    val currentPlan = profile.subscriptionType
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "CURRENT PLAN",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = when (currentPlan) {
                                        "Pro Plus" -> "Pro Plus Plan"
                                        "Pro" -> "Pro Plan"
                                        else -> "Free Plan"
                                    },
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.onSurface)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "ACTIVE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.surface
                                )
                            }
                        }
                    }
                }

                // 1. Free Plan Card
                item {
                    val isFreeActive = profile.subscriptionType == "Free" || profile.subscriptionType.isBlank()
                    PlanSelectionCard(
                        title = "Free",
                        price = "₹0 / month",
                        isActive = isFreeActive,
                        benefits = listOf(
                            "Basic AI Reasoning Model",
                            "Standard Daily Usage Limits",
                            "Standard Response Speed",
                            "Ad-supported local framework"
                        ),
                        onActionClick = {
                            viewModel.updateSubscription("Free")
                            Toast.makeText(context, "Downgraded to Free Tier", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                // 2. Pro Plan Card (₹199)
                item {
                    val isProActive = profile.subscriptionType == "Pro"
                    PlanSelectionCard(
                        title = "Pro",
                        price = "₹199 / month",
                        isActive = isProActive,
                        benefits = listOf(
                            "Faster AI Response Speed",
                            "Higher Daily Chat Limits",
                            "More AI Image Generations",
                            "Analytical Reasoning Enabled"
                        ),
                        onActionClick = {
                            viewModel.updateSubscription("Pro")
                            selectedPlanForSuccess = "Pro Plan"
                            showSuccessAnimation = true
                            Toast.makeText(context, "Upgraded to Pro Plan", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                // 3. Pro Plus Plan Card (₹399)
                item {
                    val isProPlusActive = profile.subscriptionType == "Pro Plus"
                    PlanSelectionCard(
                        title = "Pro Plus",
                        price = "₹399 / month",
                        isActive = isProPlusActive,
                        benefits = listOf(
                            "Fastest Premium AI Responses",
                            "Highest Daily Usage Limits",
                            "Premium Multi-turn Capabilities",
                            "Priority GPU Processing Priority",
                            "Maximum Daily AI Image Generations"
                        ),
                        onActionClick = {
                            viewModel.updateSubscription("Pro Plus")
                            selectedPlanForSuccess = "Pro Plus Plan"
                            showSuccessAnimation = true
                            Toast.makeText(context, "Upgraded to Pro Plus Plan", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                // Restore Purchase Action
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(
                            onClick = {
                                Toast.makeText(context, "Purchased receipt verified with store.", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Text(
                                text = "Restore Purchases",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp
                            )
                        }

                        Text(
                            text = "Subscriptions automatically renew monthly unless canceled in account settings. Billing handled securely through native platform services.",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(horizontal = 24.dp)
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
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        .clickable { showSuccessAnimation = false },
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .width(300.dp)
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = "Success",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(56.dp)
                            )

                            Text(
                                text = "Welcome to $selectedPlanForSuccess",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "Your account has successfully upgraded. All pro tier benefits are now active.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )

                            Button(
                                onClick = { showSuccessAnimation = false },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurface, contentColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(50.dp)
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
fun PlanSelectionCard(
    title: String,
    price: String,
    isActive: Boolean,
    benefits: List<String>,
    onActionClick: () -> Unit
) {
    val isProPlus = title == "Pro Plus"
    val accentColor = when (title) {
        "Pro" -> Color(0xFF6366F1) // Indigo accent
        "Pro Plus" -> Color(0xFFF59E0B) // Amber/Gold accent
        else -> MaterialTheme.colorScheme.onSurfaceVariant // Cool Gray
    }

    val badgeText = when (title) {
        "Pro" -> "POPULAR"
        "Pro Plus" -> "BEST VALUE"
        else -> "ESSENTIAL"
    }

    val cardBorder = if (isProPlus) {
        BorderStroke(
            width = 2.5.dp,
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFFF59E0B), Color(0xFFEF4444)) // Premium amber-red gradient
            )
        )
    } else {
        BorderStroke(
            width = if (isActive) 2.dp else 1.dp,
            color = if (isActive) accentColor else MaterialTheme.colorScheme.outline
        )
    }

    val cardBgColor = if (isProPlus) {
        Color(0xFFFFFDF9) // Distinct subtle warm/gold premium background fill
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = cardBgColor
        ),
        shape = RoundedCornerShape(20.dp),
        border = cardBorder
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
                    // Badge tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (isProPlus) {
                                    Color(0xFFF59E0B).copy(alpha = 0.12f)
                                } else {
                                    accentColor.copy(alpha = 0.12f)
                                }
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = badgeText,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isProPlus) Color(0xFFD97706) else accentColor,
                            letterSpacing = 0.8.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(2.dp))
                    
                    Text(
                        text = title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = price,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (isActive) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(accentColor)
                            .padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = "Active Plan",
                            tint = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFF2F2F7), thickness = 1.dp)

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

            Button(
                onClick = onActionClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isActive) Color(0xFFF2F2F7) else if (isProPlus) Color(0xFFD97706) else accentColor,
                    contentColor = if (isActive) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(50.dp), // Premium, pill-shaped fully rounded corners
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                Text(
                    text = if (isActive) "Currently Active" else "Upgrade Plan",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}
